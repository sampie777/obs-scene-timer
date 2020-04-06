# OBS Scene timer

![Screenshot 1](img/screenshot1.png)

_Screenshot with 18 seconds left on the countdown_

## Setup

1. Download and install [obs-websocket](https://github.com/Palakis/obs-websocket)
1. Launch this application and enjoy
1. Edit the settings in the _user.properties_ file, which will be created after first launch of the application

## How it works

* The application will connect on startup with a current running OBS websocket server, as specified in the properties.
* All available OBS scenes are loaded and the time limit for each scene can be adjusted.
* When the current scene changes, the timer will reset and start coutning again at 0 seconds. You can also reset the timer to 0 by clicking the reset button.
* When a time limit > 0 is set for the current scene, a countdown is also displayed including a track/progress bar.
* When the countdown nears the time limit, the screen will turn yellow. 
* When the countdown has reached 0, the screen will turn red and the countdown will count the elapsed time since the time limit has passed.
