package eu.coinform.gateway.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.cache.ModuleResponse;
import eu.coinform.gateway.cache.Views;
import eu.coinform.gateway.controller.forms.TweetEvaluationForm;
import eu.coinform.gateway.controller.forms.TweetLabelEvaluationForm;
import eu.coinform.gateway.controller.restclient.RestClient;
import eu.coinform.gateway.db.User;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.events.UserLabelReviewEvent;
import eu.coinform.gateway.events.UserTweetEvaluationEvent;
import eu.coinform.gateway.model.*;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.module.iface.AccuracyEvaluationImplementation;
import eu.coinform.gateway.module.iface.LabelEvaluationImplementation;
import eu.coinform.gateway.rule_engine.RuleEngineConnector;
import eu.coinform.gateway.service.CheckHandler;
import eu.coinform.gateway.service.RedisHandler;
import eu.coinform.gateway.util.ErrorResponse;
import eu.coinform.gateway.util.SuccesfullResponse;
import eu.coinform.gateway.util.RuleEngineHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.function.Consumer;

/**
 * The REST Controller defining the endpoints facing towards the users
 */
@RestController
@Slf4j
public class CheckController {

    private final CheckHandler checkHandler;
    private final RedisHandler redisHandler;
    private final RuleEngineConnector ruleEngineConnector;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDbManager userDbManager;
    private final ObjectMapper objectMapper;

//    @Value("${misinfome.server.scheme}://${misinfome.server.url}${misinfome.server.base_endpoint}/credibility/sources/?source=%s")
//    private String misInfoMeUrl;

    CheckController(RedisHandler redisHandler,
                    CheckHandler checkHandler,
                    RuleEngineConnector ruleEngineConnector,
                    UserDbManager userDbManager,
                    ApplicationEventPublisher eventPublisher) {
        this.redisHandler = redisHandler;
        this.checkHandler = checkHandler;
        this.ruleEngineConnector = ruleEngineConnector;
        this.userDbManager = userDbManager;
        this.eventPublisher = eventPublisher;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * The '/twitter/user' endpoint, querying for information about a twitter user.
     * @param twitterUser The twitter user the query is about
     * @return A {@link QueryResponse} containing a 'query_id' uniquely identifying the query.
     */
    @CrossOrigin(origins = "*")
    @PostMapping("/twitter/user")
    public QueryResponse twitterUser(@Valid @RequestBody TwitterUser twitterUser) {
        return queryEndpoint(twitterUser,
               (aTwitterUser) -> checkHandler.twitterUserConsumer((TwitterUser) aTwitterUser));
    }

    @RequestMapping(value = "/twitter/user", method = RequestMethod.OPTIONS)
    public void corsHeadersTwitterUser(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    /**
     * The 'twitter/tweet' endpoint, querying for information about a tweet.
     * @param tweet The tweet the query is about.
     * @return A {@link QueryResponse} containing a 'query_id' uniquely identifying the query.
     */
    @CrossOrigin(origins = "*")
    @JsonView(Views.NoDebug.class)
    @PostMapping("/twitter/tweet")
    public QueryResponse twitterTweet(@Valid @RequestBody Tweet tweet) {
        return queryEndpoint(tweet,
                (aTweet) -> checkHandler.tweetConsumer((Tweet) aTweet));
    }

    @RequestMapping(value = "/twitter/tweet", method = RequestMethod.OPTIONS)
    public void corsHeadersTweet(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }


    private QueryResponse queryEndpoint(QueryObject queryObject, Consumer<QueryObject> queryObjectConsumer) {
        log.trace("query received with query_id '{}'", queryObject.getQueryId());
        QueryResponse qrIfAbsent = new QueryResponse(queryObject.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>(), new LinkedHashMap<>());
        QueryResponse response = redisHandler.getQueryResponse(queryObject.getQueryId(), qrIfAbsent).join();
        if (response.getVersionHash() == qrIfAbsent.getVersionHash()) {
            //We only send out new requests for new Queries
            queryObjectConsumer.accept(queryObject);
        }
        return response;
    }

    /**
     * The 'response/{query_id}' endpoint. It gives back a {@link QueryResponse} with the answer or progress of an earlier query.
     * @param query_id The query_id that identifies the earlier query
     * @return A {@link QueryResponse} containing the answer or at least progress of the query.
     */
    @CrossOrigin(origins = "*")
    @JsonView(Views.NoDebug.class)
    @RequestMapping(value = "/response/{query_id}", method = RequestMethod.GET)
    public QueryResponse findById(@PathVariable(value = "query_id", required = true) String query_id) {

        log.trace("query for response received with query_id '{}'", query_id);

        QueryResponse queryResponse = redisHandler.getQueryResponse(query_id).join();

        log.trace("findById: {}", queryResponse);
        return queryResponse;
    }

    @JsonView(Views.NoDebug.class)
    @RequestMapping(value = "/response/{query_id}", method = RequestMethod.OPTIONS)
    public void corsHeadersResponse(HttpServletResponse response, @PathVariable(value = "query_id", required = true) String query_id) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    @CrossOrigin(origins = "*")
    @JsonView(Views.Debug.class)
    @RequestMapping(value = "/response/{query_id}/{debug}", method = RequestMethod.GET)
    public QueryResponse findById(@PathVariable(value = "query_id", required = true) String query_id, @PathVariable(value = "debug", required = true) String debug) {

        log.trace("query for response received with query_id '{}'", query_id);

        QueryResponse queryResponse = redisHandler.getQueryResponse(query_id).join();
        Map<String, ModuleResponse> moduleResponses = redisHandler.getModuleResponses(query_id).join();
        LinkedHashMap<String, Object> flattenedModuleResponses = new LinkedHashMap();
        for (Map.Entry<String, ModuleResponse> entry : moduleResponses.entrySet()) {
            queryResponse.getResponse().put(entry.getKey(), entry.getValue());
            RuleEngineHelper.flatResponseMap(entry.getValue(), flattenedModuleResponses, entry.getKey().toLowerCase(), "_");
        }

        log.trace("findById: {}", queryResponse);

        return queryResponse;
    }

    @JsonView(Views.Debug.class)
    @RequestMapping(value = "/response/{query_id}/{debug}", method = RequestMethod.OPTIONS)
    public void corsHeadersResponse(HttpServletResponse response,
                                    @PathVariable(value = "query_id", required = true) String query_id,
                                    @PathVariable(value = "debug", required = true) String debug) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/twitter/evaluate", method = RequestMethod.POST)
    public ResponseEntity<?> evaluateTweet(@Valid @RequestBody TweetEvaluationForm tweetEvaluationForm) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        Optional<User> oUser = userDbManager.getUserById(userId);

        if(oUser.isEmpty()){
            log.debug("No user: {}, {}", userId, authentication.getPrincipal());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.NOSUCHUSER);
        }

        eventPublisher.publishEvent(new UserTweetEvaluationEvent(new AccuracyEvaluationImplementation(tweetEvaluationForm, oUser.get().getUuid())));
        return ResponseEntity.ok(SuccesfullResponse.EVALUATETWEET);
    }

