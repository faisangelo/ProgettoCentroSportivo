package centrosportivo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Prenotazione implements Comparable<Prenotazione>, Serializable {
    private CampoDaGioco campo;
    private Date data;
    private int oraInizio;
    private ArrayList<Tesserato> giocatori;

    public Prenotazione(CampoDaGioco campo, Date data, int oraInizio, ArrayList<Tesserato> giocatori) {
        this.campo = campo;
        this.data = data;
        this.oraInizio = oraInizio;
        this.giocatori = giocatori;
    }

    public CampoDaGioco getCampo() {
        return campo;
    }

    public boolean addGiocatore(Tesserato t){
        if (giocatori.size() < campo.getMaxGiocatori()){
            return giocatori.add(t);
        }
        return false;
    }

    public boolean removeGiocatore(Tesserato t){
        return giocatori.remove(t);
    }

    public Date getData() {
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

    public void setData(Date data) {
        this.data = data;
    }

    public void setOraInizio(int oraInizio) {
        this.oraInizio = oraInizio;
    }

    @Override
    public String toString() {
        return "Prenotazione effettuata da: " + giocatori.toString() +
                ", Campo: " + campo.toString() +
                ", Data e ora di inizio: " + data.toString() + " alle " + oraInizio;
    }

    @Override
    public int compareTo(Prenotazione p){
        return this.data.compareTo(p.data);
    }
}