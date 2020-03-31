package eu.coinform.gateway.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
public class ModuleRequestFactoryTest {

    Module module;
    ModuleRequestFactory moduleRequestFactory;
    ModuleRequestBuilder moduleRequestBuilder;
    ModuleRequest moduleRequest;
    MRCImplementation mrcImplementation;
    String uuid = UUID.randomUUID().toString();
    ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup(){
        module = new Module("test", "http", "www.example.com", "", 80, (a, b) -> b) {
            @Override
            public ModuleRequestFactory getModuleRequestFactory() {
                return super.getModuleRequestFactory();
            }
        };
        moduleRequestFactory = new ModuleRequestFactory(module);
        moduleRequestBuilder = moduleRequestFactory.getRequestBuilder(uuid);
        mrcImplementation = new MRCImplementation("/module/response/");
    }

    @Test
    public void getRequestBuilderSuccessfullTest() {

        assertThat(moduleRequestFactory).isNotNull();
        assertThat(moduleRequestBuilder).isNotNull();

        try {
           moduleRequest = moduleRequestBuilder.setPath("/" + uuid).setContent(mrcImplementation).build();
        } catch (ModuleRequestBuilderException | JsonProcessingException e){
            log.error(e.getMessage());
        }
        log.debug(moduleRequest.toString());
        assertThat(moduleRequest).isNotNull();
        assertThat(moduleRequest.getQueryId()).isEqualTo(uuid);
    }

  //  @Test(expected = ModuleRequestBuilderException.class)
    @Test
    public void getRequestBuilderEmptyBodyTest()  {

        assertThat(moduleRequestFactory).isNotNull();
        assertThat(moduleRequestBuilder).isNotNull();

        try {
            moduleRequest = moduleRequestBuilder.setPath("/" + uuid).build();
//        } catch (JsonProcessingException e){
//            log.error(e.getMessage());
        } catch(ModuleRequestBuilderException e){
            log.info(e.getMessage());
        }

        assertThat(moduleRequest).isNull();
    }

    @Test
    public void getRequestBuilderMalformedUIRITest(){

        module = new Module("test", "http", "www.example.com", "", 80, (a, b) -> b) {
            @Override
            public ModuleRequestFactory getModuleRequestFactory() {
                return super.getModuleRequestFactory();
            }
        };
        moduleRequestFactory = new ModuleRequestFactory(module);
        assertThat(moduleRequestFactory).isNotNull();

        moduleRequestBuilder = moduleRequestFactory.getRequestBuilder(uuid);
        assertThat(moduleRequestBuilder).isNotNull();

        try {
            moduleRequest = moduleRequestBuilder.setPath("/"+uuid).build();
        } catch (ModuleRequestBuilderException e){
            assertThat(moduleRequest).isNull();
        }

    }

    @Test
    public void moduleRequestExceptionTest() {

        assertThat(moduleRequestFactory).isNotNull();
        assertThat(moduleRequestBuilder).isNotNull();

        try {
            moduleRequest = moduleRequestBuilder.setPath("/" + uuid).setContent(mrcImplementation).build();
        } catch (ModuleRequestBuilderException | JsonProcessingException e){
            log.error(e.getMessage());
        }

        assertThat(moduleRequest).isNotNull();

        moduleRequest.makeRequest();

    }
/*
    @Test
    public void moduleRequestLabelEvaluation() {
        TweetLabelEvaluation tle = new TweetLabelEvaluation();
        tle.setReaction(ReactionLabel.agree);
        tle.setTweet_id("1181172459325800448");
        tle.setRated_credibility("not_credible");
        tle.setRated_moduleResponse("251b6a72cd3a3af314baf748abdfac93c076e54272dabcaef5b7b115eb65c848");

        assertThat(moduleRequestFactory).isNotNull();
        assertThat(moduleRequestBuilder).isNotNull();

        LabelEvaluationImplementation levi = new LabelEvaluationImplementation(tle, UUID.randomUUID().toString());

        try {
            moduleRequest = moduleRequestBuilder.setPath("/user/accuracy-review").setContent(levi).build();
            log.debug("LEVI {}", mapper.writeValueAsString(levi));
        } catch(ModuleRequestBuilderException | JsonProcessingException e){
            log.error(e.getMessage());
        }

        assertThat(moduleRequest).isNotNull();
        log.debug(moduleRequest.toString());

    }

*/
    private class MRCImplementation extends ModuleRequestContent{

        MRCImplementation(String callbackBaseUrl) {
            super(callbackBaseUrl);
        }
    }
}