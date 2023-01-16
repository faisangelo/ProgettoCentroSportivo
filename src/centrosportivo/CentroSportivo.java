package centrosportivo;

import prog.io.ConsoleInputManager;
import prog.io.ConsoleOutputManager;
import prog.utili.Data;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CentroSportivo {
    static final String nomeFileStato = "centrosportivo.bin";
    static int righePagina = 15;
    static CampoDaGioco.Ordinamento ordinamentoCampo = CampoDaGioco.Ordinamento.CODCAMPO;
    static Tesserato.Ordinamento ordinamentoTesserato = Tesserato.Ordinamento.COGNOME;
    static Prenotazione.Ordinamento ordinamentoPrenotazione = Prenotazione.Ordinamento.DATA;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ConsoleOutputManager out = new ConsoleOutputManager();
        ConsoleInputManager in = new ConsoleInputManager();
        ArrayList<Tesserato> tesserati;
        ArrayList<CampoDaGioco> campi;
        ArrayList<Prenotazione> prenotazioni;
        out.println("Carico file di stato: " + nomeFileStato);
        try {
            FileInputStream fis = new FileInputStream(nomeFileStato);
            ObjectInputStream ois = new ObjectInputStream(fis);
            tesserati = (ArrayList<Tesserato>) ois.readObject();
            campi = (ArrayList<CampoDaGioco>) ois.readObject();
            prenotazioni = (ArrayList<Prenotazione>) ois.readObject();
            ois.close();
            fis.close();
            if (!prenotazioni.isEmpty()) {
                Data data = new Data();
                prenotazioni.removeIf(p -> p.getData().isMinore(data));  //cancella le prenotazioni dei giorni passati
            }
        } catch (FileNotFoundException f) {
            out.println("File non trovato, passo all'inserimento dei dati\n");
            tesserati = inserisciTesserati(in, out);
            out.println();
            campi = inserisciCampi(in, out);
            prenotazioni = new ArrayList<>();
        }
        int scelta;
        do {
            out.println("\n*** CENTRO SPORTIVO ***"); //menu provvisorio
            out.print("1) Scorri elenco tesserati          ");
            out.println("6) Ordina le prenotazioni");
            out.print("2) Scorri elenco campi da gioco     ");
            out.println("7) Prenota un campo da gioco");
            out.print("3) Scorri elenco prenotazioni       ");
            out.println("8) Cancella una prenotazione");
            out.print("4) Ordina i tesserati               ");
            out.println("9) Modifica una prenotazione");
            out.println("5) Ordina i campi da gioco");
            out.println("\n0 per uscire");
            scelta = in.readInt("\nScegli [0 - 9]: ");
            switch (scelta) {
                case 1 -> stampaTesserati(tesserati, in, out);
                case 2 -> stampaCampi(campi, in, out);
                case 3 -> stampaPrenotazioni(prenotazioni, in, out);
                case 4 -> stampaTesseratiOrdinati(tesserati, in, out);
                case 5 -> stampaCampiOrdinati(campi, in, out);
                case 6 -> stampaPrenotazioniOrdinate(prenotazioni, in, out);
                case 7 -> effettuaPrenotazione(prenotazioni, campi, tesserati, in, out);
                case 8 -> cancellaPrenotazione(prenotazioni, in, out);
                case 9 -> modificaPrenotazione(prenotazioni, campi, tesserati, in, out);
            }
        } while (scelta != 0);
        out.println("\nSalvo lo stato nel file: " + nomeFileStato);
        FileOutputStream fos = new FileOutputStream(nomeFileStato);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(tesserati);
        oos.writeObject(campi);
        oos.writeObject(prenotazioni);
        oos.flush();
        oos.close();
        fos.close();
    }

    static ArrayList<Tesserato> inserisciTesserati(ConsoleInputManager in, ConsoleOutputManager out) {
        ArrayList<Tesserato> tesserati = new ArrayList<>();
        boolean continua;
        String nome, cognome;
        out.println("*** INSERIMENTO TESSERATI ***");
        do {
            nome = in.readLine("\nInserisci nome: ");
            cognome = in.readLine("Inserisci cognome: ");
            Tesserato t = new Tesserato(nome, cognome);
            tesserati.add(t);
            out.println("\nTesserato inserito! Numero tessera: " + t.getNumTessera());
            continua = !in.readSiNo("Finito? [s] [n]: ");
        } while (continua);
        return tesserati;
    }

    static ArrayList<CampoDaGioco> inserisciCampi(ConsoleInputManager in, ConsoleOutputManager out) {
        ArrayList<CampoDaGioco> campi = new ArrayList<>();
        boolean continua;
        TipoCampo[] tipi = TipoCampo.values();
        CampoDaGioco campo = null;
        out.println("*** INSERIMENTO CAMPI DA GIOCO ***");
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
        return campi;
    }

    static void stampaTesserati(ArrayList<Tesserato> tesserati, ConsoleInputManager in,
                                ConsoleOutputManager out) {
        int pos = 0, i;
        char c;
        boolean fineElenco = false;
        do {
            for (i = 0; i < righePagina && i + pos < tesserati.size(); i++) {
                out.println(tesserati.get(pos + i).toString());
            }
            if (i + pos >= tesserati.size()) {
                fineElenco = true;
            }
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
            } else if (c == '-' && pos > 0) {
                pos -= righePagina;
                if (fineElenco) {
                    fineElenco = false;
                }
            }
            out.println();
        } while (c != '0');
    }

    static void stampaCampi(ArrayList<CampoDaGioco> campi, ConsoleInputManager in,
                            ConsoleOutputManager out) {
        int pos = 0, i;
        char c;
        boolean fineElenco = false;
        do {
            for (i = 0; i < righePagina && i + pos < campi.size(); i++) {
                out.println(campi.get(pos + i).toString());
            }
            if (i + pos >= campi.size()) {
                fineElenco = true;
            }
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
            } else if (c == '-' && pos > 0) {
                pos -= righePagina;
                if (fineElenco) {
                    fineElenco = false;
                }
            }
            out.println();
        } while (c != '0');
    }

    static void stampaPrenotazioni(ArrayList<Prenotazione> prenotazioni, ConsoleInputManager in,
                                   ConsoleOutputManager out) {
        int pos = 0, i;
        char c;
        boolean fineElenco = false;
        do {
            for (i = 0; i < righePagina && i + pos < prenotazioni.size(); i++) {
                out.println(i + 1 + ") " + prenotazioni.get(pos + i).toString());
            }
            if (i + pos >= prenotazioni.size()) {
                fineElenco = true;
            }
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
            } else if (c == '-' && pos > 0) {
                pos -= righePagina;
                if (fineElenco) {
                    fineElenco = false;
                }
            }
            out.println();
        } while (c != '0');
    }

    static void stampaTesseratiOrdinati(ArrayList<Tesserato> tesserati, ConsoleInputManager in,
                                        ConsoleOutputManager out) {
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
        stampaTesserati(tesserati, in, out);
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

    static void stampaCampiOrdinati(ArrayList<CampoDaGioco> campi, ConsoleInputManager in,
                                    ConsoleOutputManager out) {
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
        stampaCampi(campi, in, out);
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

    static void stampaPrenotazioniOrdinate(ArrayList<Prenotazione> prenotazioni, ConsoleInputManager in,
                                           ConsoleOutputManager out) {
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
        stampaPrenotazioni(prenotazioni, in, out);
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

    static void effettuaPrenotazione(ArrayList<Prenotazione> prenotazioni, ArrayList<CampoDaGioco> campi,
                                     ArrayList<Tesserato> tesserati, ConsoleInputManager in, ConsoleOutputManager out) {
        ArrayList<CampoDaGioco> estratti;
        Data data;
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
            flag = false;
            do {
                Data oggi = new Data();
                data = new Data(in.readLine("Inserisci la data: "));
                oraInizio = in.readInt("Inserisci l'ora di inizio (ogni prenotazione vale un'ora soltanto): ");
                if ((data.isMaggiore(oggi) || data.equals(oggi)) && oraInizio <= 21 && oraInizio >= 9) {
                    flag = true;
                } else {
                    out.println("Inserimento non valido, riprova");
                }
            } while (!flag);
            boolean occupato = false;
            if (prenotazioni.isEmpty()) {
                prenotazione = new Prenotazione(estratti.get(0), data, oraInizio);
            } else {
                for (CampoDaGioco c : estratti) {
                    occupato = false;
                    prenotazione = new Prenotazione(c, data, oraInizio);
                    for (Prenotazione p : prenotazioni) {
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
                Tesserato.Ordinamento temp = ordinamentoTesserato;
                if (ordinamentoTesserato != Tesserato.Ordinamento.NUMEROTESSERA) {
                    ordinamentoTesserato = Tesserato.Ordinamento.NUMEROTESSERA;
                    ordinaTesserati(tesserati);
                }
                out.println("Campo libero, inserisci i numeri tessera dei giocatori!");
                for (int j = 1; j <= prenotazione.getCampo().getMaxGiocatori(); j++) {
                    Tesserato t;
                    boolean inserito = false;
                    do {
                        int scelta = in.readInt("\nGiocatore " + j + ": ");
                        try {
                            t = tesserati.get(scelta - 1);
                            inserito = prenotazione.addGiocatore(t);
                            if (!inserito) {
                                out.println("Giocatore già inserito, riprova");
                            }
                        } catch (IndexOutOfBoundsException e) {
                            out.println("Inserimento non valido, riprova");
                        }
                    } while (!inserito);
                }
                if (temp != ordinamentoTesserato) {
                    ordinamentoTesserato = temp;
                    ordinaTesserati(tesserati);
                }
                prenotazioni.add(prenotazione);
                out.println("Prenotazione confermata! Visualizzo le prenotazioni\n");
                stampaPrenotazioni(prenotazioni, in, out);
            } else {
                out.println("Mi dispiace! Nessun campo libero in questo slot");
                in.readLine("[INVIO] per tornare al menu");
            }
        } else {
            out.println("Mi dispiace! Il numero di tesserati è inferiore ai giocatori richiesti oppure non esistono " +
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

    static void cancellaPrenotazione(ArrayList<Prenotazione> prenotazioni, ConsoleInputManager in,
                                     ConsoleOutputManager out) {
        int pos = 0, i;
        char c = '0';
        boolean fineElenco = false;
        do {
            for (i = 0; i < righePagina && i + pos < prenotazioni.size(); i++) {
                out.println(i + 1 + ") " + prenotazioni.get(pos + i).toString());
            }
            if (i + pos >= prenotazioni.size()) {
                fineElenco = true;
            }
            if (in.readSiNo("\nVuoi cambiare pagina? [s] [n]: ")) {
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
                } else if (c == '-' && pos > 0) {
                    pos -= righePagina;
                    if (fineElenco) {
                        fineElenco = false;
                    }
                }
            } else {
                boolean flag = false;
                do {
                    int scelta = in.readInt("Inserisci il numero della prenotazione che vuoi cancellare: ");
                    if (scelta > pos && scelta <= pos + i) {
                        prenotazioni.remove(scelta - 1);
                        out.println("Prenotazione cancellata!");
                        in.readLine("[INVIO] per tornare al menu");
                        c = '0';
                        flag = true;
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } while (!flag);
            }
            out.println();
        } while (c != '0');
    }

    static void modificaPrenotazione(ArrayList<Prenotazione> prenotazioni, ArrayList<CampoDaGioco> campi,
                                     ArrayList<Tesserato> tesserati, ConsoleInputManager in, ConsoleOutputManager out) {
        int pos = 0, i;
        char c = '0';
        boolean fineElenco = false;
        do {
            for (i = 0; i < righePagina && i + pos < prenotazioni.size(); i++) {
                out.println(i + 1 + ") " + prenotazioni.get(pos + i).toString());
            }
            if (i + pos >= prenotazioni.size()) {
                fineElenco = true;
            }
            if (in.readSiNo("\nVuoi cambiare pagina? [s] [n]: ")) {
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
                } else if (c == '-' && pos > 0) {
                    pos -= righePagina;
                    if (fineElenco) {
                        fineElenco = false;
                    }
                }
            } else {
                boolean flag = false;
                do {
                    int indice = in.readInt("Inserisci il numero della prenotazione che vuoi modificare: ");
                    if (indice > pos && indice <= pos + i) {
                        out.println("Cosa vuoi modificare?");
                        out.println("1) Giocatori");
                        out.println("2) Data e ora");
                        int scelta = in.readInt("\nScegli [1 - 2]: ");
                        if (scelta == 1 || scelta == 2) {
                            switch (scelta) {
                                case 1 -> modificaGiocatori(prenotazioni, tesserati, indice, in, out);
                                case 2 -> modificaDataOra(prenotazioni, campi, indice, in, out);
                            }
                            flag = true;
                            in.readLine("[INVIO] per tornare al menu");
                            c = '0';
                        } else {
                            out.println("Inserimento non valido, riprova!");
                        }
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } while (!flag);
            }
            out.println();
        } while (c != '0');
    }

    static void modificaGiocatori(ArrayList<Prenotazione> prenotazioni, ArrayList<Tesserato> tesserati,
                                  int indice, ConsoleInputManager in, ConsoleOutputManager out) {
        Prenotazione prenotazione = prenotazioni.remove(indice - 1);
        prenotazione.removeGiocatori();
        Tesserato.Ordinamento temp = ordinamentoTesserato;
        if (ordinamentoTesserato != Tesserato.Ordinamento.NUMEROTESSERA) {
            ordinamentoTesserato = Tesserato.Ordinamento.NUMEROTESSERA;
            ordinaTesserati(tesserati);
        }
        for (int i = 1; i <= prenotazione.getCampo().getMaxGiocatori(); i++) {
            Tesserato t;
            boolean inserito = false;
            do {
                int scelta = in.readInt("\nGiocatore " + i + ": ");
                try {
                    t = tesserati.get(scelta - 1);
                    inserito = prenotazione.addGiocatore(t);
                    if (!inserito) {
                        out.println("Giocatore già inserito, riprova");
                    }
                } catch (IndexOutOfBoundsException e) {
                    out.println("Inserimento non valido, riprova");
                }
            } while (!inserito);
        }
        if (temp != ordinamentoTesserato) {
            ordinamentoTesserato = temp;
            ordinaTesserati(tesserati);
        }
        prenotazioni.add(indice - 1, prenotazione);
        out.println("Prenotazione modificata!");
    }

    static void modificaDataOra(ArrayList<Prenotazione> prenotazioni, ArrayList<CampoDaGioco> campi, int indice,
                                ConsoleInputManager in, ConsoleOutputManager out) {
        Prenotazione prenotazione = prenotazioni.remove(indice - 1);
        ArrayList<CampoDaGioco> estratti = estraiCampi(campi, prenotazione.getCampo().getTipo());
        int oraInizio;
        Data data;
        boolean flag = false;
        do {
            Data oggi = new Data();
            data = new Data(in.readLine("Inserisci la data: "));
            oraInizio = in.readInt("Inserisci l'ora di inizio (ogni prenotazione vale un'ora soltanto): ");
            if ((data.isMaggiore(oggi) || data.equals(oggi)) && oraInizio <= 21 && oraInizio >= 9) {
                if (oraInizio != prenotazione.getOraInizio() || !data.equals(prenotazione.getData())) {
                    flag = true;
                } else {
                    out.println("Hai inserito la stessa data della prenotazione che vuoi modificare, riprova!");
                }
            } else {
                out.println("Inserimento non valido, riprova");
            }
        } while (!flag);
        boolean occupato = false;
        Prenotazione nuovaPrenotazione = prenotazione;
        nuovaPrenotazione.setData(data);
        nuovaPrenotazione.setOraInizio(oraInizio);
        if (!prenotazioni.isEmpty()) {
            for (CampoDaGioco c : estratti) {
                occupato = false;
                nuovaPrenotazione.setCampo(c);
                for (Prenotazione p : prenotazioni) {
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
            out.println("Prenotazione modificata!");
        } else {
            out.println("Mi dispiace! Nessun campo libero in questo slot");
            prenotazioni.add(indice - 1, prenotazione);
        }
    }
}