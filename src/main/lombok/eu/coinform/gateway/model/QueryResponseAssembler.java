package eu.coinform.gateway.model;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.controller.CheckController;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceAssembler;
import org.springframework.stereotype.Component;

/**
 * Assembles the HATEOAS compliant {@link QueryResponse} return objects
 */
@Component
public class QueryResponseAssembler implements ResourceAssembler<QueryResponse, Resource<QueryResponse>> {

    /**
     * Generates a HATEOAS compliant resource
     * @param queryResponse The {@link QueryResponse} object to generate from
     * @return A HATEOAS compliant resource
     */
    @Override
    public Resource<QueryResponse> toResource(QueryResponse queryResponse) {
        return new Resource<>(queryResponse,
                linkTo(methodOn(CheckController.class).findById(queryResponse.getQueryId())).withSelfRel()
            );
    }
}
