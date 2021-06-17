package eu.coinform.gateway.controller.forms;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;

@Data
public class ChangeSettings {
    @NotNull
    boolean research;
    @NotNull
    boolean communication;
    LinkedHashMap<String, Object> config;
}
