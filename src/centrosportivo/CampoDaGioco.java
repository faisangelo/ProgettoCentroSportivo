package centrosportivo;

import java.io.Serializable;

public class CampoDaGioco implements Comparable<CampoDaGioco>, Serializable {
    private final int codCampo;
    private  int maxGiocatori;
    private final TipoCampo tipo;
    private static Integer proxCodCampo;

    public CampoDaGioco(TipoCampo tipo) {
        if (proxCodCampo == null) {
            proxCodCampo = 1;
        }
        this.codCampo = proxCodCampo++;
        this.tipo = tipo;
        switch (this.tipo) {
            case TENNIS -> maxGiocatori = 2;
            case PADEL -> maxGiocatori = 4;
            case BASKET, CALCETTO -> maxGiocatori = 10;
            case CALCIOTTO -> maxGiocatori = 16;
        }
    }

    public int getCodCampo() {
        return codCampo;
    }

    public TipoCampo getTipo() {
        return tipo;
    }

    public int getMaxGiocatori() {
        return maxGiocatori;
    }

    @Override
    public int compareTo(CampoDaGioco c) {
        return Integer.compare(this.codCampo, c.codCampo);
    }

    @Override
    public String toString() {
        return codCampo + ": " + tipo;
    }

    public enum Ordinamento {
        CODCAMPO {
            @Override
            public String toString() {
                return "Codice campo";
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