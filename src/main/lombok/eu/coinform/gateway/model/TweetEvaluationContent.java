package eu.coinform.gateway.model;


import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class TweetEvaluationContent implements Serializable {

    @NotEmpty(message = "A tweet evaluation must contain a accuracy label")
    @JsonView(Views.NoDebug.class)
    private AccuracyLabel label;

    @NotEmpty(message = "A tweet evaluation must contain a url")
    @JsonView(Views.NoDebug.class)
    @URL(message = "A tweet evaluation must contain a valid url")
    private String url;

    @JsonView(Views.NoDebug.class)
    private String comment;

}
