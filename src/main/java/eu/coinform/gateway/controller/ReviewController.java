package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.Review;
import eu.coinform.gateway.model.ReviewNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@Slf4j
public class ReviewController {

    private final RedisTemplate<String, Review> template;

    ReviewController(@Qualifier("redisTemplate") RedisTemplate<String, Review> template) {
        this.template = template;
    }

    @PostMapping("/review")
    ResponseEntity<?> postReview(@Valid @RequestBody Review review) {
        template.opsForValue().set(review.getId(), review);
        log.debug("posting {}: {}", review.getId(), review);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/review/{id}")
    public org.springframework.hateoas.Resource<Review> findById(@PathVariable(value = "id", required = true) String id) {
        Review review = template.opsForValue().get(id);
        if (review == null) {
            throw new ReviewNotFoundException(id);
        }
        return new Resource<>(review,
                linkTo(methodOn(ReviewController.class).findById(id)).withSelfRel());
    }
}
