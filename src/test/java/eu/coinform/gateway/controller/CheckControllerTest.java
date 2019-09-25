package eu.coinform.gateway.controller;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.model.Check;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(CheckController.class)
public class CheckControllerTest {

    private ObjectMapper mapper = new ObjectMapper();
    private String jsonTU, jsonTW;
    private final String tweetId = "1176610391058722816";
    private final String userId = "25073877";
    private final String screenName = "realDonaldTrump";

    @MockBean
    private CheckController checkController;

    private TwitterUser twitterUser = new TwitterUser();
    private Tweet tweet = new Tweet();

    @Autowired
    private MockMvc mockMvc;


    @Before
    public void setupTests(){
        twitterUser.setScreenName(screenName);
        twitterUser.setTwitterId(userId);
        log.debug("setupTests: " + twitterUser.toString());
        tweet.setTweetId(tweetId);
        log.debug("Tweet {}", tweet.toString());
        try {
            jsonTU = mapper.writeValueAsString(twitterUser);
            log.debug("setupTests jsonTU: {}", jsonTU);
            jsonTW = mapper.writeValueAsString(tweet);
            log.debug("setUpTests jsonTW: {}", jsonTW);
        } catch (JsonProcessingException e) {
            log.error("Error: " + e.getMessage());
        }
    }

    @Test
    public void contextLoads() throws Exception {
        assertThat(checkController).isNotNull();
    }

    @Test
    public void twitterUser() throws Exception{
        log.debug("In twitteruser test");

        Resource<Check> checkResource = new Resource<>(twitterUser,
                linkTo(methodOn(CheckController.class).findById(twitterUser.getId())).withSelfRel());

        log.debug("Twitteruser: " + twitterUser.toString());
        assertThat(checkResource).isNotNull();

        when(checkController.twitterUser(Mockito.any(TwitterUser.class))).thenReturn(checkResource);
        mockMvc.perform(post("/twitter/user")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTU)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().json(jsonTU));
    }

    @Test
    public void twitterTweet() throws Exception{
        log.debug("Int twitterTweet test");
        Resource<Check> checkResource = new Resource<>(tweet,
                linkTo(methodOn(CheckController.class).findById(tweet.getId())).withSelfRel());

        log.debug("Tweet: {}",tweet.toString());
        assertThat(checkResource).isNotNull();

        when(checkController.twitterTweet(Mockito.any(Tweet.class))).thenReturn(checkResource);
        mockMvc.perform(post("/twitter/tweet")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTW)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect((status().isOk()))
                .andExpect(content().json(jsonTW));

    }
}