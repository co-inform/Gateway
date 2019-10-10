package eu.coinform.gateway.module;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

/**
 * ModuleRequestContent holds the information for the Module of where to post back the answer to a query
 */
@ToString
public abstract class ModuleRequestContent {

    /**
     * The constructor of ModuleRequestContent. Takes a string as parameter holding the base callback Url for the module
     * Also sets the transactionId which is a UUID and adds it to the basUrl and sets the callbackUrl which in turn is
     * sent to the module.
     *
     * @param callbackBaseUrl a String holding the base url for the Modules callback
     */
    public ModuleRequestContent(String callbackBaseUrl) {
          this.transactionId = UUID.randomUUID().toString();
          this.callbackUrl = callbackBaseUrl + transactionId;
          this.callbackBaseUrl = callbackBaseUrl;
    }

    @JsonIgnore
    @Getter
    final private String transactionId;

    @JsonProperty("callback_url")
    @Getter
    final private String callbackUrl;

    @JsonIgnore
    @Getter
    final private String callbackBaseUrl;

}
