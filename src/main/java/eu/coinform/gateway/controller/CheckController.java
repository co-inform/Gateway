package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.Source;
import eu.coinform.gateway.model.RequestResourceAssembler;
import eu.coinform.gateway.model.Review;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.function.Consumer;

@RestController
@Slf4j
public class CheckController {

    private final RedisTemplate<String, Review> template;
    private final RequestResourceAssembler assembler;
    private final Consumer<Source> claimConsumer;

    CheckController(@Qualifier("redisTemplate") RedisTemplate<String,Review> template,
                    RequestResourceAssembler assembler,
                    Consumer<Source> claimConsumer
    ) {
        this.template = template;
        this.assembler = assembler;
        this.claimConsumer = claimConsumer;
    }

    @PostMapping("/credibility/source")
    public Resource<Source> checkClaim(@Valid @RequestBody Source source) {
        claimConsumer.accept(source);
        return assembler.toResource(source);
    }


}
