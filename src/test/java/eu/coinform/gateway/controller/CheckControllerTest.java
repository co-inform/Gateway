package eu.coinform.gateway.controller;

import static org.mockito.BDDMockito.given;
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
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
    private final String tweetUrl = "/twitter/tweet";
    private final String userUrl = "/twitter/user";

    @MockBean
    private CheckController checkController;

    private Resource<Check> checkResource;
    private TwitterUser twitterUser = new TwitterUser();
    private Tweet tweet = new Tweet();
    private JacksonTester<Resource<Check>> jsonTester;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setupTests(){
        JacksonTester.initFields(this, mapper);
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

         checkResource = new Resource<>(twitterUser,
                linkTo(methodOn(CheckController.class).findById(twitterUser.getId())).withSelfRel());

        log.debug("Twitteruser: " + twitterUser.toString());
        assertThat(checkResource).isNotNull();

        // given
        given(checkController.twitterUser(Mockito.any(TwitterUser.class)))
                .willReturn(checkResource);

        // when
        MockHttpServletResponse response = mockMvc.perform(post(userUrl)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTU)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn().getResponse();

        // then
        log.debug("Response {}", response.getContentAsString());
        log.debug("checkResource: {}", jsonTester.write(checkResource).getJson());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString().regionMatches(0, jsonTester.write(checkResource).getJson(),0,129)).isTrue();
    }

    @Test
    public void twitterTweet() throws Exception{
        log.debug("Int twitterTweet test");
        checkResource = new Resource<>(tweet,
                linkTo(methodOn(CheckController.class).findById(tweet.getId())).withSelfRel());

        log.debug("Tweet: {}",tweet.toString());
        assertThat(checkResource).isNotNull();

        // given
        given(checkController.twitterTweet(Mockito.any(Tweet.class))).willReturn(checkResource);

        // when

        MockHttpServletResponse response = mockMvc.perform(post(tweetUrl)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTW)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn().getResponse();

        // then
        log.debug("Response: {}", response.getContentAsString());
        log.debug("checkResource: {}", jsonTester.write(checkResource).getJson());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString().regionMatches(0,jsonTester.write(checkResource).getJson(),0,106));
    }

    @Test
    public void malformedTwitterUser() throws Exception {
        log.debug("In failedTwitterUser()");

        checkResource = new Resource<>(twitterUser,
                linkTo(methodOn(CheckController.class).findById(twitterUser.getId())).withSelfRel());

        log.debug("TwitterUser: {}", twitterUser.toString());
        assertThat(checkResource).isNotNull();

        // given
        given(checkController.twitterUser((Mockito.any(TwitterUser.class)))).willReturn(checkResource);

        // when
        mockMvc.perform(post(userUrl)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTW)
                        .accept(MediaType.APPLICATION_JSON_UTF8))

        // then
                        .andDo(print()).andExpect(status().is4xxClientError());
    }

    @Test
    public void malformedTwitterTweet() throws Exception {
        log.debug("In malformedTwitterTweet()");

        checkResource = new Resource<>(tweet,
                linkTo(methodOn(CheckController.class).findById(tweet.getId())).withSelfRel());

        log.debug("TwitterTweet: {}", tweet.toString());
        assertThat(checkResource).isNotNull();

        // given
        given(checkController.twitterTweet(Mockito.any(Tweet.class))).willReturn(checkResource);

        // when
        mockMvc.perform(post(tweetUrl)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonTU)
                .accept(MediaType.APPLICATION_JSON_UTF8))

        // then
                .andDo(print()).andExpect((status().is4xxClientError()));
    }

}