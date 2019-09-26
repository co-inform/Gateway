package eu.coinform.gateway.model;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.controller.CheckController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

@Component
public class QueryResponseAssembler implements ResourceAssembler<QueryResponse, Resource<QueryResponse>> {

    @Override
    public Resource<QueryResponse> toResource(QueryResponse queryResponse) {
        return new Resource<>(queryResponse,
                linkTo(methodOn(CheckController.class).findById(queryResponse.getQueryId())).withSelfRel()
            );
    }
}
