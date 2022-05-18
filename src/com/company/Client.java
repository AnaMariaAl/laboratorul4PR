package com.company;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable {

    public static void main(String[] args) {
        Client client = new Client();
        client.run();

    }

    private Socket client;
    private BufferedReader input;
    private PrintWriter output;
    private boolean done;

    @Override
    public void run() {
        try {
            client = new Socket("127.0.0.1", 8000);
            output = new PrintWriter(client.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandlerClient inputHandlerClient = new InputHandlerClient();
            Thread t = new Thread(inputHandlerClient);
            t.start();

            String inMessage;

            while ((inMessage = input.readLine()) != null) {
                System.out.println(inMessage);
            }

        } catch (IOException e) {
            System.out.println(e);
            shutdown();
        }
    }

    public void shutdown() {
        done = true;
        try {
            input.close();
            output.close();
            if (!client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public class InputHandlerClient implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (!done) {
                    String message = reader.readLine();
                    if (message.equals("/exit")) {
                        output.println(message);
                        reader.close();
                        shutdown();
                    } else{
                        output.println(message);
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
                shutdown();
            }
        }
    }

}
