package eu.coinform.gateway.controller.forms;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
public class PluginEvaluationLog {
    @NotNull
    Date log_time;
    @NotEmpty
    String log_category;
    @NotEmpty
    @URL (message = "related_item_url must be a valid url")
    String related_item_url;
    @NotEmpty
    String related_item_data;
    @NotEmpty
    String log_action;
}
