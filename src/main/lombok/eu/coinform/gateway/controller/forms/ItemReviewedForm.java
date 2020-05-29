package eu.coinform.gateway.controller.forms;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class ItemReviewedForm implements Serializable {

    @NotEmpty
    String type;

    @NotEmpty
    String url;

}
