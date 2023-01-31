package centrosportivo;

import java.io.Serializable;

public class Tesserato implements Comparable<Tesserato>, Serializable {
    private final int numTessera;
    private final String nome, cognome;
    private static int proxNumTessera = 1;

    public Tesserato(String nome, String cognome) {
        this.numTessera = proxNumTessera++;
        this.nome = nome;
        this.cognome = cognome;
    }

    public static int getProxNumTessera(){
        return proxNumTessera;
    }

    public static void setProxNumTessera(int proxNumTessera){
        Tesserato.proxNumTessera = proxNumTessera;
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
        return "#" + numTessera + ": " + cognome + " " + nome;
    }

    @Override
    public int compareTo(Tesserato t) {
        return this.cognome.compareToIgnoreCase(t.cognome);
    }

    public enum Ordinamento {
        COGNOME {
            @Override
            public String toString() {
                return "Cognome";
            }
        },
        NOME {
            @Override
            public String toString() {
                return "Nome";
            }
        },
        NUMEROTESSERA {
            @Override
            public String toString() {
                return "Numero tessera";
            }
        }
    }
}