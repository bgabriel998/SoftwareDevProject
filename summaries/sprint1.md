# Weekly summary for week 1

## Bastien
___
### Retrieve surrounding POI point of Interest from Geonames API:
- Read the documentation of Geonames to find all needed methods
- Create Geonames Free Account
- Implement GeoNames API helper repo to PeakAR to use the functionnalities of GeoNames.
- Implement a new class GeonamesHandler to build requests and send them to geonames using a thread

This task arround 4 hours because I was already familiar with the provider

Next time I will try to find a better provider, because if the user make a request and is located in a place with a lot of surrounding POI (like in Lausanne), the provider won't be able to retrieve the surrounding mountains. 
Also the provider need to be tested more deeply. 

### Filter out Geonames Provider query results
- Filter out the query result to get only the mountains-typed-POI

This task only took an hour.

### Setup project to be able to use Cirrus CI, JaCoCo and CodeClimate

- Setup the project tools

This task much more than expected... 4 hours




## Ravi
___
First I have implemented a Point class that contains latitude longitude and altitude, and a method to compute as crow flies distance between points.

Then I created two classes:
- UserPoint extends Point and contains a reference to a GPSTracker
- POIPoint extends Point and contains a constructor that allows the construction of a POIPoint by passing a GeoPoint from GeoNames API

The GPSTracker uses Android API to monitor the user location. It tries to use the Network as a provider, and if it can't it uses the GPS (less precise).

I created a simple GUI that show all this features working.



## Giovanni
___

I implemented sign-in via Google account and I integrated Firebase for more incoming features.

At first, I struggled as I didn't know about keystores and the sign-in wouldn't work. Therefore, I spent a lot of time figuring out how to make it work.
However, integrating Firebase, at the end of the sprint, was way more simple as I had experience with Google Sign-In.

Next week I will manage my time better and try to make a more accurate estimation of time needed.


## Alexander
___
I implemented the base UI for the app this week. First by making it in Figma to show the team, then implement it to android study.   

My estimate was completly off. I totaly missread how long it takes to learn new systems to develop in.   

Next time I when I will estimate the time it will take to preform a task I won't underestimate the time it takes to learn new systems.


## Maximilian (Scrum Master)
___

I implemented one of my two tasks which were the camera-preview. My time estimate was quite off, reading the documentation and especially the UI-tests took me longer than expected.

Even though the camera preview is implemented it will still need some tweaks like the autofocusing.

Next time, I will try to better estimate the time I will take for the tasks and make sure that I test my code thoroughly.

## Overall team
___

We implemented all but one of the user stories we had assigned to this sprint, and we believe the one remaining is still high-priority and should stay.

Our time estimates were as expected quite off since it is our first week. This will become better over time. On the positive side, the two standup meetings went really well.

Another problem that we had, was that we didn't set up properly the project and so the continuous integration tests failed. Also, the rules on Codeclimate were not properly set which induced us to not properly test our code. This will be a main priority for the upcoming week.
