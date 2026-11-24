package com.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class GestoreClient implements Runnable {
    private final Socket socket;
    private final File cartella = new File("documenti");
    private static final String[][] TIPI = {
        {"txt", "text/plain; charset=utf-8"},
        {"md",  "text/markdown; charset=utf-8"},
        {"csv", "text/csv; charset=utf-8"}
    };

    public GestoreClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket clientSocket = this.socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String richiesta = in.readLine();
            if (richiesta == null) {
                return;
            }

            System.out.println("Richiesta ricevuta: " + richiesta);

            if (richiesta.trim().isEmpty()) {
                out.println("ERR SYNTAX");
                out.println();
                return;
            }

            String[] parti = richiesta.trim().split("\\s+");

            if (!parti[0].equals("LEGGI")) {
                out.println("ERR UNKNOWNCMD");
                out.println();
                return;
            }

            if (parti.length != 2) {
                out.println("ERR SYNTAX");
                out.println();
                return;
            }

            String nomeBase = parti[1];

            if (nomeBase.contains(".") || nomeBase.contains("/") || nomeBase.contains("\\")) {
                out.println("ERR SYNTAX");
                out.println();
                return;
            }

            File fileTrovato = null;
            String contentTypeTrovato = null;

            for (String[] riga : TIPI) {
                String est = riga[0];
                String mime = riga[1];
                File candidato = new File(cartella, nomeBase + "." + est);
                if (candidato.isFile()) {
                    fileTrovato = candidato;
                    contentTypeTrovato = mime;
                    break;
                }
            }

            if (fileTrovato == null) {
                out.println("ERR NOTFOUND");
                out.println();
                return;
            }

            try {
                byte[] dati = Files.readAllBytes(fileTrovato.toPath());
                String contenuto = new String(dati, StandardCharsets.UTF_8);

                out.println("OK FILE");
                out.println("Content-Type: " + contentTypeTrovato);
                out.println("Content-Length: " + dati.length);
                out.println();
                out.print(contenuto);
                out.flush();

            } catch (IOException e) {
                System.err.println("Errore durante la lettura del file " + fileTrovato.getName() + ": " + e.getMessage());
                out.println("ERR READ");
                out.println();
            }

        } catch (IOException e) {
            System.err.println("Errore di comunicazione con il client: " + e.getMessage());
        }
    }
}
