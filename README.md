#### About
this project was tested with openJDK 13 (long term stable).

#### Building and running
From the main directory run
```cmd 
mvn clean package
```
Then, switch to the `IntelMissionControl/target/artifact` directory and run the following command:
```cmd
java -DGDAL_DATA=gdal -jar .\OpenMissionControl.jar -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager -Djogamp.gluegen.UseTempJarCache=false -Dprism.order=d3d -Dsun.java2d.d3d=false
```


#### Run from IntelliJ IDEA

Use the provided `.idea/runConfigurations/Main.xml` run configuration for a list of VM arguments and configuration to run the application with IntelliJ IDEA.