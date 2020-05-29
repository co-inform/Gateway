package eu.coinform.gateway.controller.forms;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class ReviewRatingForm {

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
