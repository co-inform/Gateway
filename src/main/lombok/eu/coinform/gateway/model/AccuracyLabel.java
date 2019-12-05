package eu.coinform.gateway.model;

public enum AccuracyLabel {
    credible, mostly_credible, credibility_uncertain, mostly_not_credible, not_credible, not_verifiable;

    static public AccuracyLabel parseString(String string) throws IllegalArgumentException, NullPointerException {
        try {
            return AccuracyLabel.valueOf(AccuracyLabel.class, string);
        } catch (IllegalArgumentException ex) {
            return AccuracyLabel.valueOf(AccuracyLabel.class, string.replaceAll(" ", "_"));
        }
    }

    @Override
    public String toString() {
        return this.name().replaceAll("_", " ");
    }

}
