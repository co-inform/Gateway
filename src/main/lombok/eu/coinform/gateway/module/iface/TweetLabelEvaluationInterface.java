package eu.coinform.gateway.module.iface;

import eu.coinform.gateway.controller.TweetLabelEvaluation;
import eu.coinform.gateway.module.ModuleRequest;
import java.util.List;
import java.util.function.BiFunction;


public interface TweetLabelEvaluationInterface {

    List<BiFunction<TweetLabelEvaluation, String, ModuleRequest>> tweetLabelEvaluationRequest();

}
