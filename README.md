# Bikedump

Manage and extract stats from your bike gps gpx tcx activities.

Generate graphs and history stats.

Provide integration with Strava activities.

## Screenshot

see img folder


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

## External libraries

see LICENSE folder and NOTICE file


## License

Apache License, Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
