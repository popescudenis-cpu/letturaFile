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
                inviaRisposta(out, 400, "Bad Request", "text/plain; charset=utf-8", "Richiesta vuota.\n");
                return;
            }

            String[] parti = richiesta.trim().split("\\s+");

            if (!parti[0].equals("LEGGI")) {
                inviaRisposta(out, 501, "Not Implemented", "text/plain; charset=utf-8", "Comando \"" + parti[0] + "\" non supportato.\n");
                return;
            }

            if (parti.length != 2) {
                inviaRisposta(out, 400, "Bad Request", "text/plain; charset=utf-8", "Sintassi non valida. Uso: LEGGI nomefile\n");
                return;
            }

            String nomeBase = parti[1];

            if (nomeBase.contains(".") || nomeBase.contains("/") || nomeBase.contains("\\")) {
                inviaRisposta(out, 400, "Bad Request", "text/plain; charset=utf-8", "Nome file non valido.\n");
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
                inviaRisposta(out, 404, "Not Found", "text/plain; charset=utf-8", "Il file \"" + nomeBase + "\" non esiste.\n");
                return;
            }

            try {
                byte[] dati = Files.readAllBytes(fileTrovato.toPath());
                String contenuto = new String(dati, StandardCharsets.UTF_8);

                inviaRisposta(out, 200, "OK", contentTypeTrovato, contenuto);

            } catch (IOException e) {
                System.err.println("Errore durante la lettura del file " + fileTrovato.getName() + ": " + e.getMessage());
                inviaRisposta(out, 500, "Internal Server Error", "text/plain; charset=utf-8", "Errore interno durante la lettura del file.\n");
            }

        } catch (IOException e) {
            System.err.println("Errore di comunicazione con il client: " + e.getMessage());
        }
    }

    private void inviaRisposta(PrintWriter out, int codice, String frase, String contentType, String contenuto) {
        byte[] byteContenuto = contenuto.getBytes(StandardCharsets.UTF_8);

        out.println(codice + " " + frase);
        out.println("Content-Type: " + contentType);
        out.println("Content-Length: " + byteContenuto.length);
        out.println();
        out.print(contenuto);
        out.flush();
    }
}
