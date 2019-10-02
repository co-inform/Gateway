package eu.coinform.gateway.module;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@ToString
public abstract class ModuleRequestContent {

    public ModuleRequestContent(String callbackBaseUrl) {
          this.transactionId = UUID.randomUUID().toString();
          this.callbackUrl = callbackBaseUrl + transactionId;
    }

    @JsonIgnore
    @Getter
    final private String transactionId;

    @JsonProperty("callback_url")
    @Getter
    final private String callbackUrl;

}
