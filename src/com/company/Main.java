package com.company;
import de.sciss.net.*;

import java.io.IOException;
import java.net.SocketAddress;

public class Main {
    private static boolean pause = false; // (must be an instance or static field to be useable
    // from an anonymous inner class)
    public static void main(String[] args) {
	// write your code here


        final Object    sync = new Object();
        final OSCServer c;
        try {
            // create TCP server on loopback port 0x5454
            c = OSCServer.newUsing( OSCServer.UDP, 8000, true );
        }
        catch( IOException e1 ) {
            e1.printStackTrace();
            return;
        }

        // now add a listener for incoming messages from
        // any of the active connections
        c.addOSCListener( new OSCListener() {
            public void messageReceived(OSCMessage m, SocketAddress addr, long time )
            {
                // first of all, send a reply message (just a demo)
                try {
                    c.send( new OSCMessage( "/done", new Object[] { m.getName() }), addr );
                }
                catch( IOException e1 ) {
                    e1.printStackTrace();
                }
                if( m.getName().equals( "/test" )) {
                    for (int i = 0; i < m.getArgCount(); i++)
                        System.out.println(m.getArg(i));
//                    System.out.println(m.;
                    System.out.println("YASSSS");
                }

                if( m.getName().equals( "/pause" )) {
                    // tell the main thread to pause the server,
                    // wake up the main thread
                    pause = true;
                    synchronized( sync ) {
                        sync.notifyAll();
                    }
                } else if( m.getName().equals( "/quit" )) {
                    // wake up the main thread
                    synchronized( sync ) {
                        sync.notifyAll();
                    }
                } else if( m.getName().equals( "/dumpOSC" )) {
                    // change dumping behaviour
                    c.dumpOSC( ((Number) m.getArg( 0 )).intValue(), System.err );
                }
            }
        });
        try {
            do {
                if( pause ) {
                    System.out.println( "  waiting four seconds..." );
                    try {
                        Thread.sleep( 4000 );
                    }
                    catch( InterruptedException e1 ) {}
                    pause = false;
                }
                System.out.println( "  start()" );
                // start the server (make it attentive for incoming connection requests)
                c.start();
                try {
                    synchronized( sync ) {
                        sync.wait();
                    }
                }
                catch( InterruptedException e1 ) {}

                System.out.println( "  stop()" );
                c.stop();
            } while( pause );
        }
        catch( IOException e1 ) {
            e1.printStackTrace();
        }

        // kill the server, free its resources
        c.dispose();
    }

}