    @RequestMapping(value = "/twitter/evaluate", method = RequestMethod.OPTIONS)
    public void corsHeadersEvaluate(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    @RequestMapping(value = "/twitter/evaluate/label", method = RequestMethod.POST)
    public ResponseEntity<?> evaluateLabel(@Valid @RequestBody TweetLabelEvaluationForm tweetLabelEvaluationForm) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Long userId = (Long) authentication.getPrincipal();
        Optional<User> oUser = userDbManager.getUserById(userId);

        if(oUser.isEmpty()){
            log.debug("No user: {}, {}", userId, authentication.getPrincipal());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.NOSUCHUSER);
        }
        eventPublisher.publishEvent(new UserLabelReviewEvent(new LabelEvaluationImplementation(tweetLabelEvaluationForm, oUser.get().getUuid())));
        return ResponseEntity.ok(SuccesfullResponse.EVALUATELABEL);
    }


    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/check-url", method = RequestMethod.GET)
    public ResponseEntity<?> checkUrl(@RequestParam(value = "source") String source){

        if(!validUrl(source)){
            return ResponseEntity.badRequest().body(String.format(ErrorResponse.FORMATTED.getError(),"URL: " + source));
        }
        String misInfoMeUrl = "https://socsem.kmi.open.ac.uk/misinfo/api/credibility/sources/?source=%s";

        HttpResponse<String> status;
        try {
            RestClient client = new RestClient(HttpMethod.GET,URI.create(String.format(misInfoMeUrl, source)),"");
            status = client.sendRequest().join();
            if(status.statusCode() < 200 || status.statusCode() > 299){
                log.debug("Http error: {}", status);
                return ResponseEntity.status(status.statusCode()).body(status.body());
            }
            LinkedHashMap<String, Object> answer = objectMapper.readValue(status.body(), LinkedHashMap.class);
            return ResponseEntity.ok(checkUrlRuleEngine(answer));

        } catch (InterruptedException | IOException e) {
            log.debug("Something went wrong: {}", e.getMessage());
            return ResponseEntity.badRequest().body(String.format(ErrorResponse.FORMATTED.getError(), e.getMessage()));
        }
    }

    private LinkedHashMap<String, Object> checkUrlRuleEngine(LinkedHashMap<String, Object> misinfomeAnswer) {
        LinkedHashMap<String, Object> flatMap = new LinkedHashMap<>();
        ModuleResponse moduleResponse = new ModuleResponse();
        moduleResponse.setResponse(misinfomeAnswer);
        RuleEngineHelper.flatResponseMap(moduleResponse, flatMap, "misinfome", "_");
        Set<String> modules = new HashSet<>();
        modules.add("misinfome");
        return ruleEngineConnector.evaluateResults(flatMap, modules);
    }

    private boolean validUrl(String url){
        try{
            new URL(url).toURI().parseServerAuthority();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            log.debug("Invalid url: {}", url);
            return false;
        }
    }

    @RequestMapping(value = "/ruleengine/test", method = RequestMethod.POST)
    public LinkedHashMap<String, Object> ruleEngineCheck(@Valid @RequestBody RuleEngineTestInput ruleEngineTestInput) {
        LinkedHashMap<String, Object> moduleResponses = new LinkedHashMap<>();
        moduleResponses.put("misinfome_credibility_value", ruleEngineTestInput.getMisinfoMe().getCred());
        moduleResponses.put("misinfome_credibility_confidence", ruleEngineTestInput.getMisinfoMe().getCred());
        moduleResponses.put("contentanalysis_credibility", ruleEngineTestInput.getStance().getCred());
        moduleResponses.put("contentanalysis_confidence", ruleEngineTestInput.getStance().getConf());
        moduleResponses.put("claimcredibility_tweet_claim_credibility_0_credibility", ruleEngineTestInput.getClaimCredibility().getCred());
        moduleResponses.put("claimcredibility_tweet_claim_credibility_0_confidence", ruleEngineTestInput.getClaimCredibility().getConf());

        Set<String> modules = new HashSet<>();
        modules.add("misinfome");
        modules.add("claimcredibility");
        modules.add("contentanalysis");

        return ruleEngineConnector.evaluateResults(moduleResponses, modules);
    }

}

