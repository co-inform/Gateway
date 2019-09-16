package eu.coinform.gateway.model;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import eu.coinform.gateway.controller.CheckController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public class RequestResourceAssembler implements ResourceAssembler<Check, Resource<Check>> {

    @Override
    public Resource<Check> toResource(Check check) {
        return new Resource<>(check,
                linkTo(methodOn(CheckController.class).findById(check.getId())).withSelfRel()
            );
    }
}
