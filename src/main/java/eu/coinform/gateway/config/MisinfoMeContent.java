package eu.coinform.gateway.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.coinform.gateway.module.ModuleRequestContent;

import java.util.LinkedHashMap;

public class MisinfoMeContent extends ModuleRequestContent {

    MisinfoMeContent(String callbackBaseUrl) {
        super(callbackBaseUrl);
    }

    @JsonProperty("some_content")
    private LinkedHashMap<String, Object> someContent;
}
