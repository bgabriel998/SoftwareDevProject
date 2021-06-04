# CS-306 Software Development Project 
___
## Badges
[![Build Status](https://api.cirrus-ci.com/github/bgabriel998/SoftwareDevProject.svg)](https://cirrus-ci.com/github/bgabriel998/SoftwareDevProject)

[![Maintainability](https://api.codeclimate.com/v1/badges/7d9f8d866811a3ea3fa1/maintainability)](https://codeclimate.com/github/bgabriel998/SoftwareDevProject/maintainability)

[![Test Coverage](https://api.codeclimate.com/v1/badges/7d9f8d866811a3ea3fa1/test_coverage)](https://codeclimate.com/github/bgabriel998/SoftwareDevProject/test_coverage)

___

## PeakAR

PeakAR is an app that will guide you through your hikes, using augmented reality to display in your phone's camera the names and some information on the peaks that surround you. Watch our trailer:

[![PeakAR trailer](https://img.youtube.com/vi/Eddwj1j-FQM/0.jpg)](https://www.youtube.com/watch?v=Eddwj1j-FQM)

### Features

The app provides the user with the following features:

* **Augmented reality**: show the information (name, distance and height) of the peaks that sorround the user
* **Social profile**: save and share the peaks that the user has scanned (both as a list and as pins on an interactive map)
* **Rankings**: based on the points collected by scanning peaks (the higher the peak scanned, the higher the points archieved)
* **Challenges**: open challenges against other users and compete to archieve the most points in a given time window
* **Offline mode**: pre-download the peaks around a location to be able to use the app without any internet connection
* **Photo gallery**: take pictures directly in the app
* **Multi language**: the app is available in Deutsch, English, Français, Italiano and Svenska

___

### Requirements matching
Requirements matching:
* **Split app model**:
  * Use OpenStreetMap, OpenTopography and OSMDroid APIs to retrieve and display informations about the surroundings
* **Sensor usage**: 
  * GPS for the user location and altitude
  * Position sensors (orientation sensors and magnetometers) to detect the rotation of the phone
  * Motion sensors (accelerometers, gravity sensors, gyroscopes, and rotational vector sensors) to create a compass and display the information on the camera-view
* **User support**:
  * Login to the application with a personal account
  * Mountains that were discovered are saved and give points depending on the “rarity” and height
  *  Compete with friends to see who has more points and discovered more mountains
* **Local cache**:
  * Cache basic information about the nearby mountains to be able to reuse this data if the location does not change
* **Offline mode**:
  * Display the data that is still stored in the cache
  * Be able to see which mountains the user discovered and the pictures taken by the user
  * Let the user download an area in advance (Similar to the offline mode of google maps) so that he can use the application even if he has no internet connection   

___

## Useful links
[Software Development Project Moodle](https://moodle.epfl.ch/course/view.php?id=16172)

[Software Development Piazza](https://piazza.com/class/klgt5iozma44iy#)

[Java Documentation](https://docs.oracle.com/en/java/)

[Android Studio Documentation](https://developer.android.com/docs)

___

## Devs
- [Gangloff Maximilian](https://github.com/magangloff)
- [Gabriel Bastien](https://github.com/bgabriel998)
- [Monea Giovanni](https://github.com/giommok)
- [Olsson Alexander](https://github.com/aolsson711)
- [Srinivasan Ravi](https://github.com/ravifrancesco)
