@echo off

SET PATH=%PATH%;%JAVA_HOME%\bin

java -Dimc.description.path=descriptions ^
    -Dimc.manuals.path=manuals ^
    -Dsun.java2d.d3d=false ^
    -cp IntelMissionControl/target/dependency/*;IntelMissionControl/target/IntelMissionControl-1.0.jar;IntelMissionControl/lib64 ^
    -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager ^
    -Djava.library.path=IntelMissionControl/resources/gdal;IntelMissionControl/lib64 ^
    -Djna.library.path=IntelMissionControl/lib64 ^
    -DGDAL_DATA=IntelMissionControl/resources/gdal/data ^
    --add-modules=java.se.ee --add-opens=javafx.controls/javafx.scene.control.skin=ALL-UNNAMED --add-opens=javafx.controls/com.sun.javafx.scene.control.inputmap=ALL-UNNAMED -Djavafx.preloader=com.intel.missioncontrol.ui.SplashScreenPreloader -Dprism.order=d3d -Dsun.java2d.d3d=false ^
    com.intel.missioncontrol.Bootstrapper
