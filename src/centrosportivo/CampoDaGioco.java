package centrosportivo;

import java.io.Serializable;

public class CampoDaGioco implements Serializable {
    private int codCampo, maxGiocatori;
    private TipoCampo tipo;

    public CampoDaGioco(int codCampo, TipoCampo tipo) {
        this.codCampo = codCampo;
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

    public void setCodCampo(int codCampo) {
        this.codCampo = codCampo;
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