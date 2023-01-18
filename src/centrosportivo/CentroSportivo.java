package centrosportivo;

import prog.io.ConsoleInputManager;
import prog.io.ConsoleOutputManager;
import prog.utili.Data;
import prog.utili.Orario;

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
            Tesserato.setProxNumTessera((Integer) ois.readObject());
            CampoDaGioco.setProxCodCampo((Integer) ois.readObject());
            ois.close();
            fis.close();
            if (!prenotazioni.isEmpty()) {
                Data data = new Data();
                Orario ora = new Orario();
                prenotazioni.removeIf(p -> p.getData().isMinore(data) ||
                        (p.getData().equals(data) && p.getOraInizio() < ora.getOre()));  //cancella le prenotazioni passate
            }
        } catch (FileNotFoundException f) {
            out.println("File non trovato, passo all'inserimento dei dati\n");
            tesserati = inserisciTesserati(in, out);
            out.println();
            campi = inserisciCampi(in, out);
            prenotazioni = new ArrayList<>();
        }
        in.readLine("[INVIO] per accedere al menu");
        int scelta;
        do {
            out.println("\n*** CENTRO SPORTIVO ***"); //menu provvisorio
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
            out.println("7) Prenota un campo da gioco");
            out.println("\n0 per uscire");
            boolean flag = false;
            do {
                scelta = in.readInt("\nScegli [0 - 13]: ");
                if (0 <= scelta && scelta <= 13) {
                    flag = true;
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
                        case 10 -> primoSlotLibero(prenotazioni, campi, in, out);
                        case 11 -> campiSlotScelto(prenotazioni, campi, in, out);
                        case 12 -> modificaElencoTesserati(tesserati, in, out);
                        case 13 -> modificaElencoCampi(campi, in, out);
                    }
                } else {
                    out.println("Inserimento non valido, riprova!");
                }
            } while (!flag);
        } while (scelta != 0);
        out.println("\nSalvo lo stato nel file: " + nomeFileStato);
        FileOutputStream fos = new FileOutputStream(nomeFileStato);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(tesserati);
        oos.writeObject(campi);
        oos.writeObject(prenotazioni);
        oos.writeObject(Tesserato.getProxNumTessera());
        oos.writeObject(CampoDaGioco.getProxCodCampo());
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
        if (!tesserati.isEmpty()) {
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
        } else {
            out.println("Mi dispiace, non ci sono tesserati!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void stampaCampi(ArrayList<CampoDaGioco> campi, ConsoleInputManager in,
                            ConsoleOutputManager out) {
        if (!campi.isEmpty()) {
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
        } else {
            out.println("Mi dispiace, non ci sono campi!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void stampaPrenotazioni(ArrayList<Prenotazione> prenotazioni, ConsoleInputManager in,
                                   ConsoleOutputManager out) {
        if (!prenotazioni.isEmpty()) {
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
        } else {
            out.println("Mi dispiace, non ci sono prenotazioni!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }

    static void stampaTesseratiOrdinati(ArrayList<Tesserato> tesserati, ConsoleInputManager in,
                                        ConsoleOutputManager out) {
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
            in.readLine("[INVIO] per visualizzare");
            stampaTesserati(tesserati, in, out);
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

    static void stampaCampiOrdinati(ArrayList<CampoDaGioco> campi, ConsoleInputManager in,
                                    ConsoleOutputManager out) {
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
            in.readLine("[INVIO] per visualizzare");
            stampaCampi(campi, in, out);
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

    static void stampaPrenotazioniOrdinate(ArrayList<Prenotazione> prenotazioni, ConsoleInputManager in,
                                           ConsoleOutputManager out) {
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
            in.readLine("[INVIO] per visualizzare");
            stampaPrenotazioni(prenotazioni, in, out);
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
                Orario ora = new Orario();
                data = new Data(in.readLine("Inserisci la data: "));
                oraInizio = in.readInt("Inserisci l'ora di inizio (ogni prenotazione vale un'ora soltanto): ");
                if ((data.isMaggiore(oggi) && oraInizio <= 21 && oraInizio >= 9) ||
                        (data.equals(oggi) && oraInizio <= 21 && oraInizio > ora.getOre())) {
                    flag = true;
                } else {
                    out.println("Inserimento non valido, riprova");
                }
            } while (!flag);
            boolean occupato = false;
            if (prenotazioni.isEmpty()) {
                prenotazione = new Prenotazione(estratti.get(0), data, oraInizio);
            } else {
                Data finalData = data;
                int finalOraInizio = oraInizio;
                ArrayList<Prenotazione> prenotazioniSlot = prenotazioni;
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
                in.readLine("[INVIO] per visualizzare");
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
        if (!prenotazioni.isEmpty()) {
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
                } else if (c == '-' && pos > 0) {
                    pos -= righePagina;
                    if (fineElenco) {
                        fineElenco = false;
                    }
                }
                out.println();
            } while (c != '0');
            boolean flag = false;
            do {
                int scelta = in.readInt("Inserisci il numero della prenotazione che vuoi cancellare: ");
                if (scelta > pos && scelta <= pos + i) {
                    prenotazioni.remove(scelta - 1);
                    out.println("Prenotazione cancellata!");
                    flag = true;
                } else {
                    out.println("Inserimento non valido, riprova!");
                }
            } while (!flag);
            in.readLine("[INVIO] per tornare al menu");
        } else {
            out.println("Mi dispiace, non ci sono prenotazioni!");
            in.readLine("[INVIO] per tornare al menu");
        }
    }


    static void modificaPrenotazione(ArrayList<Prenotazione> prenotazioni, ArrayList<CampoDaGioco> campi,
                                     ArrayList<Tesserato> tesserati, ConsoleInputManager in, ConsoleOutputManager out) {
        if (!prenotazioni.isEmpty()) {
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
                } else if (c == '-' && pos > 0) {
                    pos -= righePagina;
                    if (fineElenco) {
                        fineElenco = false;
                    }
                }
                out.println();
            } while (c != '0');
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
                    } else {
                        out.println("Inserimento non valido, riprova!");
                    }
                } else {
                    out.println("Inserimento non valido, riprova!");
                }
            } while (!flag);
        } else {
            out.println("Mi dispiace, non ci sono prenotazioni!");
        }
        in.readLine("[INVIO] per tornare al menu");
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
            Orario ora = new Orario();
            data = new Data(in.readLine("Inserisci la data: "));
            oraInizio = in.readInt("Inserisci l'ora di inizio (ogni prenotazione vale un'ora soltanto): ");
            if ((data.isMaggiore(oggi) && oraInizio <= 21 && oraInizio >= 9) ||
                    (data.equals(oggi) && oraInizio <= 21 && oraInizio > ora.getOre())) {
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
            Data finalData = data;
            int finalOraInizio = oraInizio;
            ArrayList<Prenotazione> prenotazioniSlot = prenotazioni;
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
            out.println("Prenotazione modificata!");
        } else {
            out.println("Mi dispiace! Nessun campo libero in questo slot");
            prenotazioni.add(indice - 1, prenotazione);
        }
    }

    static void primoSlotLibero(ArrayList<Prenotazione> prenotazioni, ArrayList<CampoDaGioco> campi,
                                ConsoleInputManager in, ConsoleOutputManager out) {
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
            ArrayList<Prenotazione> prenotazioniCampo = prenotazioni;
            prenotazioniCampo.removeIf(p -> p.getCampo().getTipo() != estratti.get(0).getTipo());
            Prenotazione.Ordinamento temp = ordinamentoPrenotazione;
            if (ordinamentoPrenotazione != Prenotazione.Ordinamento.DATA) {
                ordinamentoPrenotazione = Prenotazione.Ordinamento.DATA;
            }
            ordinaPrenotazioni(prenotazioniCampo);
            if (temp != ordinamentoPrenotazione) {
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
            out.println("Primo slot libero per un campo da " + tipo.toString() +
                    ": " + data + " alle " + oraInizio);
        } else {
            out.println("Mi dispiace, non esistono campi del tipo selezionato!");
        }
        in.readLine("[INVIO] per tornare al menu");
    }

    static void campiSlotScelto(ArrayList<Prenotazione> prenotazioni, ArrayList<CampoDaGioco> campi,
                                ConsoleInputManager in, ConsoleOutputManager out) {
        ArrayList<CampoDaGioco> estratti;
        Data data;
        int oraInizio;
        Prenotazione prenotazione = null;
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
            flag = false;
            do {
                Data oggi = new Data();
                Orario ora = new Orario();
                data = new Data(in.readLine("Inserisci la data: "));
                oraInizio = in.readInt("Inserisci l'ora di inizio (ogni prenotazione vale un'ora soltanto): ");
                if ((data.isMaggiore(oggi) && oraInizio <= 21 && oraInizio >= 9) ||
                        (data.equals(oggi) && oraInizio <= 21 && oraInizio > ora.getOre())) {
                    flag = true;
                } else {
                    out.println("Inserimento non valido, riprova");
                }
            } while (!flag);
            boolean occupato;
            out.println("Campi liberi nello slot selezionato:");
            if (prenotazioni.isEmpty()) {
                for (CampoDaGioco c : estratti) {
                    out.println(c);
                }
            } else {
                Data finalData = data;
                int finalOraInizio = oraInizio;
                ArrayList<Prenotazione> prenotazioniSlot = prenotazioni;
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
            out.println("Mi dispiace, non esistono campi del tipo selezionato!");
        }
        in.readLine("[INVIO] per tornare al menu");
    }

    static void modificaElencoTesserati(ArrayList<Tesserato> tesserati, ConsoleInputManager in,
                                        ConsoleOutputManager out) {
        boolean flag = false;
        do {
            out.println("Cosa vuoi fare?");
            out.println("1) Aggiungi dei tesserati");
            out.println("2) Elimina dei tesserati");
            int scelta = in.readInt("\nScegli [1 - 2]: ");
            if (scelta == 1 || scelta == 2) {
                flag = true;
                switch (scelta) {
                    case 1 -> aggiungiTesserati(tesserati, in, out);
                    case 2 -> eliminaTesserati(tesserati, in, out);
                }

            } else {
                out.println("Inserimento non valido, riprova!");
            }
        } while (!flag);
        in.readLine("[INVIO] per tornare al menu");
    }

    static void aggiungiTesserati(ArrayList<Tesserato> tesserati, ConsoleInputManager in,
                                  ConsoleOutputManager out) {
        boolean continua;
        String nome, cognome;
        do {
            nome = in.readLine("\nInserisci nome: ");
            cognome = in.readLine("Inserisci cognome: ");
            Tesserato t = new Tesserato(nome, cognome);
            tesserati.add(t);
            out.println("\nTesserato inserito! Numero tessera: " + t.getNumTessera());
            continua = !in.readSiNo("Finito? [s] [n]: ");
        } while (continua);
        ordinaTesserati(tesserati);
    }

    static void eliminaTesserati(ArrayList<Tesserato> tesserati, ConsoleInputManager in,
                                 ConsoleOutputManager out) {
        if (!tesserati.isEmpty()) {
            int pos = 0, i;
            char c;
            boolean fineElenco = false;
            do {
                for (i = 0; i < righePagina && i + pos < tesserati.size(); i++) {
                    out.println(i + 1 + ") " + tesserati.get(pos + i).toString());
                }
                if (i + pos >= tesserati.size()) {
                    fineElenco = true;
                }
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
                } else if (c == '-' && pos > 0) {
                    pos -= righePagina;
                    if (fineElenco) {
                        fineElenco = false;
                    }
                }
                out.println();
            } while (c != '0');
            boolean flag = false;
            do {
                int scelta = in.readInt("Inserisci il numero d'elenco del tesserato che vuoi eliminare: ");
                if (scelta > pos && scelta <= pos + i) {
                    out.println(tesserati.remove(scelta - 1) + " eliminato/a!");
                    flag = true;
                } else {
                    out.println("Inserimento non valido, riprova!");
                }
            } while (!flag);
        } else {
            out.println("Mi dispiace, non ci sono tesserati!");
        }
    }

    static void modificaElencoCampi(ArrayList<CampoDaGioco> campi, ConsoleInputManager in,
                                    ConsoleOutputManager out) {
        boolean flag = false;
        do {
            out.println("Cosa vuoi fare?");
            out.println("1) Aggiungi dei campi");
            out.println("2) Elimina dei campi");
            int scelta = in.readInt("\nScegli [1 - 2]: ");
            if (scelta == 1 || scelta == 2) {
                flag = true;
                switch (scelta) {
                    case 1 -> aggiungiCampi(campi, in, out);
                    case 2 -> eliminaCampi(campi, in, out);
                }

            } else {
                out.println("Inserimento non valido, riprova!");
            }
        } while (!flag);
        in.readLine("[INVIO] per tornare al menu");
    }

    static void aggiungiCampi(ArrayList<CampoDaGioco> campi, ConsoleInputManager in,
                              ConsoleOutputManager out) {
        boolean continua;
        TipoCampo[] tipi = TipoCampo.values();
        CampoDaGioco campo = null;
        out.println();
        do {
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

    static void eliminaCampi(ArrayList<CampoDaGioco> campi, ConsoleInputManager in,
                             ConsoleOutputManager out) {
    }
}