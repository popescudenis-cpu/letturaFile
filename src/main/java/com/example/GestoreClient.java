package com.example;

import java.io.*;
import java.net.*;

public class GestoreClient implements Runnable {
    private Socket socket;
    private File cartella = new File("documenti");

    public GestoreClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)) {

            String richiesta = in.readLine();
            
            if (richiesta == null) {
                return; 
            }

            if (richiesta.trim().isEmpty()) {
                out.println("ERR SYNTAX");
                out.println();
                return;
            }

            String[] parti = richiesta.trim().split("\\s+");
            
            if (parti.length < 2) {
                out.println("ERR SYNTAX");
                out.println();
                return;
            }

            String comando = parti[0];
            String nomeFile = parti[1]; 

            if (!comando.equals("LEGGI")) {
                out.println("ERR SYNTAX");
                out.println();
                return;
            }

            if (!nomeFile.endsWith(".txt") || 
                nomeFile.contains("/") || 
                nomeFile.contains("\\") || 
                nomeFile.contains("..")) {
                
                out.println("ERRORE: Nome file non valido");
                out.println();
                return;
            }

            File file = new File(cartella, nomeFile);
            
            boolean presente = file.isFile();

            if (presente) {
                out.println("OK FILE");
                out.println(); 

                try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                    String rigaFile;
                    while ((rigaFile = fileReader.readLine()) != null) {
                        out.println(rigaFile);
                    }
                }
            } else {
                out.println("ERRORE: File non trovato");
                out.println();
            }

        } catch (IOException e) {
            System.err.println("Errore nel thread " + Thread.currentThread().getName() + ": " + e.getMessage());
        } 
    }
}

