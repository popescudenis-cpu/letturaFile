package com.example;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class GestoreClient implements Runnable {
    private final Socket socket;
    private final File cartella = new File("documenti");

    public GestoreClient(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        boolean statoInviato = false;

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
            if (parti.length != 2 || !parti[0].equals("LEGGI")) {
                out.println("ERR SYNTAX");
                out.println();
                return;
            }

            String nomeFile = parti[1];

            if (!nomeFile.endsWith(".txt") || 
                nomeFile.contains("/") || 
                nomeFile.contains("\\") || 
                nomeFile.contains("..")) {
                
                out.println("ERR INVALID"); 
                out.println();
                return;
            }

            File file = new File(cartella, nomeFile);
            if (!file.isFile()) {
                out.println("ERR NOTFOUND"); 
                out.println();
                return;
            }

            try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                out.println("OK FILE");
                out.println();
                statoInviato = true;

                String rigaFile;
                while ((rigaFile = fileReader.readLine()) != null) {
                    out.println(rigaFile);
                }
            } catch (IOException e) {
                System.err.println("Errore durante la lettura del file " + nomeFile + ": " + e.getMessage());
                if (!statoInviato) {
                    out.println("ERR READ"); 
                    out.println();
                }
            }

        } catch (IOException e) {
            System.err.println("Errore di comunicazione con il client: " + e.getMessage());
        }
    }
}
