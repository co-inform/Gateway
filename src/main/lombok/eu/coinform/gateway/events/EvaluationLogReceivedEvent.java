package eu.coinform.gateway.events;

import eu.coinform.gateway.controller.forms.PluginEvaluationLog;
import eu.coinform.gateway.db.entity.SessionToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class EvaluationLogReceivedEvent {

    @Getter
    List<PluginEvaluationLog> evaluationLogList;
    @Getter
    SessionToken sessionToken;

}
