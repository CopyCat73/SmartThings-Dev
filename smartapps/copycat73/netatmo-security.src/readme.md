# Installing the Netatmo security smartapp

https://github.com/CopyCat73/SmartThings-Dev/tree/master/smartapps/copycat73/netatmo-security.src

Copyright (c) 2018 Nick Veenstra (https://github.com/copycat73)

## Functionality

This smartapp supports Netatmo Welcome and Presence devices for motion detection, taking snapshots and presence monitoring. 
 

## Set up a Netatmo developer account

Go to https://dev.netatmo.com and sign up for a developer account. Click "create your app" to install a new application, and give it a name that references smartthings so you know what it's for later on. You should now have a client id and client secret. 

## Install the code

Create the smartapp by clicking "+ new smartapp" in the SmartThings IDE. Select "from code" and paste the code from:

https://raw.githubusercontent.com/CopyCat73/SmartThings-Dev/master/smartapps/copycat73/netatmo-security.src/netatmo-security.groovy

Create the Device Type Handlers in the same way:

Netatmo Person: https://raw.githubusercontent.com/CopyCat73/SmartThings-Dev/master/devicetypes/copycat73/netatmo-person.src/netatmo-person.groovy

Netatmo Welcome: https://raw.githubusercontent.com/CopyCat73/SmartThings-Dev/master/devicetypes/copycat73/netatmo-welcome.src/netatmo-welcome.groovy

Netatmo Presence: https://raw.githubusercontent.com/CopyCat73/SmartThings-Dev/master/devicetypes/copycat73/netatmo-presence.src/netatmo-presence.groovy

Make sure to publish each item after it is created. 

## Initial settings

Go into the SmartApps section of your IDE and click on "edit properties" for the Netatmo Security app. Enable oAuth and under the app settings provide the client id and secret for your Netatmo Development app. Also make sure to enter the correct server shard your hub is on, e.g. https://graph-eu01-euwest1.api.smartthings.com. Don't put a trailing slash at the end. 

## Install the SmartApp

In the SmartThings app, go to Automation, SmartApps. Click "+ add a SmartApp" and into "My Apps". Netatmo Security should be listed there. Open it and click "Connect to Netatmo". You will be prompted by a Netatmo login. Use your developer account to log in and give permission to access the welcome and presence scope by clicking "yes". Tap "done" to continue. The screen will now confirm that Netatmo is connected to SmartThings.

Click Next. Do not enter any settings yet and click Save (theres a bug here I need to solve you might get a save error when you start setting up preferences now). A popup should say "Successfully added Netatmo Security".

Go back into the SmartApp, click next to enter the preferences and add the devices and people you want to have monitored. Click save to add them as devices. Now comes the tricky part.

## Connecting the cameras for image snapshots

In the preferences for each camera, there is a local ip and access key setting. It is easier to set this up via the IDE, so go there and open the my devices screen. Click on your camera device, and then click the preferences edit link. In a separate browser tab go to https://dev.netatmo.com and sign in.At the top of the screen there should be a link "our resources". Go there and click References > Cameras > Gethomedata. To the right click the arrow at "Try this method by yourself with our TRY IT module.". Then click "try it". You will see a server response, expand the result to "body > homes > cameras". Expand each camera and look at the "name" property to find the ones you installed in SmartThings. There should be a property "vpn_url" that looks something like "https://v1.netatmo.net/restricted/someip/somelongstring/anotherlongstring".

Copy the "somelongstring" part (so the first bit after the ip address) and paste it into the device preference "camera secret" via the IDE (this would be hard to to in the app). Now you need to find the actual ip address for your camera(s), for instance via your router. Enter that ip address in the "camera ip" setting. Keep in mind that the Welcome cameras can be connected via cable or wireless so they have 2 mac addresses. The Netatmo dev try it module shows the cable mac address as the device id. 

You should now be able to go into an installed camera in the smartthings app and take a snapshot from the camera.

## Polling and webhook

The SmartApp polls the Netatmo server every 5 minutes to check the camera properties (currently only to see if it's on) and the presence for each person. In each camera there is also a preference to setup a snapshot to be taken at each poll (around 5 minutes). 

In the SmartApp preferences it is also possible to activate a webhook. The webhook enables the Netatmo server to talk back to the SmartThings hub, this has the advantage to receive motion and person alerts. To set this up, enable the webhook. Then go into your Netatmo security app and make sure that notification works as you want. E.g. if you've setup "be notified of motion detection" and "only when nobody is home" and the camera(s) have noticed you are home, no motion will be reported. 

Once the webhook is running the motion detect part of the camera's should start to work. Also the person presence will be more exact; normally this would be updated only via the 5 minute poll, but now any person recognized will be sent instantly or should I say "instantly" via the webhook. 

Please note that Netatmo is very strict about the webhook. If there are 5 failed attempts to send something to the SmartThings hub (because the service is down or I messed up some code) the webhook will be blocked. Go to https://dev.netatmo.com, open your app and check the webhooks section for ban status. If it's banned you can then unban it manually. 














