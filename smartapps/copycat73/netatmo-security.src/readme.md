# Installing the Netatmo security smartapp

https://github.com/CopyCat73/SmartThings-Dev/tree/master/smartapps/copycat73/netatmo-security.src

Copyright (c) 2018 Nick Veenstra (https://github.com/copycat73)

## Functionality

This smartapp supports Netatmo Welcome and Presence devices for motion detection, taking snapshots and presence monitoring. 
 

## Set up a Netatmo developer account

Go to https://dev.netatmo.com and sign up for a developer account. Click "create your app" to install a new application, and give it a name that references smartthings so you know what it's for later on. 

![netatmo app](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/netatmo_app.png)

You should now have a client id and client secret:

![netatmo secret](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/netatmo_secret.png)


## Install the code

Create the smartapp by clicking "+ new smartapp" in the SmartThings IDE. 

![new smartapp](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/new_smartapp.png)

Select "from code": 

![new smartapp](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/new_smartapp_code.png)

and paste the code from:

https://raw.githubusercontent.com/CopyCat73/SmartThings-Dev/master/smartapps/copycat73/netatmo-security.src/netatmo-security.groovy

Create the Device Type Handlers in the same way (under "my device type handlers"):

Netatmo Person: https://raw.githubusercontent.com/CopyCat73/SmartThings-Dev/master/devicetypes/copycat73/netatmo-person.src/netatmo-person.groovy

Netatmo Welcome: https://raw.githubusercontent.com/CopyCat73/SmartThings-Dev/master/devicetypes/copycat73/netatmo-welcome.src/netatmo-welcome.groovy

Netatmo Presence: https://raw.githubusercontent.com/CopyCat73/SmartThings-Dev/master/devicetypes/copycat73/netatmo-presence.src/netatmo-presence.groovy

Make sure to publish each item after it is created. 

## Initial settings

Go into the SmartApps section of your IDE and click on "edit properties" for the Netatmo Security app. 

![edit properties](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/netatmo_edit_properties.png)

Enable oAuth and under the app settings provide the client id and secret you received for your Netatmo Development app. Also make sure to enter the correct server url for the shard your hub is on, e.g. https://graph-eu01-euwest1.api.smartthings.com. Don't put a trailing slash at the end. 

![edit app settings](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/smartapp_settings.png)

## Install the SmartApp

In the SmartThings app, go to Automation, SmartApps. Click "+ add a SmartApp" and into "My Apps". Netatmo Security should be listed there:

![app1](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_1.png | width=300)

Open it and click "Connect to Netatmo". You will be prompted by a Netatmo login. Use your developer account to log in and give permission to access the welcome and presence scope by clicking "yes". Tap "done" to continue. The screen will now confirm that Netatmo is connected to SmartThings.

![app2](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_2.png)
![app3](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_3.png)
![app4](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_4.png)
![app5](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_5.png)
![app6](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_6.png)

Click next to enter the preferences and add the devices and people you want to have monitored. Click save to add them as devices. 

![app7](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_7.png)
![app8](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_8.png)
![app9](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_9.png)

Enable the webhook (explained later):
![app10](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_10.png)

Click save. A popup should say "Successfully added Netatmo Security".

![app11](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_11.png)

You should now see devices (present and welcome) and people show up as devices, depending on what you have activated in the smartapp preferences:

![app12](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_12.png)
![app13](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_13.png)

 A presence camera:
![app14](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_14.png)
From top left to bottom right: a "take" button for snapshots, motion indicator, camera on/off indicator, human detected, car detected, pet detected, home name. 

A welcome camera:
![app15](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_15.png)
From top left to bottom right: a "take" button for snapshots, motion indicator, camera on/off indicator, home name, mark everyone as being away. 

A person presence device:
![app16](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_16.png)
From top left to bottom right: presence indicator, home name, mark this person as being away.

Note that for the camera's theres no images yet. Now comes the hard part.


## Connecting the cameras for image snapshots

Snapshots can only be taken when we have the IP address and a netatmo secret key for the cameras. In the preferences for each camera, there is a local ip and access key setting. 

![app18](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_18.png)

It is easier to set this up via the IDE, so go there and open the my devices screen. Click on your camera device:

![deviceprefs1](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/device_prefs_1.png)

and then click the preferences edit link.

![deviceprefs2](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/device_prefs_2.png)

In a separate browser tab go to https://dev.netatmo.com and sign in.At the top of the screen there should be a link "our resources". Go there and click References > Cameras > Gethomedata. 

![gethomedata_1](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/gethomedata_1.png)

To the right click the arrow at "Try this method by yourself with our TRY IT module.". Then click "try it". You will see a server response, expand the result to "body > homes > cameras". 

![gethomedata_2](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/gethomedata_2.png)
![gethomedata_3](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/gethomedata_3.png)

Expand each camera and look at the "name" property to find the ones you installed in SmartThings. There should be a property "vpn_url" that looks something like "https://v1.netatmo.net/restricted/someip/somelongstring/anotherlongstring".

![gethomedata_4](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/gethomedata_4.png)

Copy the red underlined part (so the first bit after the ip address, watch the slashes!) and paste it into the device preference "camera secret" via the IDE (this would be hard to to in the app). 

![deviceprefs3](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/device_prefs_3.png)

Now you need to find the actual ip address for your camera(s), for instance via your router. Enter that ip address in the "camera ip" setting as seen above.

Keep in mind that the Welcome cameras can be connected via cable or wireless so they have 2 mac addresses. The Netatmo dev try it module shows the cable mac address as the device id. 

You should now be able to go into an installed camera in the smartthings app and take a snapshot from the camera.

## Polling and webhook

The SmartApp polls the Netatmo server every 5 minutes to check the camera properties (currently only to see if it's on) and the presence for each person. 

In the SmartApp preferences it is also possible to activate a webhook. The webhook enables the Netatmo server to talk back to the SmartThings hub, this has the advantage to receive motion and person alerts. To set this up, enable the webhook as shown earlier. Then go into your Netatmo security app and make sure that notification works as you want. E.g. if you've setup "be notified of motion detection" and "only when nobody is home" and the camera(s) have noticed you are home, no motion will be reported.

Once the webhook is running the motion detect part of the camera's should start to work. Also the person presence will be more exact; normally this would be updated only via the 5 minute poll, but now any person recognized will be sent instantly or should I say "instantly" via the webhook. 

Please note that Netatmo is very strict about the webhook. If there are 5 failed attempts to send something to the SmartThings hub (because the service is down or I messed up some code) the webhook will be blocked. Go to https://dev.netatmo.com, open your app and check the webhooks section for ban status. If it's banned you can then unban it manually. 

Both camera type preferences have a setting for the ip address and secret. Since the webhook does not report when motion ends, you need to specify the timeout in the preferences for both camera types. Both types can also be setup to take a snapshot at certain intervals. The presence camera also has the option to link human, pet or car detection to the motion detector, so you could say that a car passing by also counts as motion.

Welcome camera preferences:
![app18](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_18.png)

Presence camera preferences:
![app19](https://raw.githubusercontent.com/CopyCat73/CopyCat73.github.io/master/app_19.png)














