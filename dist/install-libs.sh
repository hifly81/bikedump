mvn install:install-file -Dfile=gpx.jar -DgroupId=com.topografix.gpx -DartifactId=gpxmodel -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=gpx1.0.jar -DgroupId=com.topografix.gpx -DartifactId=gpxmodelx0 -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=tcx.jar -DgroupId=com.garmin.xmlschemas.trainingCenterDatabase -DartifactId=tcxmodel -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=kml21.jar -DgroupId=com.google.earth.kml -DartifactId=x21 -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=kml22.jar -DgroupId=net.opengis.kml -DartifactId=x22 -Dversion=1.0 -Dpackaging=jar
