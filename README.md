# Bikedump

Manage and extract stats from your bike gps gpx tcx activities.

Generate graphs and history stats.

Provide integration with Strava activities.

## Screenshot

see img folder

## Prerequisites

java >= 11

## Compile, Test, Run

### Compile: ###

Before compiling add the libraries to maven repo:

```
cd dist
sh install-libs.sh
```

### Run: ###

```
mvn clean compile && mvn exec:java -Dexec.mainClass="org.hifly.bikedump.gui.BikeDump"
```

## External libraries: LICENSE

bikedump uses these external libraries:

| Library          | Link | License
|------------------|------|-------|
| sunrisesunsetlib |https://github.com/mikereedell/sunrisesunsetlib-java|Apache 2.0
| jmapviewer       |http://wiki.openstreetmap.org/wiki/JMapViewer|GPL
| JStrava          |https://github.com/dustedrob/JStrava|MIT
| kyro             |https://github.com/EsotericSoftware/kryo|BSD-3-Clause
| ormlite-core |https://github.com/j256/ormlite-core|ISC
| JFreeChart |https://github.com/jfree/jfreechart|LGPL-2.1


For additional details see:
[NOTICE file](LICENSE/NOTICE.md)

