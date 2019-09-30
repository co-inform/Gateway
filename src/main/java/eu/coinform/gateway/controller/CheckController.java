package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.*;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.service.CheckHandler;
import eu.coinform.gateway.service.RedisHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

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

    @PostMapping("/twitter/user")
    public Resource<QueryResponse> twitterUser(@Valid @RequestBody TwitterUser twitterUser) {
        return queryEndpoint(twitterUser,
               (aTwitterUser) -> checkHandler.twitterUserConsumer((TwitterUser) aTwitterUser));
    }

    @PostMapping("/twitter/tweet")
    public Resource<QueryResponse> twitterTweet(@Valid @RequestBody Tweet tweet) {
        return queryEndpoint(tweet,
                (aTweet) -> checkHandler.tweetConsumer((Tweet) aTweet));
    }

    private Resource<QueryResponse> queryEndpoint(QueryObject queryObject, Consumer<QueryObject> queryObjectConsumer) {
        CompletableFuture<QueryResponse> queryResponseFuture;
        queryResponseFuture = redisHandler.getQueryResponse(queryObject.getQueryId()).exceptionally((throwable ->
                    redisHandler.setQueryResponse(queryObject.getQueryId(),
                        new QueryResponse(queryObject.getQueryId(), QueryResponse.Status.in_progress, null)).join()
            ));
        QueryResponse queryResponse = queryResponseFuture.join();
        if (queryResponse.getStatus() == QueryResponse.Status.done) {
            //todo: We're ignoring the modules. Some logic for when to send them queries must be made.
            // Like if the cache is older than some threshold it is handled as a new query.
            // The information of results directly from cache must also be saved/sent somewhere for the modules to know.
            return assembler.toResource(queryResponse);
        }
        queryObjectConsumer.accept(queryObject);
        return assembler.toResource(queryResponse);
    }

    @GetMapping("/response/{query_id}")
    public Resource<QueryResponse> findById(@PathVariable(value = "query_id", required = true) String id) {
        QueryResponse queryResponse = redisHandler.getQueryResponse(id).join();
        return new Resource<>(queryResponse,
                linkTo(methodOn(CheckController.class).findById(id)).withSelfRel());
    }
}

