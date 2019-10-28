
import de.sciss.net.*;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static boolean pause = false; // (must be an instance or static field to be useable

    // from an anonymous inner class)


    public static void main(String[] args) {
        // write your code here
        long timeElapsed = System.nanoTime();

        final Object sync = new Object();
        final OSCServer c;
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


            void distMessages(OSCMessage m, SocketAddress from, int spot, int instrument, String operation) {
                for (SocketAddress socketAddress : clientList) {
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

            public void messageReceived(OSCMessage m, SocketAddress addr, long time) {
                System.out.println("MESSAGE RECEIVED " + m.getName() + " FROM: " + addr + ", TIME: " + time + ", ARGS: " + m.getArgCount());

                if (m.getArgCount() > 1) {
                    System.out.println("GUImessage received with " + m.getArgCount() + " arguments");
                    int spot = (int) m.getArg(0);
                    int instrument = (int) m.getArg(1);
                    String operation = (String) m.getArg(2);
                    distMessages(m, addr, spot, instrument, operation);
                }

//                ******************************************************************************************************
                // /hello initiates communication and server saves clients in clientList
                if (m.getName().equals("/hello")) {
                    System.out.println("/hello from " + addr);
                    Boolean alreadyExists = false;
                    for (SocketAddress socketAddress : clientList) {
                        if (socketAddress.equals(addr)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        this.clientList.add(addr);
                        System.out.println("New player connected, now there's " + clientList.size());
                        try {
                            System.out.println("NANOTIME: " + timeElapsed);
                            c.send(new OSCMessage("/server/setPlayerId", new Object[]{clientList.size(), timeElapsed}), addr);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else
                        System.out.println("ADRESS ALREADY IN LIST");
                }
//                ******************************************************************************************************


                if (m.getName().equals("/test")) {
                    for (int i = 0; i < m.getArgCount(); i++)
                        System.out.println(m.getArg(i));
//                    System.out.println(m.;
                    System.out.println("YASSSS");
                }

                if (m.getName().equals("/pause")) {
                    // tell the main thread to pause the server,
                    // wake up the main thread
                    pause = true;
                    synchronized (sync) {
                        sync.notifyAll();
                    }
                } else if (m.getName().equals("/quit")) {
                    // wake up the main thread
                    synchronized (sync) {
                        sync.notifyAll();
                    }
                } else if (m.getName().equals("/dumpOSC")) {
                    // change dumping behaviour
                    c.dumpOSC(((Number) m.getArg(0)).intValue(), System.err);
                }
            }
        });
        try {
            do {
                if (pause) {
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
                c.stop();
            } while (pause);
        } catch (
                IOException e1) {
            e1.printStackTrace();
        }
        // kill the server, free its resources
        c.dispose();
    }

}
