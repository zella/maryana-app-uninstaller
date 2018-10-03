# adb-app-uninstaller

Crossplatform adb gui for deleting any applications (without root).

### Features:
- Select and delete application without command line 

### TODO:
- Info about package from community maintained database


## Usage:
- Enable "usb debugging" in developer settings of android device
- Install adb
- Install java
- Download adb-app-uninstaller.xxx.jar from "releases"
- Run adb-app-uninstaller.xxx.jar

## How build:
- Install sbt (scala build tool)
- Clone repo
- Go to project dir
- `sbt assembly`
- Find adb-app-uninstaller.xxx.jar in target folder

