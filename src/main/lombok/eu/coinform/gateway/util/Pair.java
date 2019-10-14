package eu.coinform.gateway.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A key-value Pair
 * @param <K> the Type of the key
 * @param <V> the Type of the value
 */
@EqualsAndHashCode
@ToString
public class Pair<K extends Serializable, V extends Serializable> implements Serializable {

    /**
     * -- GETTER --
     * Get the key
     *
     * @return The key
     * -- SETTER --
     * Set the key
     *
     * @param key The key
     */
    @Setter
    @Getter
    private K key;
    /**
     * -- GETTER --
     * Get the value
     *
     * @return The value
     * -- SETTER --
     * Set the value
     *
     * @param value The value
     */
    @Setter
    @Getter
    private V value;

    /**
     * Creates a key-value Pair
     * @param inK The key
     * @param inV The value
     */
    public Pair(@NotNull K inK, @NotNull V inV){
        this.key = inK;
        this.value = inV;
    }

    /**
     * Creates an empty key-value Pair
     */
    public Pair(){}

    /**
     * Static method for creating a Pair on the fly
     *
     * @param inK the type passed in must not be null
     * @param inV the type passed in must not be null
     * @param <K> the type passed in must implement serializable
     * @param <V> the type passed in must implement serializable
     * @return a Pair of the parameters passed in
     */
    static public <K extends Serializable, V extends Serializable> Pair<K,V> of(@NotNull K inK, @NotNull V inV){
        return new Pair<>(inK,inV);
    }

}
