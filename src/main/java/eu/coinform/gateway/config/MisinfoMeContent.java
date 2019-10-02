package eu.coinform.gateway.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.coinform.gateway.module.ModuleRequestContent;
import lombok.ToString;

import java.util.LinkedHashMap;

public class MisinfoMeContent extends ModuleRequestContent {

    MisinfoMeContent(String callbackBaseUrl) {
        super(callbackBaseUrl);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
