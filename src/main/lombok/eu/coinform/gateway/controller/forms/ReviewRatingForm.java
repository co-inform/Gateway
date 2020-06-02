package eu.coinform.gateway.controller.forms;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
public class ReviewRatingForm implements Serializable {

    @NotEmpty
    String context;

    @NotEmpty
    String type;

    @NotEmpty
    String ratingValue;

    @NotEmpty
    List<String> possibleRatingValues;

    @NotEmpty
    String reviewAspect;

    @NotEmpty
    String reviewExplanation;

}
