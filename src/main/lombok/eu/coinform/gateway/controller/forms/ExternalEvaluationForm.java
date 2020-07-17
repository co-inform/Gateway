package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Date dateCreated;
}
