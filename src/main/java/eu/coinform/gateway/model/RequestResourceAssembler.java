package eu.coinform.gateway.model;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import eu.coinform.gateway.controller.CheckController;
import eu.coinform.gateway.controller.ReviewController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public class RequestResourceAssembler implements ResourceAssembler<Source, Resource<Source>> {

    @Override
    public Resource<Source> toResource(Source source) {
        return new Resource<>(source,
                linkTo(methodOn(ReviewController.class).findById(source.getId())).withSelfRel()
            );
    }
}
