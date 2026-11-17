package com.example;

import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        int porta = 3000;

        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("Server in ascolto sulla porta " + porta + "...");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nuovo client connesso da: " + socket.getRemoteSocketAddress());

                GestoreClient gestore = new GestoreClient(socket);
                Thread nuovoThread = new Thread(gestore);
                nuovoThread.start(); 
            }
        } catch (IOException e) {
            System.err.println("Errore nell'avvio del server sulla porta " + porta + ": " + e.getMessage());
        }
    }
}

