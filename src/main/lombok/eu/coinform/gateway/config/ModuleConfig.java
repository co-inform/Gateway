package eu.coinform.gateway.config;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.module.Module;
import eu.coinform.gateway.module.ModuleRequest;
import eu.coinform.gateway.module.misinfome.MisInfoMe;
import eu.coinform.gateway.service.RedisHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.BiFunction;

@Configuration
@Slf4j
public class ModuleConfig {

    @Bean
    @Qualifier("misinfome")
    public Module misinfoMeModule(@Value("${misinfome.name}") String name,
                                  @Value("${misinfome.server.scheme}") String scheme,
                                  @Value("${misinfome.server.url}") String url,
                                  @Value("${misinfome.server.base_endpoint}") String baseEndpoint,
                                  @Value("${misinfome.server.port}") int port,
                                  BiFunction<ModuleRequest, HttpResponse, HttpResponse> standardResponseHandler) {

        return new MisInfoMe(name, scheme, url, baseEndpoint, port, standardResponseHandler);
    }

    /*
    @Bean
    @Qualifier("contentanalysis")
    public Module contentanalysisModule(@Value("${contentanalysis.name}") String name,
                                  @Value("${contentanalysis.server.scheme}") String scheme,
                                  @Value("${contentanalysis.server.url}") String url,
                                  @Value("${contentanalysis.server.base_endpoint}") String baseEndpoint,
                                  @Value("${contentanalysis.server.port}") int port) {
        return new ContentAnalysis(name, scheme, url, baseEndpoint, port);
    }
     */

    /*
    @Bean
    @Qualifier("claimcredibility")
    public Module claimcredibilityModule(@Value("${claimcredibility.name}") String name,
                                         @Value("${claimcredibility.server.scheme}") String scheme,
                                         @Value("${claimcredibility.server.url}") String url,
                                         @Value("${claimcredibility.server.base_endpoint}") String baseEndpoint,
                                         @Value("${claimcredibility.server.port}") int port) {
        return new ClaimCredibility(name, scheme, url, baseEndpoint, port);
    }
    */

    @Bean
    public BiFunction<ModuleRequest, HttpResponse, HttpResponse> standardResponseHandler(RedisHandler redisHandler) {
        return ((moduleRequest, httpResponse) -> {
            log.debug("request with query_id '{}' got response {}", moduleRequest.getQueryId(), httpResponse.getStatusLine());
            if (log.isTraceEnabled()) {
                StringBuilder sb = new StringBuilder();
                for (Header header : httpResponse.getAllHeaders()) {
                    sb.append(header.toString());
                    sb.append("\n");
                }
                log.trace("headers: {}", sb.substring(0, sb.length()-1));
                try {
                    log.trace("content: {}", CharStreams
                            .toString(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8)));
                } catch (IOException ex) {
                    log.trace("failing to write content: {}", ex.getMessage());
                }
            }
            QueryResponse queryResponse = redisHandler.getQueryResponse(moduleRequest.getQueryId()).join();
            queryResponse.getModule_response_code().put(moduleRequest.getModule().getName().toLowerCase(), httpResponse.getStatusLine().getStatusCode());
            redisHandler.setQueryResponse(moduleRequest.getQueryId(), queryResponse);
            return httpResponse;
        });
    }
}
