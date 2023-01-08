package centrosportivo;

import java.io.Serializable;

public class Tesserato implements Comparable<Tesserato>, Serializable {
    private int numTessera;
    private final String nome, cognome;

    public Tesserato(int numTessera, String nome, String cognome) {
        this.numTessera = numTessera;
        this.nome = nome;
        this.cognome = cognome;
    }

    public int getNumTessera() {
        return numTessera;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setNumTessera(int numTessera) {
        this.numTessera = numTessera;
    }

    @Override
    public String toString() {
        return numTessera + ": " + nome + " " + cognome;
    }

    @Override
    public int compareTo(Tesserato t) {
        return this.cognome.compareTo(t.cognome);
    }
}