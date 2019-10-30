
import de.sciss.net.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static boolean pause = false; // (must be an instance or static field to be useable




    // from an anonymous inner class)


    public static void main(String[] args) {
        boolean[] stageSpotTaken = new boolean[4]; // Stores if a stagespots are taken
        int[] instrumentId = { // Stores which instruments are chosen on a certain stagespot
                -1,
                -1,
                -1,
                -1
        };

        // write your code here
        long timeElapsed = System.nanoTime();

        final Object sync = new Object();
        final OSCServer c; // This is the server
        try {
            // create TCP server on loopback port 0x5454
            c = OSCServer.newUsing(OSCServer.UDP, 8000);
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }

        // now add a listener for incoming messages from
        // any of the active connections
        c.addOSCListener(new OSCListener() {
            List<SocketAddress> clientList = new ArrayList<SocketAddress>();

            // Distribute messages to all clients except the client it received the message from
            void distMessages(OSCMessage m, SocketAddress from, int spot, int instrument, String operation) {
                for (SocketAddress socketAddress : clientList) { // Checks all clients in the clientList
                    System.out.println("socketAddress: " + socketAddress);
                    //    send to everybody but the recipient
                    if (!socketAddress.equals(from)) {
                        try {
                            System.out.println("DISTRIBUTING MESSAGE: " + m.getName() + " TO: " + socketAddress);
                            c.send(new OSCMessage("/server" + m.getName(), new Object[]{spot, instrument, operation}), socketAddress);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
            // Determent how to handle the messages
            public void messageReceived(OSCMessage m, SocketAddress addr, long time) {

                System.out.println("MESSAGE RECEIVED " + m.getName() + " FROM: " + addr + ", TIME: " + time + ", ARGS: " + m.getArgCount());

                // receives messages containing an object and distributes it
                if (m.getArgCount() > 1) {
                    distMessages(m, addr, (int) m.getArg(0), (int) m.getArg(1), (String) m.getArg(2));
                    if (m.getArgCount() > 2 && m.getArg(2).equals("take")){
                        stageSpotTaken[(int) m.getArg(0)] = true;
                        instrumentId[(int) m.getArg(0)] = (int) m.getArg(1);
                    }
                    if (m.getArgCount() > 2 && m.getArg(2).equals("leave")){
                        stageSpotTaken[(int) m.getArg(0)] = false;
                        instrumentId[(int) m.getArg(0)] = -1;
                    }
                    if (m.getArgCount() > 2 && m.getArg(2).equals("reserve")){
                        stageSpotTaken[(int) m.getArg(0)] = true;
                        instrumentId[(int) m.getArg(0)] = -1;
                    }
                    if (m.getArgCount() > 2 && m.getArg(2).equals("release")){
                        stageSpotTaken[(int) m.getArg(0)] = false;
                        instrumentId[(int) m.getArg(0)] = -1;
                    }
                }

//                ******************************************************************************************************
                // /hello initiates communication and server saves clients in clientList
                if (m.getName().equals("/hello")) {
                    System.out.println("/hello from " + addr);
                    boolean alreadyExists = false;
                    for (SocketAddress socketAddress : clientList) { //Checks if the client is already in the clientList
                        if (socketAddress.equals(addr)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) { // Send messages to clients 
                        this.clientList.add(addr);
                        System.out.println("New player connected, now there's " + clientList.size());
                        try {
                            System.out.println("NANOTIME: " + timeElapsed);
                            c.send(new OSCMessage("/server/setPlayerId", new Object[]{clientList.size(), timeElapsed}), addr);
                            for (int i = 0; i < instrumentId.length; i++){
                                if (stageSpotTaken[i] && instrumentId[i] != -1) {
                                    c.send(new OSCMessage("/GUImessage", new Object[]{i, instrumentId[i], "take"}), addr);
                                }
                                if (stageSpotTaken[i] && instrumentId[i] == -1) {
                                    c.send(new OSCMessage("/GUImessage", new Object[]{i, instrumentId[i], "reserve"}), addr);
                                }
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else
                        System.out.println("ADRESS ALREADY IN LIST");
                }
            }
        });

        // *****************************************************************************************************

        try {
            do {
                if (pause) { // This must be included for the server to work (we don't use it, tho)
                    System.out.println("  waiting four seconds...");
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e1) {
                    }
                    pause = false;
                }
                System.out.println("  start()");
                // start the server (make it attentive for incoming connection requests)
                c.start();
                try {
                    synchronized (sync) {
                        sync.wait();
                    }
                } catch (InterruptedException e1) {
                }

                System.out.println("  stop()");
                c.stop(); // Stops the server
            } while (pause);
        } catch (
                IOException e1) {
            e1.printStackTrace();
        }
        // kill the server, free its resources
        c.dispose();


    }

}
