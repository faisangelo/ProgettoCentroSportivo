package centrosportivo;

public enum TipoCampo {
    TENNIS {
        @Override
        public String toString() {
            return "Tennis";
        }
    },
    BASKET {
        @Override
        public String toString() {
            return "Basket";
        }
    },
    CALCIOTTO {
        @Override
        public String toString() {
            return "Calciotto";
        }
    },
    CALCETTO {
        @Override
        public String toString() {
            return "Calcetto";
        }
    },
    PADEL {
        @Override
        public String toString() {
            return "Padel";
        }
    }
}
