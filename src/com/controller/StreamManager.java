package com.controller;

import java.io.*;

public class StreamManager {

    private static StreamManager streamManager = new StreamManager();

    // For sending raw binary data
    private static OutputStream outputStream;
    private static InputStream inputStream;

    // For strings - remove later
    private static DataOutputStream dataOutputStream;
    private static DataInputStream dataInputStream;

    /*
     * A private Constructor prevents any other
     * class from instantiating.
     */

    private StreamManager(){ }

    public static StreamManager getInstance( ) {
        return streamManager;
    }

    public void initializeStreams(InputStream is, OutputStream os) {
        inputStream = is;
        outputStream = os;
        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);
    }

    public void sendCommand(String command) throws IOException {
        dataOutputStream.writeBytes(command + "\n");
        dataOutputStream.flush();
        System.out.println("Sending command " + command);
    }
}
