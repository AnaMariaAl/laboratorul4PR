package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }

    private ArrayList<Connection> connections;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }
    @Override
    public void run() {
        try {
            server = new ServerSocket(8000);
            pool = Executors.newCachedThreadPool();

            while (!done) {
                Socket client = server.accept();
                Connection handler = new Connection(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }
    public void broadcast(Connection sender, String message) {
        for (Connection text : connections) {
            if (text != null && !text.equals(sender)) {
                text.messageSender(message);
            }
        }
    }
    public void shutdown() {
        try {
            done = true;
            pool.shutdown();
            if (!server.isClosed()) {
                server.close();
            }
            for (Connection ch : connections) {
                ch.shutDownServer();
            }
        } catch (IOException e) {
            System.out.println(e);
        }

    }
    public class Connection implements Runnable {

        private Socket client;
        private BufferedReader input;
        private PrintWriter output;
        private String name;

        public Connection(Socket client) {
            this.client = client;

        }
        @Override
        public void run() {
            try {
                output = new PrintWriter(client.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                output.println("Write your nickname: ");
                name = input.readLine();
                System.out.println(name + "Connected successfully!");
                broadcast(this, name + " Added to chat ");

                String message;
                while ((message = input.readLine()) != null) {
                    if (message.startsWith("/name ")) {
                        String[] messageSplit = message.split(" ", 2);

                        if (messageSplit.length == 2) {
                            broadcast(this, name + "changed its name: " + messageSplit[1]);
                            System.out.println(name + "changed it's name: " + messageSplit[1]);
                            name = messageSplit[1];
                            output.println("Nickname changed successfully!");
                        } else {
                            output.println("No name provided");

                        }

                    } else if (message.startsWith("/exit")) {
                        broadcast(this, name + " left chat!");
                        shutdown();

                    } else {
                        broadcast(this, name + ": " + message);
                    }
                }
            } catch (IOException e) {
                System.out.println(e.fillInStackTrace());
                e.printStackTrace();
            }
        }

        public void messageSender(String message) {
            output.println(message);
        }

        public void shutDownServer() {
            try {

                input.close();
                output.close();
                if (!client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

}