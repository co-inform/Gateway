package eu.coinform.gateway.controller.forms;

import eu.coinform.gateway.module.iface.FactChecker;
import eu.coinform.gateway.module.iface.ItemToReview;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Data
@Getter
@Setter
//@NoArgsConstructor
public class RecordRequestForm implements Serializable {

    private final List<FactChecker> factcheckers;
    private final ItemToReview itemToReview;

}
