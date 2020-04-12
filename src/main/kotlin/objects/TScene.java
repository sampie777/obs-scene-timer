package objects;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Logger;

public class TScene {
    private final Logger logger = Logger.getLogger(TScene.class.getName());

    private String name = "";
    private ArrayList<TSource> sources = new ArrayList<>();

    public ArrayList<TSource> getSources() {
        return sources;
    }

    public void setSources(ArrayList<TSource> sources) {
        this.sources = sources;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxVideoLength() {
        Optional<TSource> longestVideoLengthSource = getLongestVideoLengthSource();

        if (!longestVideoLengthSource.isPresent()) {
            logger.info("No longest video source found for TScene " + name);
        } else {
            logger.info("Longest video source for TScene '" + name + "' has length = " + longestVideoLengthSource.get().getVideoLength());
        }

        return longestVideoLengthSource.map(TSource::getVideoLength).orElse(0);
    }

    private Optional<TSource> getLongestVideoLengthSource() {
        return sources.stream().max(Comparator.comparingInt(TSource::getVideoLength));
    }
}
