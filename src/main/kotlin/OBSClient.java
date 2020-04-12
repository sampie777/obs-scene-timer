import com.xuggle.xuggler.IContainer;
import config.Config;
import net.twasi.obsremotejava.OBSRemoteController;
import net.twasi.obsremotejava.objects.Scene;
import net.twasi.obsremotejava.requests.GetCurrentScene.GetCurrentSceneResponse;
import net.twasi.obsremotejava.requests.GetSceneList.GetSceneListResponse;
import net.twasi.obsremotejava.requests.GetSourceSettings.GetSourceSettingsResponse;
import objects.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OBSClient {
    Logger logger = Logger.getLogger(OBSClient.class.getName());

    private OBSRemoteController controller;
    private Timer sceneListenerTimer = new Timer();
    private int sceneListenerTimerInterval = 1000;

    public OBSClient() {
        initOBS();
    }

    public void initOBS() {
        logger.info("Connecting to OBS on: " + Config.INSTANCE.getObsAddress());
        Globals.INSTANCE.setOBSConnectionStatus(OBSStatus.CONNECTING);
        GUI.INSTANCE.refreshOBSStatus();

        controller = new OBSRemoteController(Config.INSTANCE.getObsAddress(), false);

        if (controller.isFailed()) { // Awaits response from OBS
            // Here you can handle a failed connection request
            logger.severe("Failed to create controller");
            Globals.INSTANCE.setOBSConnectionStatus(OBSStatus.CONNECTION_FAILED);
            GUI.INSTANCE.refreshOBSStatus();
        }

        controller.registerDisconnectCallback(response -> {
            logger.info("Disconnected from OBS");
            Globals.INSTANCE.setOBSConnectionStatus(OBSStatus.DISCONNECTED);
            GUI.INSTANCE.refreshOBSStatus();
        });

        controller.registerConnectCallback(connectResponse -> {
            logger.info("Connected to OBS");
            Globals.INSTANCE.setOBSConnectionStatus(OBSStatus.CONNECTED);
            GUI.INSTANCE.refreshOBSStatus();

            getScenes();

            startSceneWatcherTimer();
        });

        try {
            controller.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startSceneWatcherTimer() {
        sceneListenerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                OBSSceneTimer.INSTANCE.increaseTimer();
                GUI.INSTANCE.refreshTimer();

                logger.fine("Retrieving current scene");
                controller.getCurrentScene(res -> {
                    GetCurrentSceneResponse currentScene = (GetCurrentSceneResponse) res;

                    if (!OBSSceneTimer.INSTANCE.getCurrentSceneName().equals(currentScene.getName())) {
                        OBSSceneTimer.INSTANCE.setCurrentSceneName(currentScene.getName());
                        logger.info("New scene: " + OBSSceneTimer.INSTANCE.getCurrentSceneName());

                        OBSSceneTimer.INSTANCE.resetTimer();
                        OBSSceneTimer.INSTANCE.setCurrentSceneName(OBSSceneTimer.INSTANCE.getCurrentSceneName());

                        GUI.INSTANCE.switchedScenes();
                        GUI.INSTANCE.refreshTimer();

                        getScenes();
                    }
                });

                Config.INSTANCE.save();
            }
        }, Config.INSTANCE.getObsConnectionDelay(), sceneListenerTimerInterval);
    }

    private void getScenes() {
        logger.info("Retrieving scenes");
        Globals.INSTANCE.setOBSStatus(OBSStatus.LOADING_SCENES);
        GUI.INSTANCE.refreshOBSStatus();

        controller.getScenes((response) -> {
            GetSceneListResponse res = (GetSceneListResponse) response;
            logger.info(res.getScenes().size() + " scenes retrieved");

            setOBSScenes(res.getScenes());

            Globals.INSTANCE.setOBSStatus(null);
            GUI.INSTANCE.refreshOBSStatus();
        });
    }

    private void setOBSScenes(List<Scene> scenes) {
        Globals.INSTANCE.getScenes().clear();
        for (Scene scene : scenes) {
            TScene tScene = new TScene();
            tScene.setName(scene.getName());

            if (scene.getSources() != null) {
                ArrayList<TSource> tSources = scene.getSources()
                        .stream()
                        .map(source -> {
                            TSource tSource = new TSource();
                            tSource.setName(source.getName());
                            tSource.setType(source.getType());
                            return tSource;
                        })
                        .collect(Collectors.toCollection(ArrayList::new));

                tScene.setSources(tSources);
            }

            Globals.INSTANCE.getScenes().put(tScene.getName(), tScene);
        }

        if (!loadSourceSettings()) {
            GUI.INSTANCE.refreshScenes();
            Globals.INSTANCE.setOBSStatus(null);
            GUI.INSTANCE.refreshOBSStatus();
        }
    }

    /**
     * Loads the scene sources. If there are no sources, or if it can't load the sources, it will return false.
     * Otherwise it will return true and will eventually call GUI.INSTANCE.refreshScenes();
     * @return
     */
    private boolean loadSourceSettings() {
        if (!(Config.INSTANCE.getObsAddress().contains("localhost")
                || Config.INSTANCE.getObsAddress().contains("127.0.0.1"))) {
            logger.info("Not going to try to get the video lengths, because the source files are probably running on another computer");
            return false;
        }

        Globals.INSTANCE.setOBSStatus(OBSStatus.LOADING_SCENE_SOURCES);
        GUI.INSTANCE.refreshOBSStatus();

        List<TSource> sources = Globals.INSTANCE.getScenes().values().stream()
                .flatMap((TScene tScene) -> tScene.getSources().stream())
                .collect(Collectors.toCollection(ArrayList::new));

        if (sources.size() > 0) {
            loadSourceSettings(sources);
            return true;
        }
        return false;
    }

    private void loadSourceSettings(List<TSource> sources) {
        TSource source = sources.remove(0);

        logger.info("Loading source settings for source: " + source.getName());

        controller.getSourceSettings(source.getName(), response -> {
            GetSourceSettingsResponse res = (GetSourceSettingsResponse) response;

            source.setSettings(res.getSourceSettings());
            source.setType(res.getSourceType());

            if ("ffmpeg_source".equals(source.getType())) {
                source.setFileName((String) source.getSettings().get("local_file"));

                int videoLength = 0;
                try {
                    videoLength = (int) getVideoLength(source.getFileName());
                } catch (Exception e) {
                    logger.severe("Failed to get video length: " + e.toString());
                }
                source.setVideoLength(videoLength);
            }

            if (sources.size() == 0) {
                GUI.INSTANCE.refreshScenes();
                Globals.INSTANCE.setOBSStatus(null);
                GUI.INSTANCE.refreshOBSStatus();
            } else {
                loadSourceSettings(sources);
            }
        });
    }

    /**
     * Returns video length in seconds, or 0 if file not found
     *
     * @param filename
     * @return
     */
    private long getVideoLength(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            logger.warning("File does not exists: " + filename);
            return 0;
        }

        logger.info("Getting duration of: " + filename);

        IContainer container = IContainer.make();
        int result = container.open(filename, IContainer.Type.READ, null);
        long duration = container.getDuration();

        return TimeUnit.MICROSECONDS.toSeconds(duration);
    }
}
