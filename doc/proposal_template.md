# SDP Proposal - Team \# 17

## Team / App name:
Gabriel, Bastien
Gangloff, Maximilian Christian
Monea, Giovanni
Olsson, Alexander John Viktor
Srinivasan, Ravi Francesco

App Name : to be determined (we discussed alot more about the app features)

## Description:
We are planning to build an AR (Augmented Reality) app. Using this app the user could get additional information about the surrounding mountains using 
its smartphone camera. The info such as the peak name, its height and the distance between the user and the mountain would be displayed on the camera preview. 
The app would also implement some user accounts to create a ranking between users. This ranking would be based on the peaks the user scans (more points for higher peaks or "rare" peaks for instance).  

## Requirements:
### Split app model: 
We are planning to use OpenStreetMap or geonames API to get the POI (point of interest) that are near the user location. By filtering them by type we can extract only peaks to retrieve their information. 

### Sensor usage:
We need to use the GPS to get the user location and altitude, the camera (if it counts as a sensor), the accelerometer (to get the direction pointed by the user) 

### User support:
The user will be able to log using its account to see and increase its score by scanning nearby summits. 

### Local cache:
Cache basic information about the nearby mountains to be able to reuse this data if the location does not change

### Offline mode:
Display the data that is still stored in the cache on the camera preview. The user will be able to see peaks he already scanned on a map. 