# Megahay - Server

Have you ever dreamt about being a rock star? Now's your chance! 
Join the infamous rock band Megahay on their World Tour. 
Choose your favourite instrument and jam with all your friends in front of a live audience.

## Releases 
[Release version 1.0](somelink)

## Dependencies

### Java 11
The project is built in Java 11

### [NetUtil](https://github.com/Sciss/NetUtil)
An [OpenSoundControl](http://opensoundcontrol.org/introduction-osc) (OSC) communication library for Java. Documentation for it can be found [here](https://www.sciss.de/netutil/doc/api/index.html). The JAR-file is in the intellij project


## Communication

This repository is the server side of the application. The client side can be found [here](https://github.com/lmadza18/P3_G6_Miniproject_Client).
Running the server is needed in order for multiple clients to play together. The server runs the NetUtil() . 
We use a UDP server, which runs on port 8000. We create an OSCServer for which we add an OSCListener.
The OSCListener is used for receiving messages from clients and distributing them to other clients.


The server connect all client through a given IP-address. 
The IP-address needed for the clients to connect, is the local IP-address of the device hosting the server.


Messages for Sound and GUI are just distributed to all other clients. 
Furthermore, there are two more messages from the client that the server handles:

* Establish communication ("/hello")
* Receiving status ("status")

Establish communication initiates communication and server saves clients address in a list of active clients.
The server then sends a response and notifies if other clients are already active and playing.

When receiving status from a client, a timer for that client is reset. If no status message has been received
in 4 seconds the client is declared as inactive and is removed.


## How to set up
(Using IntelliJ)
* Go to project structure, in "Project". Set Project SDK to Java 11 (11.0.3). 
* In "Project" set project language level to SDK Default.
* Copy the project path and set it in Project compiler output field and add "\out" at the end.
* In Project structure, in "Modules" under dependencies, press the add button (+). 
Choose 1 JARs or directories and choose the jar file name "netutil-1.1.0.jar"
* In "Modules", under Sources, choose the src folder and mark as Sources. 
* Press OK. 

## Authors

* **Casper Skaarup Ovesen**
* **Kristinn Bragi Garðarsson**
* **Malte Elkær Rasmussen** 
* **Mikkel Kappel Persson**
* **Niels Erik Raursø**
* **Tor Arnth Petersen**

See also the list of [contributors](https://github.com/malteerasmussen/P3_G6_Miniproject_Server/contributors) 
who participated in this project.


## Acknowledgments

* Thanks to [Molly and Joy-Joy](https://www.goatslive.com/)
