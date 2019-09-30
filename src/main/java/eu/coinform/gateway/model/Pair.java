package eu.coinform.gateway.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode
@ToString
public class Pair<K extends Serializable, V extends Serializable> implements Serializable {

    @Getter
    private K key;
    @Getter
    private V value;


    public Pair(@NotNull final K inK, @NotNull final V inV){
        this.key = inK;
        this.value = inV;
    }

    static public <K extends Serializable, V extends Serializable> Pair<K,V> of(@NotNull K inK, @NotNull V inV){
        return new Pair<>(inK,inV);
    }

}
