
This is an experimental project regarding onvif devices, I made this source
available to make it handy for me and also in the case that it can help someone else :)

## Summary
This software allows controlling onvif devices (IP cameras) and gives to them
the ability of track objects if the IP camera has movement capabilities.
The software is designed to work for `ARM64` and `x86_64`

## Description
The below information is the latest so far, I might change the code or add new features :p before
update the below documentation but I will try to keep it up to date
The project is devided in two parts
 - device comunication
 - data analysis

the device communication works with NodeJS v9.6.1 and it uses node-onvif v0.1.2 as the backbone
the node server is a rest API which communicates with the devices and has 3 functions

/**getDevices** - broadcasts the network looking for devices that want to handshake with us

/**initDevices** - gathers all the data of a given device

/**move** - receives motion instruction which are transmitted to the specified device

the data analysis is done by Java 9, the flow goes like this

 1. java calls to node server to start a broadcast, node returns to use a json array with the devices found
 2. java creates an object from the result and for each device, it asks node to initialize its data 
 3. from the above operation we create Camera objects for which we start thread which will follow the below flow

## Camera workflow
start a thread which will create a process to break the rstp stream from the camera into frames
start a thread which will read the current frame, transform it into a Matrix (we use OpenCV for this) and
apply haar classifier to each frame to detect interesting objects (like a faces or motion)

if an object is found, a rectangle will be created around the object and an event will be triggered by ab observer which will verify if the object is outside of the hot area (a rectangle contained within the image of configurable size)
if the object is outside of this area, an instruction of motion will be sent to the device to try to center the object, in the meantime, the matrix is transformed into the image and a JavaFX image container will be updated with the result
if nothing is found, we just send the image to the image container


## Installation
 **for node:** 

    npm install
    node jscast.js

**for java**
download OpenCV for the platform you are running with and copy the jar and the native libraries under src/main/libs

    gradle compileJava
    gradle myRun

**Note**: if you don't have installed ffmpeg, please do so, it used to handle the cameras' streams
