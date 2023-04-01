# PlayerJAVA
Simple Java-based player app for playing DMX recordings made with the DMX Recorder app with audio. The app simply plays the show ASAP after being started. Use a terminal script or system scheduler to create and schedule shows. This app is intended to allow users of the DMX Recorder app to play shows on non-Windows platforms. In my testing, I was able to run a show on macOS ARM64 with a .WAV audio file.

# Instructions

## Setup
- Download and install the official Java runtime on your device. I developed this with version 20. 
- Download the bin/App.class file from this repository to your device.
- From the terminal or console, navigate the folder containing the App.class file.
  - It is easiest to place the App.class file and all of your DMX and audio files in the same folder.

## Playback
This example assumes your terminal is open to the folder where the App.class file is stored.

From the terminal:
```
$>java App [path of .DMX file] [IP Address] [Optional: Audio file]
```
```
$>java App Bells.dmx 192.168.1.255 Bells.wav
```

As with the Windows app, you can unicast to a single IP address by entering the device's IP address. You can broadcast by replacing the last number in the IP address with 255. For example, 192.168.1.15 is a unicast address. 192.168.1.255 will broadcast to all devices on whose address starts with 192.168.1.*** .
