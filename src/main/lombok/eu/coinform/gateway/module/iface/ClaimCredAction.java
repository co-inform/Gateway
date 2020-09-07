package eu.coinform.gateway.module.iface;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.coinform.gateway.controller.forms.PluginEvaluationLog;
import eu.coinform.gateway.db.entity.SessionToken;
import lombok.Data;
import lombok.Getter;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

public class ClaimCredAction implements Serializable {

    @Getter
    String user_id;
    @Getter
    String plugin_version;
    @Getter
    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    Date log_time;
    @Getter
    @NotEmpty
    String log_category;
    @Getter
    @NotEmpty
    @URL(message = "related_item_url must be a valid url")
    String related_item_url;
    @Getter
    @NotEmpty
    String related_item_data;
    @Getter
    @NotEmpty
    String log_action;

    public ClaimCredAction(PluginEvaluationLog pel, SessionToken st) {
        user_id = st.getUser().getUuid();
        plugin_version = st.getPluginVersion();
        log_time = pel.getLog_time();
        log_category = pel.getLog_category();
        related_item_url = pel.getRelated_item_url();
        related_item_data = pel.getRelated_item_data();
        log_action = pel.getLog_action();
    }
}
