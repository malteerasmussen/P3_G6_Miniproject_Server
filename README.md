# pcss

#OSC MESSAGES
/hello -  hello initiates communication and server saves clients adress
* sends /helloBack as response




#CLIENT

Player%/
* children are GUI & Sound

Player%/GUI
* setPlayerID/%int%
* spot/%/taken or release
* avatar/%id%

Player%/Sound/%Instrument%
* /%sampleName%/noteOn or noteOff


#HOW TO SET UP

* DELETE EXISTING INTELLIJ PROJECT SERVER FOLDER
* NEW FROM GIT 
* GO TO PROJECT STRUCTURE, MARK "SRC" AS "SOURCES", AND UNDER THE PANE 'DEPENDENCIES', ADD THE NETUTIL.JAR
* UNDER   PROJECT STRUCTURE, under project on the left, set default language bla bal to SDK DEFAULT