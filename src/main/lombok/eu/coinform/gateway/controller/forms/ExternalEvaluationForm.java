package eu.coinform.gateway.controller.forms;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;

@Data
public class ExternalEvaluationForm implements Serializable {

    @NotEmpty
    String context;

    @NotEmpty
    String type;

    @NotEmpty
    String url;

    @NotEmpty
    AuthorForm author;

    @NotEmpty
    String text;

    @NotEmpty
    String name;

    @NotEmpty
    ReviewRatingForm reviewRating;

    @NotEmpty
    ItemReviewedForm itemReviewed;

    @NotEmpty
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    Date dateCreated;
}
