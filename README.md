# OBS Scene Timer

_Author: Samuel-Anton Jansen_

![Screenshot 1](img/screenshot1.png)

_Screenshot with 28 seconds left on the countdown_

Use cases:

* Playing a media file in OBS but not sure when it ends? This timer will keep track of time!
* Need a timer that starts directly when you switch scenes? This timer will start immediately!
* Want to limit scene durations to a specific time? This timer will help you keep track of time!


## Build

You can build this application with Maven, use the prebuild executable jar in [out/](out/), or just download a custom version from [downloads](https://bitbucket.org/sajansen/obs-scene-timer/downloads/).

## Setup

1. Download and install [obs-websocket](https://github.com/Palakis/obs-websocket) for your OBS application.
1. Make sure your OBS websocket is discoverable by the computer you will run this application on. If it's the same computer, no worries. 
1. Launch this application (by running the executable JAR file) with Java and enjoy.
1. Edit the settings in the _user.properties_ file if needed. This file will be created after first launch of the application. Don't run the application when editing these settings.

## How it works

1. The application will connect on startup with an already running OBS websocket server, as specified in the properties.
1. All available OBS scenes are loaded, and the time limit for each scene can be adjusted. If you are running OBS on the same computer as this application, the application will try to automatically set the time limit for each scene with a video file as source.
1. When the current scene changes, the timer will reset and start counting again from 0 seconds. You can also reset the timer to 0 by clicking the reset button. Also, the list of scenes will be reloaded.
1. When a time limit greater than 0 is set for the current scene, a countdown will also be displayed including a track/progress bar.
1. When the countdown approaches the time limit, the screen will turn yellow. 
1. When the countdown has reached 0, the screen will turn red, and the countdown will continue to count the elapsed time since the time limit has passed (negative number).

More info: https://obsproject.com/forum/resources/obs-scene-timer.920/

### Properties

In the same directory as the _.jar_ file, the _user.properties_ can be found. Edit this file in your favorite editor. 

Don't run the application when editing these settings, as your changes won't be loaded until the next launch of the application. Also, the application will overwrite your changes if it is still running. 

_Connection settings_

* `obsAddress` (default: `ws://localhost:4444`): holds the full address of the OBS websocket server. This server can be on any computer in the same network of even over internet, as long as it can be reached by the obs-scene-timer application.
* `obsConnectionDelay` (milli seconds) (default: `1000`): delays connecting to OBS on startup with this value in milli seconds.

_Application color settings_

* `timerBackgroundColor` (rgb) (default: `192,192,192`): a RGB color, separated by comma's, which will be used as the default background color for the timer.
* `approachingLimitColor` (rgb) (default: `255,200,0`): a RGB color, separated by comma's, which will be used as the background color for the timer when it warns that the time limit is being approached (see `smallTimeDifferenceForLimitApproaching` and `largeTimeDifferenceForLimitApproaching`).
* `exceededLimitColor` (rgb) (default: `255,0,0`): a RGB color, separated by comma's, which will be used as the background color for the timer when it warns that the time limit has been reached.

 _Timer settings_
 
 * `smallMinLimitForLimitApproaching` (seconds) (default `20`): don't show time-limit-approaching warnings for time limits smaller than this value.
 * `smallTimeDifferenceForLimitApproaching` (seconds) (default `10`): show the time-limit-approaching warning this amount of seconds before reaching the time limit.
 * `largeMinLimitForLimitApproaching` (seconds) (default `60`): use `largeTimeDifferenceForLimitApproaching` value for time-limit-approaching warning for time limits greater or equal to this value.  
 * `largeTimeDifferenceForLimitApproaching` (seconds) (default `30`): show the time-limit-approaching warning this amount of seconds before reaching the time limit. Only for scene's with a time limit greater or equal than `largeTimeDifferenceForLimitApproaching` value.
 