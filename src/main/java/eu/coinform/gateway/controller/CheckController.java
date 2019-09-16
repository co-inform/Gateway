package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.function.Consumer;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@Slf4j
public class CheckController {

    private final RedisTemplate<String, QueryResponse> template;
    private final RequestResourceAssembler assembler;
    private final Consumer<TwitterUser> twitterUserConsumer;
    private final Consumer<Tweet> tweetConsumer;

    CheckController(@Qualifier("redisTemplate") RedisTemplate<String, QueryResponse> template,
                    RequestResourceAssembler assembler,
                    Consumer<TwitterUser> twitterUserConsumer,
                    Consumer<Tweet> tweetConsumer
    ) {
        this.template = template;
        this.assembler = assembler;
        this.twitterUserConsumer = twitterUserConsumer;
        this.tweetConsumer = tweetConsumer;
    }

    @PostMapping("/twitter/user")
    public Resource<Check> twitterUser(@Valid @RequestBody TwitterUser twitterUser) {
        twitterUserConsumer.accept(twitterUser);
        return assembler.toResource(twitterUser);
    }

    @PostMapping("/twitter/tweet")
    public Resource<Check> twitterTweet(@Valid @RequestBody Tweet tweet) {
        tweetConsumer.accept(tweet);
        return assembler.toResource(tweet);
    }

    @GetMapping("/response/{id}")
    public org.springframework.hateoas.Resource<QueryResponse> findById(@PathVariable(value = "id", required = true) String id) {
        QueryResponse queryResponse = template.opsForValue().get(id);
        if (queryResponse == null) {
            throw new ResponseNotFoundException(id);
        }
        return new Resource<>(queryResponse,
                linkTo(methodOn(CheckController.class).findById(id)).withSelfRel());
    }
}

