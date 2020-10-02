package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFeedback implements Serializable {

    private Response response;
}
