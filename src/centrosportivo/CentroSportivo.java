package centrosportivo;

import prog.io.ConsoleInputManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class CentroSportivo {
    static String nomeFileStato = "centrosportivo.bin";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ArrayList<Tesserato> tesserati;
        ArrayList<CampoDaGioco> campi;
        ArrayList<Prenotazione> prenotazioni;
        try {
            FileInputStream fis = new FileInputStream(nomeFileStato);
            ObjectInputStream ois = new ObjectInputStream(fis);
            System.out.println("Carico file di stato: " + nomeFileStato);
            tesserati = (ArrayList<Tesserato>) ois.readObject();
            campi = (ArrayList<CampoDaGioco>) ois.readObject();
            prenotazioni = (ArrayList<Prenotazione>) ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException f) {
            System.out.println("File non trovato, passo all'inserimento dei dati\n\n");
            tesserati = inserisciTesserati();
            campi = inserisciCampi();
            prenotazioni = inserisciPrenotazioni();
        }
    }

    public static ArrayList<Tesserato> inserisciTesserati() {
        ConsoleInputManager in = new ConsoleInputManager();
        ArrayList<Tesserato> tesserati = new ArrayList<>();
        boolean continua;
        int num;
        String nome, cognome;
        do {
            nome = in.readLine("Inserisci nome: ");
            cognome = in.readLine("Inserisci cognome: ");
            Tesserato t = new Tesserato(nome, cognome);
            tesserati.add(t);
            continua = !in.readSiNo("Finito? [s] [n]: ");
        } while (continua);
        return tesserati;
    }

    public static ArrayList<CampoDaGioco> inserisciCampi() {
        ConsoleInputManager in = new ConsoleInputManager();
        ArrayList<CampoDaGioco> campi = new ArrayList<>();
        boolean continua;
        TipoCampo tipo;
        do {
            tipo = TipoCampo.valueOf(in.readLine("Inserisci il tipo del campo: ")); //provvisorio
            CampoDaGioco c = new CampoDaGioco(tipo);
            campi.add(c);
            continua = !in.readSiNo("Finito? [s] [n]: ");
        } while (continua);
        return campi;
    }

    public static ArrayList<Prenotazione> inserisciPrenotazioni() {
        return null;
    }
}
