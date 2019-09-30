package eu.coinform.gateway.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode
@ToString
public class Pair<K, V> {

    @Getter
    private K key;
    @Getter
    private V value;


    public Pair(@NotNull final K inK, @NotNull final V inV){
        this.key = inK;
        this.value = inV;
    }


}
