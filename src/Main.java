
import de.sciss.net.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static boolean pause = false; // (must be an instance or static field to be usable


    // from an anonymous inner class)


    public static void main(String[] args) {
        SocketAddress[] stageSpotTaken = new SocketAddress[4]; // Stores socket address for a spot taken
        int[] instrumentId = {-1, -1, -1, -1}; // Stores which instruments are chosen on a certain stageSpot

        List<SocketAddress> clientList = new ArrayList<SocketAddress>();
        List<Long> timerList = new ArrayList<Long>();

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

                // Display when receiving a message from a client - except status messages
                if (!m.getName().contains("status")) {
                    System.out.println("MESSAGE RECEIVED " + m.getName() + " FROM: " + addr + ", TIME: " + time + ", ARGS: " + m.getArgCount());
                }

                // receives messages containing an object and distributes it
                if (m.getArgCount() > 1) {
                    distMessages(m, addr, (int) m.getArg(0), (int) m.getArg(1), (String) m.getArg(2));
                    if (m.getArgCount() > 2 && m.getArg(2).equals("take")) {
                        stageSpotTaken[(int) m.getArg(0)] = addr;
                        instrumentId[(int) m.getArg(0)] = (int) m.getArg(1);
                    }
                    if (m.getArgCount() > 2 && m.getArg(2).equals("leave")) {
                        stageSpotTaken[(int) m.getArg(0)] = null;
                        instrumentId[(int) m.getArg(0)] = -1;
                    }
                    if (m.getArgCount() > 2 && m.getArg(2).equals("reserve")) {
                        stageSpotTaken[(int) m.getArg(0)] = addr;
                        instrumentId[(int) m.getArg(0)] = -1;
                    }
                    if (m.getArgCount() > 2 && m.getArg(2).equals("release")) {
                        stageSpotTaken[(int) m.getArg(0)] = null;
                        instrumentId[(int) m.getArg(0)] = -1;
                    }
                }
                if (m.getName().contains("status")) {
                    for (int i = 0; i < clientList.size(); i++) {
                        if (clientList.get(i).equals(addr)) {
                            timerList.set(i, System.currentTimeMillis());
                        }
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
                        clientList.add(addr);
                        timerList.add(System.currentTimeMillis());
                        System.out.println("New player connected, now there's " + clientList.size());
                        try {
                            System.out.println("NANOTIME: " + timeElapsed);
                            c.send(new OSCMessage("/server/setPlayerId", new Object[]{clientList.size(), timeElapsed}), addr);
                            for (int i = 0; i < instrumentId.length; i++) {
                                if (stageSpotTaken[i] != null && instrumentId[i] != -1) {
                                    c.send(new OSCMessage("/GUImessage", new Object[]{i, instrumentId[i], "take"}), addr);
                                }
                                if (stageSpotTaken[i] != null && instrumentId[i] == -1) {
                                    c.send(new OSCMessage("/GUImessage", new Object[]{i, instrumentId[i], "reserve"}), addr);
                                }
                            }
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else
                        System.out.println("ADDRESS ALREADY IN LIST");
                }
            }
        });

        // *****************************************************************************************************

        // Thread for checking status of clients
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    for (int i = 0; i < clientList.size(); i++) {
                        // check if clients have not send status for 4 seconds
                        if ((System.currentTimeMillis() - timerList.get(i)) / 1000 > 4) {
                            System.out.println("Client " + clientList.get(i) + " is inactive");
                            for (int j = 0; j < stageSpotTaken.length; j++) {
                                // check if the client who is inactive has occupied a stageSpot
                                if (stageSpotTaken[j] == clientList.get(i)) {
                                    for (SocketAddress socketAddress : clientList) {
                                        // release the spot for all clients
                                        try {
                                            c.send(new OSCMessage("/server/GUImessage", new Object[]{j, instrumentId[j], "leave"}), socketAddress);
                                        } catch (IOException e1) {
                                            e1.printStackTrace();
                                        }
                                    }
                                    stageSpotTaken[j] = null;
                                    instrumentId[j] = -1;
                                }
                            }
                            timerList.remove(i);
                            clientList.remove(i);
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


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
