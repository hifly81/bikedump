mvn install:install-file -Dfile=gpx.jar -DgroupId=com.topografix.gpx -DartifactId=gpxmodel -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=JMapViewer.jar -DgroupId=org.openstreetmap.gui.jmapviewer -DartifactId=jmapviewer -Dversion=1.0.2 -Dpackaging=jar
mvn install:install-file -Dfile=tcx.jar -DgroupId=com.garmin.xmlschemas.trainingCenterDatabase -DartifactId=tcxmodel -Dversion=1.0 -Dpackaging=jar
