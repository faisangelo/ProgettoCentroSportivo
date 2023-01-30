package centrosportivo;

import prog.io.ConsoleInputManager;
import prog.io.ConsoleOutputManager;
import prog.utili.Data;
import prog.utili.Orario;
import prog.utili.SintassiDataScorretta;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class CentroSportivo {
    static final String nomeFileStato = "centrosportivo.bin";
    static final int righePagina = 10;
    static ConsoleInputManager in;
    static ConsoleOutputManager out;
    static Tesserato.Ordinamento ordinamentoTesserato = Tesserato.Ordinamento.COGNOME;
    static CampoDaGioco.Ordinamento ordinamentoCampo = CampoDaGioco.Ordinamento.CODCAMPO;
    static Prenotazione.Ordinamento ordinamentoPrenotazione = Prenotazione.Ordinamento.DATA;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        in = new ConsoleInputManager();
        out = new ConsoleOutputManager();
        ArrayList<Tesserato> tesserati;
        ArrayList<CampoDaGioco> campi;
        ArrayList<Prenotazione> prenotazioni;
        Data.setSeparatore('/');
        out.println("Carico file di stato: " + nomeFileStato);
        try {
            FileInputStream fis = new FileInputStream(nomeFileStato);
            ObjectInputStream ois = new ObjectInputStream(fis);
            tesserati = (ArrayList<Tesserato>) ois.readObject();
            campi = (ArrayList<CampoDaGioco>) ois.readObject();
            prenotazioni = (ArrayList<Prenotazione>) ois.readObject();
            Tesserato.setProxNumTessera((Integer) ois.readObject());
            CampoDaGioco.setProxCodCampo((Integer) ois.readObject());
            ordinamentoTesserato = (Tesserato.Ordinamento) ois.readObject();
            ordinamentoCampo = (CampoDaGioco.Ordinamento) ois.readObject();
            ordinamentoPrenotazione = (Prenotazione.Ordinamento) ois.readObject();
            ois.close();
            fis.close();
            if (!prenotazioni.isEmpty()) {
                Data data = new Data();
                Orario ora = new Orario();
                prenotazioni.removeIf(p -> p.getData().isMinore(data) ||
                        (p.getData().equals(data) && p.getOraInizio() < ora.getOre()));  //cancella le prenotazioni passate
            }
        } catch (FileNotFoundException f) {
            in.readLine("File non trovato!\n[INVIO] per passare all'inserimento dei dati");
            tesserati = new ArrayList<>();
            campi = new ArrayList<>();
            prenotazioni = new ArrayList<>();
            inserisciTesserati(tesserati, true);
            ordinaTesserati(tesserati);
            out.println();
            inserisciCampi(campi, true);
        }
        in.readLine("[INVIO] per accedere al menu");
        int scelta;
        do {
            out.println("\n\t\t\t\t\t*** CENTRO SPORTIVO ***\n");
            out.print("1) Scorri elenco tesserati          ");
            out.println("8) Cancella una prenotazione");
            out.print("2) Scorri elenco campi da gioco     ");
            out.println("9) Modifica una prenotazione");
            out.print("3) Scorri elenco prenotazioni       ");
            out.println("10) Vedi primo slot libero per un campo");
            out.print("4) Ordina i tesserati               ");
            out.println("11) Vedi campi disponibili in uno slot scelto");
            out.print("5) Ordina i campi da gioco          ");
            out.println("12) Modifica elenco tesserati");
            out.print("6) Ordina le prenotazioni           ");
            out.println("13) Modifica elenco campi");
            out.print("7) Prenota un campo da gioco        ");
            out.println("14) Salva lo stato nel file");
            out.println("\n[0] per uscire (salvataggio automatico)");
            boolean flag = false;
            do {
                scelta = in.readInt("\nScegli [0 - 14]: ");
                if (0 <= scelta && scelta <= 14) {
                    out.println();
                    flag = true;
                    switch (scelta) {
                        case 1 -> stampaTesserati(tesserati);
                        case 2 -> stampaCampi(campi);
                        case 3 -> stampaPrenotazioni(prenotazioni);
                        case 4 -> stampaTesseratiOrdinati(tesserati);
                        case 5 -> stampaCampiOrdinati(campi);
                        case 6 -> stampaPrenotazioniOrdinate(prenotazioni);
                        case 7 -> effettuaPrenotazione(tesserati, campi, prenotazioni);
                        case 8 -> cancellaPrenotazione(prenotazioni);
                        case 9 -> modificaPrenotazione(tesserati, campi, prenotazioni);
                        case 10 -> primoSlotLibero(campi, prenotazioni);
                        case 11 -> campiSlotScelto(campi, prenotazioni);
                        case 12 -> modificaElencoTesserati(tesserati, prenotazioni);
                        case 13 -> modificaElencoCampi(campi, prenotazioni);
                        case 14 -> salvaStato(tesserati, campi, prenotazioni, false);
                    }
                } else {
                    out.println("Inserimento non valido, riprova!");
                }
            } while (!flag);
        } while (scelta != 0);
        salvaStato(tesserati, campi, prenotazioni, true);
        in.close();
        out.close();
    }

    static void inserisciTesserati(ArrayList<Tesserato> tesserati, boolean fileNonTrovato) {
        boolean continua;
        String nome, cognome;
        if (fileNonTrovato) {
            out.println("\n*** INSERIMENTO TESSERATI ***");
        }
        do {
            nome = in.readLine("\nInserisci nome: ");
            cognome = in.readLine("Inserisci cognome: ");
            if (nome.isBlank() || cognome.isBlank()) {
                out.println("\nNome e/o cognome non validi, riprova!");
                continua = true;
            } else {
                Tesserato t = new Tesserato(nome, cognome);
                tesserati.add(t);
                out.println("\nTesserato inserito! Numero tessera: " + t.getNumTessera());
                continua = !in.readSiNo("Finito? [s] [n]: ");
            }
        } while (continua);
        ordinaTesserati(tesserati);
    }

    static void inserisciCampi(ArrayList<CampoDaGioco> campi, boolean fileNonTrovato) {
        boolean continua;
        TipoCampo[] tipi = TipoCampo.values();
        CampoDaGioco campo = null;
        if (fileNonTrovato) {
            out.println("*** INSERIMENTO CAMPI DA GIOCO ***");
        }
        do {
            out.println();
            int i = 1;
            for (TipoCampo t : tipi) {
                out.println(i + ") " + t.toString());
                i++;
            }
            boolean flag = false;
            do {
                int scelta = in.readInt("\nScegli [1 - 5]: ");
                try {
                    campo = new CampoDaGioco(tipi[scelta - 1]);
                    flag = true;
                } catch (ArrayIndexOutOfBoundsException a) {
                    out.println("Inserimento non valido, riprova");
                }
            } while (!flag);
            campi.add(campo);
            out.println("Campo da gioco inserito! Codice campo: " + campo.getCodCampo());
            continua = !in.readSiNo("Finito? [s] [n]: ");
        } while (continua);
        ordinaCampi(campi);
    }

    static void stampaTesserati(ArrayList<Tesserato> tesserati) {
        if (!tesserati.isEmpty()) {
            int pos = 0, i;
            char c;
            boolean fineElenco = false, flag;
            do {
                for (i = 0; i < righePagina && i + pos < tesserati.size(); i++) {
                    out.println(tesserati.get(pos + i).toString());
                }
                if (i + pos >= tesserati.size()) {
                    fineElenco = true;
                }
                do {
                    flag = false;
                    if (pos > 0 && !fineElenco) {
                        c = in.readChar("\n[+] per andare avanti, [-] per tornare indietro, [0] per uscire: ");
                    } else if (fineElenco && pos > 0) {
                        c = in.readChar("\n[-] per tornare indietro, [0] per uscire: ");
                    } else if (fineElenco && pos == 0) {
                        c = in.readChar("\n[0] per uscire: ");
                    } else {
                        c = in.readChar("\n[+] per andare avanti, [0] per uscire: ");
                    }
                    if (c == '+' && !fineElenco) {
                        pos += righePagina;
                        flag = true;
                    } else if (c == '-' && pos > 0) {
                        pos -= righePagina;
                        if (fineElenco) {
                            fineElenco = false;
                        }
                        flag = true;
                    } else if (c == '0') {
                        flag = true;
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } while (!flag);
                out.println();
            } while (c != '0');
        } else {
            out.println("Mi dispiace, non ci sono tesserati!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void stampaCampi(ArrayList<CampoDaGioco> campi) {
        if (!campi.isEmpty()) {
            int pos = 0, i;
            char c;
            boolean fineElenco = false, flag;
            do {
                for (i = 0; i < righePagina && i + pos < campi.size(); i++) {
                    out.println(campi.get(pos + i).toString());
                }
                if (i + pos >= campi.size()) {
                    fineElenco = true;
                }
                do {
                    flag = false;
                    if (pos > 0 && !fineElenco) {
                        c = in.readChar("\n[+] per andare avanti, [-] per tornare indietro, [0] per uscire: ");
                    } else if (fineElenco && pos > 0) {
                        c = in.readChar("\n[-] per tornare indietro, [0] per uscire: ");
                    } else if (fineElenco && pos == 0) {
                        c = in.readChar("\n[0] per uscire: ");
                    } else {
                        c = in.readChar("\n[+] per andare avanti, [0] per uscire: ");
                    }
                    if (c == '+' && !fineElenco) {
                        pos += righePagina;
                        flag = true;
                    } else if (c == '-' && pos > 0) {
                        pos -= righePagina;
                        if (fineElenco) {
                            fineElenco = false;
                        }
                        flag = true;
                    } else if (c == '0') {
                        flag = true;
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } while (!flag);
                out.println();
            } while (c != '0');
        } else {
            out.println("Mi dispiace, non ci sono campi!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void stampaPrenotazioni(ArrayList<Prenotazione> prenotazioni) {
        if (!prenotazioni.isEmpty()) {
            int pos = 0, i;
            char c;
            boolean fineElenco = false, flag;
            do {
                for (i = 0; i < righePagina && i + pos < prenotazioni.size(); i++) {
                    out.println(i + 1 + pos + ") " + prenotazioni.get(pos + i).toString());
                }
                if (i + pos >= prenotazioni.size()) {
                    fineElenco = true;
                }
                do {
                    flag = false;
                    if (pos > 0 && !fineElenco) {
                        c = in.readChar("\n[+] per andare avanti, [-] per tornare indietro, [0] per uscire: ");
                    } else if (fineElenco && pos > 0) {
                        c = in.readChar("\n[-] per tornare indietro, [0] per uscire: ");
                    } else if (fineElenco && pos == 0) {
                        c = in.readChar("\n[0] per uscire: ");
                    } else {
                        c = in.readChar("\n[+] per andare avanti, [0] per uscire: ");
                    }
                    if (c == '+' && !fineElenco) {
                        pos += righePagina;
                        flag = true;
                    } else if (c == '-' && pos > 0) {
                        pos -= righePagina;
                        if (fineElenco) {
                            fineElenco = false;
                        }
                        flag = true;
                    } else if (c == '0') {
                        flag = true;
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } while (!flag);
                out.println();
            } while (c != '0');
        } else {
            out.println("Mi dispiace, non ci sono prenotazioni!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void stampaTesseratiOrdinati(ArrayList<Tesserato> tesserati) {
        if (!tesserati.isEmpty()) {
            Tesserato.Ordinamento[] ordinamenti = Tesserato.Ordinamento.values();
            int i = 1;
            for (Tesserato.Ordinamento o : ordinamenti) {
                out.println(i + ") Ordinamento per " + o.toString());
                i++;
            }
            boolean flag = false;
            do {
                int scelta = in.readInt("\nScegli [1 - 3]: ");
                try {
                    ordinamentoTesserato = Tesserato.Ordinamento.values()[scelta - 1];
                    flag = true;
                } catch (ArrayIndexOutOfBoundsException a) {
                    out.println("Inserimento non valido, riprova");
                }
            } while (!flag);
            ordinaTesserati(tesserati);
            in.readLine("[INVIO] per visualizzare\n");
            stampaTesserati(tesserati);
        } else {
            out.println("Mi dispiace, non ci sono tesserati!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void ordinaTesserati(ArrayList<Tesserato> tesserati) {
        switch (ordinamentoTesserato) {
            case COGNOME -> Collections.sort(tesserati);
            case NOME -> Collections.sort(tesserati, new Comparator<Tesserato>() {
                @Override
                public int compare(Tesserato t1, Tesserato t2) {
                    return t1.getNome().compareTo(t2.getNome());
                }
            });
            case NUMEROTESSERA -> Collections.sort(tesserati, new Comparator<Tesserato>() {
                @Override
                public int compare(Tesserato t1, Tesserato t2) {
                    return Integer.compare(t1.getNumTessera(), t2.getNumTessera());
                }
            });
        }
    }

    static void stampaCampiOrdinati(ArrayList<CampoDaGioco> campi) {
        if (!campi.isEmpty()) {
            CampoDaGioco.Ordinamento[] ordinamenti = CampoDaGioco.Ordinamento.values();
            int i = 1;
            for (CampoDaGioco.Ordinamento o : ordinamenti) {
                out.println(i + ") Ordinamento per " + o.toString());
                i++;
            }
            boolean flag = false;
            do {
                int scelta = in.readInt("\nScegli [1 - 2]: ");
                try {
                    ordinamentoCampo = CampoDaGioco.Ordinamento.values()[scelta - 1];
                    flag = true;
                } catch (ArrayIndexOutOfBoundsException a) {
                    out.println("Inserimento non valido, riprova");
                }
            } while (!flag);
            ordinaCampi(campi);
            in.readLine("[INVIO] per visualizzare\n");
            stampaCampi(campi);
        } else {
            out.println("Mi dispiace, non ci sono campi!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void ordinaCampi(ArrayList<CampoDaGioco> campi) {
        switch (ordinamentoCampo) {
            case CODCAMPO -> Collections.sort(campi);
            case TIPOCAMPO -> Collections.sort(campi, new Comparator<CampoDaGioco>() {
                @Override
                public int compare(CampoDaGioco c1, CampoDaGioco c2) {
                    return c1.getTipo().toString().compareTo(c2.getTipo().toString());
                }
            });
        }
    }

    static void stampaPrenotazioniOrdinate(ArrayList<Prenotazione> prenotazioni) {
        if (!prenotazioni.isEmpty()) {
            Prenotazione.Ordinamento[] ordinamenti = Prenotazione.Ordinamento.values();
            int i = 1;
            for (Prenotazione.Ordinamento o : ordinamenti) {
                out.println(i + ") Ordinamento per " + o.toString());
                i++;
            }
            boolean flag = false;
            do {
                int scelta = in.readInt("\nScegli [1 - 3]: ");
                try {
                    ordinamentoPrenotazione = Prenotazione.Ordinamento.values()[scelta - 1];
                    flag = true;
                } catch (ArrayIndexOutOfBoundsException a) {
                    out.println("Inserimento non valido, riprova");
                }
            } while (!flag);
            ordinaPrenotazioni(prenotazioni);
            in.readLine("[INVIO] per visualizzare\n");
            stampaPrenotazioni(prenotazioni);
        } else {
            out.println("Mi dispiace, non ci sono prenotazioni!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void ordinaPrenotazioni(ArrayList<Prenotazione> prenotazioni) {
        switch (ordinamentoPrenotazione) {
            case DATA -> Collections.sort(prenotazioni);
            case GIOCATORE1 -> Collections.sort(prenotazioni, new Comparator<Prenotazione>() {
                @Override
                public int compare(Prenotazione p1, Prenotazione p2) {
                    return p1.getGiocatori().get(0).getCognome().compareTo(p2.getGiocatori().get(0).getCognome());
                }
            });
            case TIPOCAMPO -> Collections.sort(prenotazioni, new Comparator<Prenotazione>() {
                @Override
                public int compare(Prenotazione p1, Prenotazione p2) {
                    return p1.getCampo().getTipo().toString().compareTo(p2.getCampo().getTipo().toString());
                }
            });
        }
    }

    static void effettuaPrenotazione(ArrayList<Tesserato> tesserati, ArrayList<CampoDaGioco> campi,
                                     ArrayList<Prenotazione> prenotazioni) {
        ArrayList<CampoDaGioco> estratti;
        Data data = null;
        int oraInizio;
        Prenotazione prenotazione = null;
        TipoCampo[] tipi = TipoCampo.values();
        TipoCampo tipo = null;
        out.println("Che tipo di campo vuoi prenotare?\n");
        int i = 1;
        for (TipoCampo t : tipi) {
            out.println(i + ") " + t.toString());
            i++;
        }
        boolean flag = false;
        do {
            int scelta = in.readInt("\nScegli [1 - 5]: ");
            try {
                tipo = tipi[scelta - 1];
                flag = true;
            } catch (ArrayIndexOutOfBoundsException a) {
                out.println("Inserimento non valido, riprova");
            }
        } while (!flag);
        estratti = estraiCampi(campi, tipo);  //tutti i campi del tipo scelto
        if (!estratti.isEmpty() && tesserati.size() >= estratti.get(0).getMaxGiocatori()) {
            do {
                flag = false;
                Data oggi = new Data();
                Orario ora = new Orario();
                do {
                    try {
                        data = new Data(in.readLine("Inserisci la data: "));
                        flag = true;
                    } catch (SintassiDataScorretta s) {
                        out.println("Sintassi data scorretta, riprova!\n");
                    }
                } while (!flag);
                flag = false;
                oraInizio = in.readInt("Inserisci l'ora di inizio (ogni prenotazione vale un'ora soltanto): ");
                if ((data.isMaggiore(oggi) && oraInizio <= 21 && oraInizio >= 9) ||
                        (data.equals(oggi) && oraInizio <= 21 && oraInizio > ora.getOre())) {
                    Data fraDieciGiorni = new Data(oggi.getGiorno() + 10, oggi.getMese(), oggi.getAnno());
                    if (data.isMinore(fraDieciGiorni) || data.equals(fraDieciGiorni)) {
                        flag = true;
                    } else {
                        out.println("Puoi prenotare fino a 10 giorni prima, riprova\n");
                    }
                } else {
                    out.println("Inserimento non valido, riprova\n");
                }
            } while (!flag);
            boolean occupato = false;
            if (prenotazioni.isEmpty()) {
                prenotazione = new Prenotazione(estratti.get(0), data, oraInizio);
            } else {
                Data finalData = data;
                int finalOraInizio = oraInizio;
                ArrayList<Prenotazione> prenotazioniSlot = new ArrayList<>(prenotazioni);
                prenotazioniSlot.removeIf(p -> p.getCampo().getTipo() != estratti.get(0).getTipo() ||
                        (!p.getData().equals(finalData) || p.getOraInizio() != finalOraInizio));
                for (CampoDaGioco c : estratti) {
                    occupato = false;
                    prenotazione = new Prenotazione(c, data, oraInizio);
                    for (Prenotazione p : prenotazioniSlot) {
                        if (p.equals(prenotazione)) {
                            occupato = true;
                            break;
                        }
                    }
                    if (!occupato) {
                        break;
                    }
                }
            }
            if (!occupato) {
                HashMap<Integer, Tesserato> tesseratiMap = new HashMap<>();
                for (Tesserato tess : tesserati) {
                    tesseratiMap.put(tess.getNumTessera(), tess);
                }
                out.println("\nCampo libero, inserisci i numeri tessera dei giocatori!");
                for (int j = 1; j <= prenotazione.getCampo().getMaxGiocatori(); j++) {
                    boolean inserito = false;
                    do {
                        int scelta = in.readInt("\nGiocatore " + j + ": ");
                        if (tesseratiMap.containsKey(scelta)) {
                            inserito = prenotazione.addGiocatore(tesseratiMap.get(scelta));
                        }
                        if (!inserito) {
                            out.println("Giocatore già inserito o non esistente, riprova");
                        } else {
                            out.println(tesseratiMap.get(scelta).toString() + " inserito/a!");
                        }
                    } while (!inserito);
                }
                prenotazioni.add(prenotazione);
                ordinaPrenotazioni(prenotazioni);
                out.println("\nPrenotazione confermata! Visualizzo le prenotazioni");
                in.readLine("[INVIO] per visualizzare\n");
                stampaPrenotazioni(prenotazioni);
            } else {
                out.println("\nMi dispiace! Nessun campo libero in questo slot");
                in.readLine("[INVIO] per tornare al menu");
            }
        } else {
            out.println("\nMi dispiace! Il numero di tesserati è inferiore ai giocatori richiesti oppure non esistono " +
                    "campi del tipo selezionato");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static ArrayList<CampoDaGioco> estraiCampi(ArrayList<CampoDaGioco> campi, TipoCampo tipo) {
        ArrayList<CampoDaGioco> estratti = new ArrayList<>();
        for (CampoDaGioco c : campi) {
            if (c.getTipo() == tipo) {
                estratti.add(c);
            }
        }
        return estratti;
    }

    static void cancellaPrenotazione(ArrayList<Prenotazione> prenotazioni) {
        if (!prenotazioni.isEmpty()) {
            int pos = 0, i;
            char c;
            boolean fineElenco = false, flag;
            do {
                for (i = 0; i < righePagina && i + pos < prenotazioni.size(); i++) {
                    out.println(i + 1 + pos + ") " + prenotazioni.get(pos + i).toString());
                }
                if (i + pos >= prenotazioni.size()) {
                    fineElenco = true;
                }
                do {
                    flag = false;
                    if (pos > 0 && !fineElenco) {
                        c = in.readChar("\n[+] per andare avanti, [-] per tornare indietro, [0] per confermare pagina: ");
                    } else if (fineElenco && pos > 0) {
                        c = in.readChar("\n[-] per tornare indietro, [0] per confermare pagina: ");
                    } else if (fineElenco && pos == 0) {
                        c = in.readChar("\n[0] per confermare pagina: ");
                    } else {
                        c = in.readChar("\n[+] per andare avanti, [0] per confermare pagina: ");
                    }
                    if (c == '+' && !fineElenco) {
                        pos += righePagina;
                        flag = true;
                    } else if (c == '-' && pos > 0) {
                        pos -= righePagina;
                        if (fineElenco) {
                            fineElenco = false;
                        }
                        flag = true;
                    } else if (c == '0') {
                        flag = true;
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } while (!flag);
                out.println();
            } while (c != '0');
            flag = false;
            Data oggi = new Data();
            Orario ora = new Orario();
            do {
                int scelta = in.readInt("Inserisci il numero della prenotazione che vuoi cancellare: ");
                if (scelta > pos && scelta <= pos + i) {
                    if (!(prenotazioni.get(scelta - 1).getData().equals(oggi) &&
                            prenotazioni.get(scelta - 1).getOraInizio() - ora.getOre() < 5)) {
                        prenotazioni.remove(scelta - 1);
                        out.println("\nPrenotazione cancellata!");
                    } else {
                        out.println("\nPuoi cancellare una prenotazione fino a 4 ore prima!");
                    }
                    flag = true;
                } else {
                    out.println("Inserimento non valido, riprova!\n");
                }
            } while (!flag);
            in.readLine("[INVIO] per tornare al menu");
        } else {
            out.println("Mi dispiace, non ci sono prenotazioni!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void modificaPrenotazione(ArrayList<Tesserato> tesserati, ArrayList<CampoDaGioco> campi,
                                     ArrayList<Prenotazione> prenotazioni) {
        if (!prenotazioni.isEmpty()) {
            int pos = 0, i;
            char c;
            boolean fineElenco = false, flag;
            do {
                for (i = 0; i < righePagina && i + pos < prenotazioni.size(); i++) {
                    out.println(i + 1 + pos + ") " + prenotazioni.get(pos + i).toString());
                }
                if (i + pos >= prenotazioni.size()) {
                    fineElenco = true;
                }
                do {
                    flag = false;
                    if (pos > 0 && !fineElenco) {
                        c = in.readChar("\n[+] per andare avanti, [-] per tornare indietro, [0] per confermare pagina: ");
                    } else if (fineElenco && pos > 0) {
                        c = in.readChar("\n[-] per tornare indietro, [0] per confermare pagina: ");
                    } else if (fineElenco && pos == 0) {
                        c = in.readChar("\n[0] per confermare pagina: ");
                    } else {
                        c = in.readChar("\n[+] per andare avanti, [0] per confermare pagina: ");
                    }
                    if (c == '+' && !fineElenco) {
                        pos += righePagina;
                        flag = true;
                    } else if (c == '-' && pos > 0) {
                        pos -= righePagina;
                        if (fineElenco) {
                            fineElenco = false;
                        }
                        flag = true;
                    } else if (c == '0') {
                        flag = true;
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } while (!flag);
                out.println();
            } while (c != '0');
            flag = false;
            int indice;
            do {
                indice = in.readInt("Inserisci il numero della prenotazione che vuoi modificare: ");
                if (indice > pos && indice <= pos + i) {
                    flag = true;
                } else {
                    out.println("Inserimento non valido, riprova!\n");
                }
            } while (!flag);
            Data oggi = new Data();
            Orario ora = new Orario();
            flag = false;
            if (!(prenotazioni.get(indice - 1).getData().equals(oggi) &&
                    prenotazioni.get(indice - 1).getOraInizio() - ora.getOre() < 5)) {
                out.println("Cosa vuoi modificare?\n");
                out.println("1) Giocatori");
                out.println("2) Data e ora");
                do {
                    int scelta = in.readInt("\nScegli [1 - 2]: ");
                    if (scelta == 1 || scelta == 2) {
                        switch (scelta) {
                            case 1 -> modificaGiocatori(tesserati, prenotazioni, indice);
                            case 2 -> modificaDataOra(campi, prenotazioni, indice);
                        }
                        flag = true;
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } while (!flag);
            } else {
                out.println("\nPuoi modificare una prenotazione fino a 4 ore prima!");
            }
        } else {
            out.println("Mi dispiace, non ci sono prenotazioni!");
        }
        ordinaPrenotazioni(prenotazioni);
        in.readLine("[INVIO] per tornare al menu");
    }

    static void modificaGiocatori(ArrayList<Tesserato> tesserati, ArrayList<Prenotazione> prenotazioni, int indice) {
        Prenotazione prenotazione = prenotazioni.remove(indice - 1);
        prenotazione.removeGiocatori();
        HashMap<Integer, Tesserato> tesseratiMap = new HashMap<>();
        for (Tesserato tess : tesserati) {
            tesseratiMap.put(tess.getNumTessera(), tess);
        }
        out.println("Inserisci i numeri tessera dei giocatori:");
        for (int i = 1; i <= prenotazione.getCampo().getMaxGiocatori(); i++) {
            boolean inserito = false;
            do {
                int scelta = in.readInt("\nGiocatore " + i + ": ");
                if (tesseratiMap.containsKey(scelta)) {
                    inserito = prenotazione.addGiocatore(tesseratiMap.get(scelta));
                }
                if (!inserito) {
                    out.println("Giocatore già inserito o non esistente, riprova");
                } else {
                    out.println(tesseratiMap.get(scelta).toString() + " inserito/a!");
                }
            } while (!inserito);
        }
        prenotazioni.add(indice - 1, prenotazione);
        out.println("\nPrenotazione modificata!");
    }

    static void modificaDataOra(ArrayList<CampoDaGioco> campi, ArrayList<Prenotazione> prenotazioni, int indice) {
        Prenotazione prenotazione = prenotazioni.remove(indice - 1);
        ArrayList<CampoDaGioco> estratti = estraiCampi(campi, prenotazione.getCampo().getTipo());
        int oraInizio;
        Data data = null, oggi = new Data();
        Orario ora = new Orario();
        boolean flag;
        out.println();
        do {
            flag = false;
            do {
                try {
                    data = new Data(in.readLine("Inserisci la data: "));
                    flag = true;
                } catch (SintassiDataScorretta s) {
                    out.println("Sintassi data scorretta, riprova!\n");
                }
            } while (!flag);
            flag = false;
            oraInizio = in.readInt("Inserisci l'ora di inizio (ogni prenotazione vale un'ora soltanto): ");
            if ((data.isMaggiore(oggi) && oraInizio <= 21 && oraInizio >= 9) ||
                    (data.equals(oggi) && oraInizio <= 21 && oraInizio > ora.getOre())) {
                if (oraInizio != prenotazione.getOraInizio() || !data.equals(prenotazione.getData())) {
                    Data fraDieciGiorni = new Data(oggi.getGiorno() + 10, oggi.getMese(), oggi.getAnno());
                    if (data.isMinore(fraDieciGiorni) || data.equals(fraDieciGiorni)) {
                        flag = true;
                    } else {
                        out.println("Puoi prenotare fino a 10 giorni prima, riprova\n");
                    }
                } else {
                    out.println("Hai inserito la stessa data della prenotazione che vuoi modificare, riprova!\n");
                }
            } else {
                out.println("Inserimento non valido, riprova\n");
            }
        } while (!flag);
        boolean occupato = false;
        Prenotazione nuovaPrenotazione = new Prenotazione(prenotazione);
        nuovaPrenotazione.setData(data);
        nuovaPrenotazione.setOraInizio(oraInizio);
        if (!prenotazioni.isEmpty()) {
            Data finalData = data;
            int finalOraInizio = oraInizio;
            ArrayList<Prenotazione> prenotazioniSlot = new ArrayList<>(prenotazioni);
            prenotazioniSlot.removeIf(p -> p.getCampo().getTipo() != estratti.get(0).getTipo() ||
                    (!p.getData().equals(finalData) || p.getOraInizio() != finalOraInizio));
            for (CampoDaGioco c : estratti) {
                occupato = false;
                nuovaPrenotazione.setCampo(c);
                for (Prenotazione p : prenotazioniSlot) {
                    if (p.equals(nuovaPrenotazione)) {
                        occupato = true;
                        break;
                    }
                }
                if (!occupato) {
                    break;
                }
            }
        }
        if (!occupato) {
            prenotazioni.add(indice - 1, nuovaPrenotazione);
            out.println("\nPrenotazione modificata!");
        } else {
            out.println("\nMi dispiace! Nessun campo libero in questo slot");
            prenotazioni.add(indice - 1, prenotazione);
        }
    }

    static void primoSlotLibero(ArrayList<CampoDaGioco> campi, ArrayList<Prenotazione> prenotazioni) {
        ArrayList<CampoDaGioco> estratti;
        TipoCampo[] tipi = TipoCampo.values();
        TipoCampo tipo = null;
        out.println("Primo slot di quale campo?\n");
        int i = 1;
        for (TipoCampo t : tipi) {
            out.println(i + ") " + t.toString());
            i++;
        }
        boolean flag = false;
        do {
            int scelta = in.readInt("\nScegli [1 - 5]: ");
            try {
                tipo = tipi[scelta - 1];
                flag = true;
            } catch (ArrayIndexOutOfBoundsException a) {
                out.println("Inserimento non valido, riprova");
            }
        } while (!flag);
        estratti = estraiCampi(campi, tipo);
        if (!estratti.isEmpty()) {
            ArrayList<Prenotazione> prenotazioniCampo = new ArrayList<>(prenotazioni);
            prenotazioniCampo.removeIf(p -> p.getCampo().getTipo() != estratti.get(0).getTipo());
            if (ordinamentoPrenotazione != Prenotazione.Ordinamento.DATA) {
                Prenotazione.Ordinamento temp = ordinamentoPrenotazione;
                ordinamentoPrenotazione = Prenotazione.Ordinamento.DATA;
                ordinaPrenotazioni(prenotazioniCampo);
                ordinamentoPrenotazione = temp;
            }
            Data data = new Data();
            Orario ora = new Orario();
            int oraInizio = ora.getOre() + 1, giorno = data.getGiorno(), mese = data.getMese(), anno = data.getAnno();
            boolean occupato = true;
            while (true) {
                for (i = oraInizio; i <= 21 && occupato; i++) {
                    for (CampoDaGioco c : estratti) {
                        occupato = false;
                        Prenotazione prenotazione = new Prenotazione(c, data, i);
                        for (Prenotazione p : prenotazioniCampo) {
                            if (p.getData().isMaggiore(data)) {
                                break;
                            }
                            if (p.equals(prenotazione)) {
                                occupato = true;
                                break;
                            }
                        }
                        if (!occupato) {
                            break;
                        }
                    }
                    oraInizio = i;
                }
                if (!occupato) {
                    break;
                }
                oraInizio = 9;
                data = new Data(++giorno, mese, anno);
            }
            out.println("\nPrimo slot libero per un campo da " + tipo.toString() +
                    ": " + data + " alle " + oraInizio);
        } else {
            out.println("\nMi dispiace, non ci sono campi del tipo selezionato!");
        }
        in.readLine("[INVIO] per tornare al menu");
    }

    static void campiSlotScelto(ArrayList<CampoDaGioco> campi, ArrayList<Prenotazione> prenotazioni) {
        ArrayList<CampoDaGioco> estratti;
        Data data = null;
        int oraInizio;
        Prenotazione prenotazione;
        TipoCampo[] tipi = TipoCampo.values();
        TipoCampo tipo = null;
        out.println("Che tipo di campo vuoi cercare?\n");
        int i = 1;
        for (TipoCampo t : tipi) {
            out.println(i + ") " + t.toString());
            i++;
        }
        boolean flag = false;
        do {
            int scelta = in.readInt("\nScegli [1 - 5]: ");
            try {
                tipo = tipi[scelta - 1];
                flag = true;
            } catch (ArrayIndexOutOfBoundsException a) {
                out.println("Inserimento non valido, riprova");
            }
        } while (!flag);
        estratti = estraiCampi(campi, tipo);  //tutti i campi del tipo scelto
        if (!estratti.isEmpty()) {
            do {
                flag = false;
                Data oggi = new Data();
                Orario ora = new Orario();
                do {
                    try {
                        data = new Data(in.readLine("Inserisci la data: "));
                        flag = true;
                    } catch (SintassiDataScorretta s) {
                        out.println("Sintassi data scorretta, riprova!\n");
                    }
                } while (!flag);
                flag = false;
                oraInizio = in.readInt("Inserisci l'ora di inizio (ogni prenotazione vale un'ora soltanto): ");
                if ((data.isMaggiore(oggi) && oraInizio <= 21 && oraInizio >= 9) ||
                        (data.equals(oggi) && oraInizio <= 21 && oraInizio > ora.getOre())) {
                    flag = true;
                } else {
                    out.println("Inserimento non valido, riprova\n");
                }
            } while (!flag);
            boolean occupato;
            out.println("Campi liberi nello slot selezionato:\n");
            if (prenotazioni.isEmpty()) {
                for (CampoDaGioco c : estratti) {
                    out.println(c);
                }
            } else {
                Data finalData = data;
                int finalOraInizio = oraInizio;
                ArrayList<Prenotazione> prenotazioniSlot = new ArrayList<>(prenotazioni);
                prenotazioniSlot.removeIf(p -> p.getCampo().getTipo() != estratti.get(0).getTipo() ||
                        (!p.getData().equals(finalData) || p.getOraInizio() != finalOraInizio));
                for (CampoDaGioco c : estratti) {
                    occupato = false;
                    prenotazione = new Prenotazione(c, data, oraInizio);
                    for (Prenotazione p : prenotazioniSlot) {
                        if (p.equals(prenotazione)) {
                            occupato = true;
                            break;
                        }
                    }
                    if (!occupato) {
                        out.println(c);
                        flag = false;
                    }
                }
                if (flag) {
                    out.println("Nessun campo libero!");
                }
            }
        } else {
            out.println("\nMi dispiace, non ci sono campi del tipo selezionato!");
        }
        in.readLine("[INVIO] per tornare al menu");
    }

    static void modificaElencoTesserati(ArrayList<Tesserato> tesserati, ArrayList<Prenotazione> prenotazioni) {
        boolean flag = false;
        out.println("Cosa vuoi fare?\n");
        out.println("1) Aggiungi dei tesserati");
        out.println("2) Elimina un tesserato");
        do {
            int scelta = in.readInt("\nScegli [1 - 2]: ");
            if (scelta == 1 || scelta == 2) {
                flag = true;
                switch (scelta) {
                    case 1 -> inserisciTesserati(tesserati, false);
                    case 2 -> eliminaTesserati(tesserati, prenotazioni);
                }
            } else {
                out.println("Inserimento non valido, riprova!");
            }
        } while (!flag);
        in.readLine("[INVIO] per tornare al menu");
    }

    static void eliminaTesserati(ArrayList<Tesserato> tesserati, ArrayList<Prenotazione> prenotazioni) {
        if (!tesserati.isEmpty()) {
            int pos = 0, i;
            char c;
            boolean fineElenco = false, flag;
            out.println();
            do {
                for (i = 0; i < righePagina && i + pos < tesserati.size(); i++) {
                    out.println(i + 1 + pos + ") " + tesserati.get(pos + i).toString());
                }
                if (i + pos >= tesserati.size()) {
                    fineElenco = true;
                }
                do {
                    flag = false;
                    if (pos > 0 && !fineElenco) {
                        c = in.readChar("\n[+] per andare avanti, [-] per tornare indietro, [0] per confermare pagina: ");
                    } else if (fineElenco && pos > 0) {
                        c = in.readChar("\n[-] per tornare indietro, [0] per confermare pagina: ");
                    } else if (fineElenco && pos == 0) {
                        c = in.readChar("\n[0] per confermare pagina: ");
                    } else {
                        c = in.readChar("\n[+] per andare avanti, [0] per confermare pagina: ");
                    }
                    if (c == '+' && !fineElenco) {
                        pos += righePagina;
                        flag = true;
                    } else if (c == '-' && pos > 0) {
                        pos -= righePagina;
                        if (fineElenco) {
                            fineElenco = false;
                        }
                        flag = true;
                    } else if (c == '0') {
                        flag = true;
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } while (!flag);
                out.println();
            } while (c != '0');
            flag = false;
            boolean protetto = false;
            int scelta;
            do {
                scelta = in.readInt("Inserisci il numero d'elenco del tesserato che vuoi eliminare: ");
                if (scelta > pos && scelta <= pos + i) {
                    flag = true;
                } else {
                    out.println("Inserimento non valido, riprova!\n");
                }
            } while (!flag);
            for (Prenotazione p : prenotazioni) {
                if (p.getGiocatori().contains(tesserati.get(scelta - 1))) {
                    protetto = true;
                    break;
                }
            }
            if (protetto) {
                out.println("\nImpossibile eliminare il tesserato scelto, una o più prenotazioni a suo nome!");
            } else {
                out.println("\n" + tesserati.remove(scelta - 1) + " eliminato/a!");
            }
        } else {
            out.println("\nMi dispiace, non ci sono tesserati!");
        }
    }

    static void modificaElencoCampi(ArrayList<CampoDaGioco> campi, ArrayList<Prenotazione> prenotazioni) {
        boolean flag = false;
        out.println("Cosa vuoi fare?\n");
        out.println("1) Aggiungi dei campi");
        out.println("2) Elimina un campo");
        do {
            int scelta = in.readInt("\nScegli [1 - 2]: ");
            if (scelta == 1 || scelta == 2) {
                flag = true;
                switch (scelta) {
                    case 1 -> inserisciCampi(campi, false);
                    case 2 -> eliminaCampi(campi, prenotazioni);
                }
            } else {
                out.println("Inserimento non valido, riprova!");
            }
        } while (!flag);
        in.readLine("[INVIO] per tornare al menu");
    }

    static void eliminaCampi(ArrayList<CampoDaGioco> campi, ArrayList<Prenotazione> prenotazioni) {
        ArrayList<CampoDaGioco> estratti;
        ArrayList<Prenotazione> prenotazioniCampo;
        TipoCampo[] tipi = TipoCampo.values();
        TipoCampo tipo = null;
        out.println("\nChe tipo di campo vuoi eliminare?\n");
        int i = 1;
        for (TipoCampo t : tipi) {
            out.println(i + ") " + t.toString());
            i++;
        }
        boolean flag = false;
        do {
            int scelta = in.readInt("\nScegli [1 - 5]: ");
            try {
                tipo = tipi[scelta - 1];
                flag = true;
            } catch (ArrayIndexOutOfBoundsException a) {
                out.println("Inserimento non valido, riprova");
            }
        } while (!flag);
        estratti = estraiCampi(campi, tipo);
        if (!estratti.isEmpty()) {
            prenotazioniCampo = new ArrayList<>(prenotazioni);
            prenotazioniCampo.removeIf(pren -> pren.getCampo().getTipo() != estratti.get(0).getTipo());
            boolean occupato = false;
            for (CampoDaGioco c : estratti) {
                occupato = false;
                for (Prenotazione p : prenotazioniCampo) {
                    if (p.getCampo().getCodCampo() == c.getCodCampo()) {
                        occupato = true;
                        break;
                    }
                }
                if (!occupato) {
                    out.println("\n" + c + " eliminato!");
                    campi.remove(c);
                    break;
                }
            }
            if (occupato) {
                out.println("\nMi dispiace, tutti i campi del tipo selezionato sono prenotati!");
            }
        } else {
            out.println("\nMi dispiace, non ci sono campi del tipo selezionato!");
        }
    }

    static void salvaStato(ArrayList<Tesserato> tesserati, ArrayList<CampoDaGioco> campi,
                           ArrayList<Prenotazione> prenotazioni, boolean termineProgramma) throws IOException {
        out.println("Salvo lo stato nel file: " + nomeFileStato);
        FileOutputStream fos = new FileOutputStream(nomeFileStato);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(tesserati);
        oos.writeObject(campi);
        oos.writeObject(prenotazioni);
        oos.writeObject(Tesserato.getProxNumTessera());
        oos.writeObject(CampoDaGioco.getProxCodCampo());
        oos.writeObject(ordinamentoTesserato);
        oos.writeObject(ordinamentoCampo);
        oos.writeObject(ordinamentoPrenotazione);
        oos.flush();
        oos.close();
        fos.close();
        out.println("Stato salvato!");
        if (!termineProgramma) {
            in.readLine("[INVIO] per tornare al menu");
        }
    }
}