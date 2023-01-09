package centrosportivo;

import java.io.Serializable;

public class Tesserato implements Comparable<Tesserato>, Serializable {
    private final int numTessera;
    private final String nome, cognome;
    private static Integer proxNumTessera;

    public Tesserato(String nome, String cognome) {
        if (proxNumTessera == null) {
            proxNumTessera = 1;
        }
        this.numTessera = proxNumTessera++;
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

    @Override
    public String toString() {
        return numTessera + ": " + nome + " " + cognome;
    }

    @Override
    public int compareTo(Tesserato t) {
        return this.cognome.compareTo(t.cognome);
    }
}