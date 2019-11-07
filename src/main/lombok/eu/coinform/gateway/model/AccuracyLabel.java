package eu.coinform.gateway.model;

import org.checkerframework.checker.units.qual.A;

public enum AccuracyLabel {
    accurate, accurate_with_considerations, unsubstantiated, inaccurate_with_considerations, inaccurate, not_verifiable;

    static public AccuracyLabel parseString(String string) throws IllegalArgumentException, NullPointerException {
        try {
            return AccuracyLabel.valueOf(AccuracyLabel.class, string);
        } catch (IllegalArgumentException ex) {
            return AccuracyLabel.valueOf(AccuracyLabel.class, string.replaceAll(" ", "_"));
        }
    }

}
