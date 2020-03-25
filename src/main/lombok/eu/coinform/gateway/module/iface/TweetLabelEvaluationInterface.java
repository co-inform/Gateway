package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.controller.TweetLabelEvaluation;
import eu.coinform.gateway.module.ModuleRequest;

import java.util.List;
import java.util.function.Function;

public interface TweetLabelEvaluationInterface {

    List<Function<TweetLabelEvaluation, ModuleRequest>> tweetLabelEvaluationRequest();

}
