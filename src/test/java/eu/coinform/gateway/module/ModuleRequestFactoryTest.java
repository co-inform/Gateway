package eu.coinform.gateway.module;

import com.fasterxml.jackson.core.JsonProcessingException;
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

    private class MRCImplementation extends ModuleRequestContent{

        MRCImplementation(String callbackBaseUrl) {
            super(callbackBaseUrl);
        }
    }
}