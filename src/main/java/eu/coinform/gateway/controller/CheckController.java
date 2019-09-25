package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.*;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.service.CheckHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@Slf4j
public class CheckController {

    private final RedisTemplate<String, QueryResponse> template;
    private final RequestResourceAssembler assembler;
    private final CheckHandler checkHandler;

    CheckController(@Qualifier("redisQueryTemplate") RedisTemplate<String, QueryResponse> template,
                    RequestResourceAssembler assembler,
                    CheckHandler checkHandler
    ) {
        this.template = template;
        this.assembler = assembler;
        this.checkHandler = checkHandler;
    }

    @PostMapping("/twitter/user")
    public Resource<Check> twitterUser(@Valid @RequestBody TwitterUser twitterUser) {
        checkHandler.twitterUserConsumer(twitterUser);
        return assembler.toResource(twitterUser);
    }

    @PostMapping("/twitter/tweet")
    public Resource<Check> twitterTweet(@Valid @RequestBody Tweet tweet) {
        checkHandler.tweetConsumer(tweet);
        return assembler.toResource(tweet);
    }

    @GetMapping("/response/{query_id}")
    public Resource<QueryResponse> findById(@PathVariable(value = "query_id", required = true) String id) {
        QueryResponse queryResponse = template.opsForValue().get(id);
        if (queryResponse == null) {
            throw new ResponseNotFoundException(id);
        }
        return new Resource<>(queryResponse,
                linkTo(methodOn(CheckController.class).findById(id)).withSelfRel());
    }
}

