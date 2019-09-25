package eu.coinform.gateway.controller;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.model.Check;
import eu.coinform.gateway.model.TwitterUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(CheckController.class)
public class CheckControllerTest {

    ObjectMapper mapper = new ObjectMapper();
    String jsonTU;
    final String userId = "25073877";
    final String screenName = "realDonaldTrump";

    @MockBean
    private CheckController checkController;

    TwitterUser twitterUser = new TwitterUser();

    @Autowired
    private MockMvc mockMvc;

    Resource<Check> rc;

    @Before
    public void setupTests(){
        log.debug("setupTests: " + twitterUser.toString());
        twitterUser.setScreenName(screenName);
        twitterUser.setTwitterId(userId);
        rc = new Resource<>(twitterUser,
                linkTo(methodOn(CheckController.class).findById(twitterUser.getId())).withSelfRel()
        );
        log.debug("RC {}", rc.toString());
        log.debug("setupTests: " + twitterUser.toString());
        try {
            jsonTU = mapper.writeValueAsString(twitterUser);
            log.debug("setupTests: " + twitterUser.toString());
            log.debug("setupTests: " + jsonTU);
            log.debug("Json: " + jsonTU);
        } catch (JsonProcessingException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    @Test
    public void contextLoads() throws Exception {
        assertThat(checkController).isNotNull();
//        checkController.
    }

    @Test
    public void twitterUser() throws Exception{
        log.debug("In twitteruser test");

        log.debug("Twitteruser: " + twitterUser.toString());
        String id = twitterUser.getId();
        assertThat(rc).isNotNull(); // är jävligt mykke null...

        when(checkController.twitterUser(Mockito.any(TwitterUser.class))).thenReturn(rc);
        mockMvc.perform(post("/twitter/user")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTU)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(jsonTU));
    }

    @Test
    public void twitterTweet() {
        assertTrue(true);
    }
}