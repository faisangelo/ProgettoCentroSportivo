package centrosportivo;

import java.io.Serializable;

public class CampoDaGioco implements Serializable {
    private final int codCampo;
    private int maxGiocatori;
    private TipoCampo tipo;
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

    public void setTipo(TipoCampo tipo) {
        this.tipo = tipo;
    }

    public void setMaxGiocatori(int maxGiocatori) {
        this.maxGiocatori = maxGiocatori;
    }

    @Override
    public String toString() {
        return codCampo + ": " + tipo;
    }
}