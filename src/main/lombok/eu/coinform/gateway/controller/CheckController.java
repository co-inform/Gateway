package eu.coinform.gateway.controller;

import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import eu.coinform.gateway.db.RoleEnum;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.db.UsernameAlreadyExistException;
import eu.coinform.gateway.jwt.JwtToken;
import eu.coinform.gateway.model.*;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.rule_engine.RuleEngineConnector;
import eu.coinform.gateway.service.CheckHandler;
import eu.coinform.gateway.service.RedisHandler;
import eu.coinform.gateway.util.Pair;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * The REST Controller defining the endpoints facing towards the users
 */
@RestController
@Slf4j
public class CheckController {

    private final CheckHandler checkHandler;
    private final RedisHandler redisHandler;
    private final UserDbManager userDbManager;
    private final String signatureKey;
    private final RuleEngineConnector ruleEngineConnector;

    CheckController(RedisHandler redisHandler,
                    CheckHandler checkHandler,
                    RuleEngineConnector ruleEngineConnector,
                    UserDbManager userDbManager,
                    @Value("${JWT_KEY}") String signatureKey) {
        this.redisHandler = redisHandler;
        this.checkHandler = checkHandler;
        this.ruleEngineConnector = ruleEngineConnector;
        this.userDbManager = userDbManager;
        this.signatureKey = signatureKey;
    }

    /**
     * The '/twitter/user' endpoint, querying for information about a twitter user.
     * @param twitterUser The twitter user the query is about
     * @return A {@link QueryResponse} containing a 'query_id' uniquely identifying the query.
     */
    //@CrossOrigin("https://twitter.com, chrome://**, chrome-extension://**")
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
    //@CrossOrigin("https://twitter.com, chrome://**, chrome-extension://**")
    @CrossOrigin(origins = "*")
    @JsonView(Views.NoDebug.class)
    @PostMapping("/twitter/tweet")
    public QueryResponse twitterTweet(@Valid @RequestBody Tweet tweet) {
        return queryEndpoint(tweet,
                (aTweet) -> checkHandler.tweetConsumer((Tweet) aTweet));
    }

    //@CrossOrigin("*")
    @RequestMapping(value = "/twitter/tweet", method = RequestMethod.OPTIONS)
    public void corsHeadersTweet(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }


    //todo: known bug. If multiple request are very close to each other in time the gateway can send 2 duplicate queries to the modules.
    private QueryResponse queryEndpoint(QueryObject queryObject, Consumer<QueryObject> queryObjectConsumer) {
        log.trace("query received with query_id '{}'", queryObject.getQueryId());
        long start = System.currentTimeMillis();
        log.trace("{}: query handling start, {}", System.currentTimeMillis() - start, queryObject);
        //response par is a pair {Existant, queryResponse}. Existant is true is there already exists a query response with the queryId specified
        Pair<Boolean, QueryResponse> responsePair = redisHandler.getOrSetIfAbsentQueryResponse(queryObject.getQueryId(),
                new QueryResponse(queryObject.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>(), new LinkedHashMap<>())).join();
        QueryResponse queryResponse = responsePair.getValue();
        log.trace("{}: got query response {}", System.currentTimeMillis() - start, queryResponse);
        if (queryResponse.getStatus() == QueryResponse.Status.done || queryResponse.getStatus() == QueryResponse.Status.partly_done) {
            //todo: We're ignoring the modules. Some logic for when to send them queries must be made.
            // Like if the cache is older than some threshold it is handled as a new query.
            // The information of results directly from cache must also be saved/sent somewhere for the modules to know.
            return queryResponse;
        } else if (!responsePair.getKey()) {
            queryObjectConsumer.accept(queryObject);
            log.trace("{}: query sent of to handler", System.currentTimeMillis() - start);
        }
        return queryResponse;
    }

    /**
     * The 'response/{query_id}' endpoint. It gives back a {@link QueryResponse} with the answer or progress of an earlier query.
     * @param query_id The query_id that identifies the earlier query
     * @return A {@link QueryResponse} containing the answer or at least progress of the query.
     */
    //@CrossOrigin(origins = "https://twitter.com, chrome-extension://kodmajniflhcofdbnfjpkgimbmkpgend")
    @CrossOrigin(origins = "*")
    @JsonView(Views.NoDebug.class)
    @RequestMapping(value = "/response/{query_id}", method = RequestMethod.GET)
    public QueryResponse findById(@PathVariable(value = "query_id", required = true) String query_id) {

        log.trace("query for response received with query_id '{}'", query_id);

        QueryResponse queryResponse = redisHandler.getQueryResponse(query_id).join();

        log.trace("findById: {}", queryResponse);
        return queryResponse;
    }

    //@CrossOrigin("*")
    @JsonView(Views.NoDebug.class)
    @RequestMapping(value = "/response/{query_id}", method = RequestMethod.OPTIONS)
    public void corsHeadersResponse(HttpServletResponse response, @PathVariable(value = "query_id", required = true) String query_id) {
        //response.addHeader("Access-Control-Allow-Origin", "https://twitter.com, chrome://**, chrome-extension://**");
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

        log.trace("findById: {}", queryResponse);

        return queryResponse;
    }

    //@CrossOrigin("*")
    @JsonView(Views.Debug.class)
    @RequestMapping(value = "/response/{query_id}/{debug}", method = RequestMethod.OPTIONS)
    public void corsHeadersResponse(HttpServletResponse response,
                                    @PathVariable(value = "query_id", required = true) String query_id,
                                    @PathVariable(value = "debug", required = true) String debug) {
        //response.addHeader("Access-Control-Allow-Origin", "https://twitter.com, chrome://**, chrome-extension://**");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/twitter/evaluate", method = RequestMethod.POST)
    public EvaluationResponse evaluateTweet(@Valid @RequestBody TweetEvaluation tweetEvaluation) {

        //todo: actually do something with the incoming tweet evaluations
        redisHandler.addToEvaluationList(tweetEvaluation);

        return new EvaluationResponse(tweetEvaluation.getEvaluationId());
    }

    @RequestMapping(value = "/twitter/evaluate", method = RequestMethod.OPTIONS)
    public void corsHeadersEvaluate(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public LoginResponse login() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        String token = (new JwtToken.Builder())
                .setSignatureAlgorithm(SignatureAlgorithm.HS512)
                .setKey(signatureKey)
                .setExpirationTime(7*24*60*60*1000L)
                .setUser(authentication.getName())
                .setRoles(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .build().getToken();
        return new LoginResponse(token);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public void register(@RequestBody @Valid RegisterForm registerForm) throws UsernameAlreadyExistException  {

        List<RoleEnum> roles = new LinkedList<>();
        roles.add(RoleEnum.USER);
        userDbManager.registerUser(registerForm.email, registerForm.password, roles);
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

