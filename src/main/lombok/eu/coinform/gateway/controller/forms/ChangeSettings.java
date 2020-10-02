package eu.coinform.gateway.controller.forms;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChangeSettings {
    @NotNull
    boolean research;
    @NotNull
    boolean communication;
}
