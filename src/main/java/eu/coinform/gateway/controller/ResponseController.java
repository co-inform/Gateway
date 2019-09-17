package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.QueryResponse;
import eu.coinform.gateway.model.ResponseNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.function.Consumer;


@RestController
@Slf4j
public class ResponseController {

    private final RedisTemplate<String, QueryResponse> template;

    ResponseController(@Qualifier("redisTemplate") RedisTemplate<String, QueryResponse> template) {
        this.template = template;
    }

    @PostMapping("/misinfome")
    ResponseEntity<?> postMisinfoMe(@Valid @RequestBody QueryResponse queryResponse, @Qualifier("misinfome") Consumer<QueryResponse> responseConsumer) {
        responseConsumer.accept(queryResponse);
        template.opsForValue().set(queryResponse.getId(), queryResponse);
        log.debug("posting {}: {}", queryResponse.getId(), queryResponse);
        return ResponseEntity.ok().build();
    }
}
