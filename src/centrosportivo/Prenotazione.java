package centrosportivo;

import prog.utili.Data;

import java.io.Serializable;
import java.util.ArrayList;

public class Prenotazione implements Comparable<Prenotazione>, Serializable {
    private CampoDaGioco campo;
    private Data data;
    private int oraInizio;
    private ArrayList<Tesserato> giocatori;

    public Prenotazione(CampoDaGioco campo, Data data, int oraInizio) {
        this.campo = campo;
        this.data = data;
        this.oraInizio = oraInizio;
        giocatori = new ArrayList<>();
    }

    public Prenotazione(Prenotazione altra) {
        campo = altra.campo;
        data = altra.data;
        oraInizio = altra.oraInizio;
        giocatori = altra.giocatori;
    }

    public CampoDaGioco getCampo() {
        return campo;
    }

    public boolean addGiocatore(Tesserato t) {
        if (giocatori.size() < campo.getMaxGiocatori() && !giocatori.contains(t)) {
            return giocatori.add(t);
        }
        return false;
    }

    public void removeGiocatori() {
        giocatori.clear();
    }

    public Data getData() {
        return data;
    }

    public int getOraInizio() {
        return oraInizio;
    }

    public ArrayList<Tesserato> getGiocatori() {
        return giocatori;
    }

    public void setCampo(CampoDaGioco campo) {
        this.campo = campo;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public void setOraInizio(int oraInizio) {
        this.oraInizio = oraInizio;
    }

    @Override
    public String toString() {
        return "Prenotazione effettuata da: " + giocatori.toString() +
                ", Campo " + campo.toString() +
                ", Data e ora di inizio: " + data.toString() + " alle " + oraInizio;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        if (this.data.equals(((Prenotazione) obj).data)){
            if (this.campo.getCodCampo() == ((Prenotazione) obj).campo.getCodCampo()){
                return this.oraInizio == ((Prenotazione) obj).oraInizio;
            }
        }
        return false;
    }

    @Override
    public int compareTo(Prenotazione p) {
        return this.data.compareTo(p.data);
    }

    public enum Ordinamento {
        DATA {
            @Override
            public String toString() {
                return "Data";
            }
        },
        GIOCATORE1 {
            @Override
            public String toString() {
                return "Giocatore 1";
            }
        },
        TIPOCAMPO {
            @Override
            public String toString() {
                return "Tipo campo";
            }
        }
    }
}