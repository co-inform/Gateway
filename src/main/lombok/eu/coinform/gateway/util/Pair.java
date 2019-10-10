package eu.coinform.gateway.util;

import lombok.EqualsAndHashCode;
import lombok.Getter;
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
     */
    @Getter
    private K key;
    /**
     * -- GETTER --
     * Get the value
     *
     * @return The value
     */
    @Getter
    private V value;

    /**
     * Creates a key-value Pair
     * @param inK The key
     * @param inV The value
     */
    public Pair(@NotNull final K inK, @NotNull final V inV){
        this.key = inK;
        this.value = inV;
    }

    static public <K extends Serializable, V extends Serializable> Pair<K,V> of(@NotNull K inK, @NotNull V inV){
        return new Pair<>(inK,inV);
    }

}
