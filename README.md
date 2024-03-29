# Bike Dump

Bike Dump is a Java GUI that can be used to manage and extract stats from GPX 1.0, GPX 1.1 and TCX 2 activities from your cycling/mountain biking workouts.

It also offers graphs and history stats.

## Screenshots

<img src="img/img1.png" height="200" width="350">
<img src="img/img2.png" height="200" width="350">
<img src="img/img3.png" height="200" width="350">
<img src="img/img4.png" height="200" width="350">
<img src="img/img5.png" height="200" width="350">
<img src="img/img6.png" height="200" width="350">

## Prerequisites

java >= 11 (11, 17 and 21 tested)

## How to use

Run Bike Dump:

```
java -jar dist/bikedump-0.2.4-release.jar
```

## How to Compile

Before compiling add the libraries to maven repo:

```
cd dist
sh install-libs.sh
```

Compile with Apache Maven:

```
mvn clean compile
```

Create an executable jar with Apache Maven:

```
mvn clean package
```

Run Bike Dump: 

```
java -jar target/bikedump-0.2.4-release.jar
```

## External libraries: LICENSE

Bike Dump uses these external libraries:

| Library          | Link                                                 | License                                                             |      
|------------------|------------------------------------------------------|---------------------------------------------------------------------|
| sunrisesunsetlib | https://github.com/mikereedell/sunrisesunsetlib-java | [Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)           |
| jmapviewer       | http://wiki.openstreetmap.org/wiki/JMapViewer        | [GPL](https://www.gnu.org/licenses/gpl-3.0.html)                    |                         | [MIT](https://opensource.org/licenses/MIT)                          |         
| kyro             | https://github.com/EsotericSoftware/kryo             | [BSD-3-Clause](https://opensource.org/licenses/BSD-3-Clause)        | 
| ormlite-core     | https://github.com/j256/ormlite-core                 | [ISC](https://opensource.org/licenses/ISC)                          |          
| JFreeChart       | https://github.com/jfree/jfreechart                  | [LGPL-2.1](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html) |     

For additional details see:
[NOTICE file](LICENSE/NOTICE.md)