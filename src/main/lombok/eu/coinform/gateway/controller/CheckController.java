package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.*;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.service.CheckHandler;
import eu.coinform.gateway.service.RedisHandler;
import eu.coinform.gateway.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * The REST Controller defining the endpoints facing towards the users
 */
@RestController
@Slf4j
public class CheckController {

    private final QueryResponseAssembler assembler;
    private final CheckHandler checkHandler;
    private final RedisHandler redisHandler;

    CheckController(RedisHandler redisHandler,
                    QueryResponseAssembler assembler,
                    CheckHandler checkHandler
    ) {
        this.redisHandler = redisHandler;
        this.assembler = assembler;
        this.checkHandler = checkHandler;
    }

    /**
     * The '/twitter/user' endpoint, querying for information about a twitter user.
     * @param twitterUser The twitter user the query is about
     * @return A {@link QueryResponse} containing a 'query_id' uniquely identifying the query.
     */
    //@CrossOrigin("https://twitter.com, chrome://**, chrome-extension://**")
    @CrossOrigin(origins = "*")
    @PostMapping("/twitter/user")
    public Resource<QueryResponse> twitterUser(@Valid @RequestBody TwitterUser twitterUser) {
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
    @PostMapping("/twitter/tweet")
    public Resource<QueryResponse> twitterTweet(@Valid @RequestBody Tweet tweet) {
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


    private Resource<QueryResponse> queryEndpoint(QueryObject queryObject, Consumer<QueryObject> queryObjectConsumer) {
        log.trace("query received with query_id '{}'", queryObject.getQueryId());
        long start = System.currentTimeMillis();
        log.trace("{}: query handling start, {}", System.currentTimeMillis() - start, queryObject);
        Pair<Boolean, QueryResponse> responsePair = redisHandler.getOrSetIfAbsentQueryResponse(queryObject.getQueryId(),
                new QueryResponse(queryObject.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>())).join();
        QueryResponse queryResponse = responsePair.getValue();
        log.trace("{}: got query response {}", System.currentTimeMillis() - start, queryResponse);
        if (queryResponse.getStatus() == QueryResponse.Status.done) {
            //todo: We're ignoring the modules. Some logic for when to send them queries must be made.
            // Like if the cache is older than some threshold it is handled as a new query.
            // The information of results directly from cache must also be saved/sent somewhere for the modules to know.
            return assembler.toResource(queryResponse);
        } else if (!responsePair.getKey()) {
            queryObjectConsumer.accept(queryObject);
            log.trace("{}: query sent of to hander", System.currentTimeMillis() - start);
        }
        return assembler.toResource(queryResponse);
    }

    /**
     * The 'response/{query_id}' endpoint. It gives back a {@link QueryResponse} with the answer or progress of an earlier query.
     * @param query_id The query_id that identifies the earlier query
     * @return A {@link QueryResponse} containing the answer or at least progress of the query.
     */
    //@CrossOrigin(origins = "https://twitter.com, chrome-extension://kodmajniflhcofdbnfjpkgimbmkpgend")
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/response/{query_id}", method = RequestMethod.GET)
    public Resource<QueryResponse> findById(@PathVariable(value = "query_id", required = true) String query_id) {
        log.trace("query for response reveived with query_id '{}'", query_id);
        QueryResponse queryResponse = redisHandler.getQueryResponse(query_id).join();
        return new Resource<>(queryResponse,
                linkTo(methodOn(CheckController.class).findById(query_id)).withSelfRel());
    }

    //@CrossOrigin("*")
    @RequestMapping(value = "/response/{query_id}", method = RequestMethod.OPTIONS)
    public void corsHeadersResponse(HttpServletResponse response, @PathVariable(value = "query_id", required = true) String query_id) {
        //response.addHeader("Access-Control-Allow-Origin", "https://twitter.com, chrome://**, chrome-extension://**");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/twitter/evaluate", method = RequestMethod.POST)
    public Resource<EvaluationResponse> evaluateTweet(@Valid @RequestBody TweetEvaluation tweetEvaluation) {

        //todo: actually do something with the incoming tweet evaluations
        return new Resource<>(new EvaluationResponse(tweetEvaluation.getEvaluationId()));
    }

    @RequestMapping(value = "/twitter/evaluate", method = RequestMethod.OPTIONS)
    public void corsHeadersEvaluate(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

}

