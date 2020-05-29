package eu.coinform.gateway.controller.forms;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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

    @NotNull
    AuthorForm author;

    @NotEmpty
    String text;

    @NotEmpty
    String name;

    @NotNull
    ReviewRatingForm reviewRating;

    @NotNull
    ItemReviewedForm itemReviewed;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    Date dateCreated;
}
