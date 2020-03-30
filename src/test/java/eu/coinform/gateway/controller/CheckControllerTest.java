package eu.coinform.gateway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import eu.coinform.gateway.GatewayApplication;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.cache.Views;
import eu.coinform.gateway.db.*;
import eu.coinform.gateway.controller.restclient.RestClient;
import eu.coinform.gateway.db.PasswordAuthRepository;
import eu.coinform.gateway.db.RoleRepository;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.db.UserRepository;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.module.iface.LabelEvaluationImplementation;
import eu.coinform.gateway.util.Pair;
import eu.coinform.gateway.util.ReactionLabel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(CheckController.class)
public class CheckControllerTest {

    private ObjectMapper mapper = new ObjectMapper();
    private String jsonTU, jsonTW;
    private final Long tweetId = 1189715310766645254L;
    private final String screenName = "realDonaldTrump";
    private final String idUrl = "/response/%s";
    private final String tweetUrl = "/twitter/tweet";
    private final String userUrl = "/twitter/user";
    private final String debugUrl = idUrl + "/debog";
    final Function<StackWalker, String> methodName = s -> s.walk(sfs -> sfs.skip(1).findFirst().get().getMethodName());

    @MockBean
    private CheckController checkController;

    private TwitterUser twitterUser = new TwitterUser();
    private Tweet tweet = new Tweet();
    private JacksonTester<QueryResponse> jsonTester;

    @Autowired
    private MockMvc mockMvc;

    @Configuration
    @Import(GatewayApplication.class)
    public static class CheckControllerTestConfig {
        @Bean
        public UserDbManager testUserDbManager() {
            return new UserDbManager(mock(UserRepository.class), mock(PasswordAuthRepository.class), mock(RoleRepository.class), mock(VerificationTokenRepository.class), mock(PasswordEncoder.class));
        }
    }

    @Before
    public void setupTests(){
        JacksonTester.initFields(this, mapper);
        twitterUser.setScreenName(screenName);
        //twitterUser.setUserId(userId);
        log.debug("setupTests: {}", twitterUser.toString());
        tweet.setTweetId(tweetId);
        tweet.setTweetText("Hejbaberiba");
        tweet.setTweetAuthor("fakeDonaldTrump");
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
        assertThat(jsonTester).isNotNull();
    }

    @Test
    public void twitterUser() throws Exception{
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        QueryResponse queryResponse = new QueryResponse(twitterUser.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>(), new LinkedHashMap<>());

        log.debug("Twitteruser: " + twitterUser.toString());

        // given
        given(checkController.twitterUser(Mockito.any(TwitterUser.class))).willReturn(queryResponse);

        // when
        MockHttpServletResponse response = mockMvc.perform(post(userUrl)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTU)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn().getResponse();

        // then
        log.debug("Response {}", response.getContentAsString());
        QueryResponse resp = mapper.readValue(response.getContentAsString(), QueryResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(resp).isEqualTo(queryResponse);
    }

    @Test
    public void twitterTweet() throws Exception{
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>(), new LinkedHashMap<>());

        log.debug("Tweet: {}",tweet.toString());

        // given
        given(checkController.twitterTweet(Mockito.any(Tweet.class))).willReturn(queryResponse);

        // when
        MockHttpServletResponse response = mockMvc.perform(post(tweetUrl)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTW)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn().getResponse();

        // then
        log.debug("Response: {}", response.getContentAsString());
        QueryResponse resp = mapper.readValue(response.getContentAsString(), QueryResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(resp).isEqualTo(queryResponse);
    }

    @Test
    public void malformedTwitterUser() throws Exception {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>(), new LinkedHashMap<>());

        log.debug("TwitterUser: {}", twitterUser.toString());

        // given
        given(checkController.twitterUser((Mockito.any(TwitterUser.class)))).willReturn(queryResponse);

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
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>(), new LinkedHashMap<>());

        log.debug("TwitterTweet: {}", tweet.toString());

        // given
        given(checkController.twitterTweet(Mockito.any(Tweet.class))).willReturn(queryResponse);

        // when
        mockMvc.perform(post(tweetUrl)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonTU)
                .accept(MediaType.APPLICATION_JSON_UTF8))

        // then
                .andDo(print()).andExpect((status().is4xxClientError()));
    }

    @Test
    public void successfullIdGet() throws Exception{
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));


        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>(), new LinkedHashMap<>());

        String url = String.format(idUrl, tweet.getQueryId());

        // given
        given(checkController.findById(tweet.getQueryId())).willReturn(queryResponse);

        // when
        MockHttpServletResponse httpResponse = mockMvc.perform(get(url)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn().getResponse();

        QueryResponse response = mapper.readValue(httpResponse.getContentAsString(), QueryResponse.class);

        log.debug("Response: {}", response);
        log.debug("QueryResponse: {}", queryResponse);
        // then
        assertThat(httpResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getModuleResponseCode().isEmpty()).isTrue();
    }

    @Test
    public void nullIdGet() throws Exception {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        QueryResponse queryResponse = new QueryResponse("", QueryResponse.Status.in_progress, null, new LinkedHashMap<>(), new LinkedHashMap<>());

        String url = String.format(idUrl, "");
        log.debug("url: {}", url);

        // given
        given(checkController.findById("")).willReturn(queryResponse);

        // when
        MockHttpServletResponse httpResponse = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse();

        // then
        assertThat(httpResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());

    }

    @Test
    public void noTweetTextFailTest() throws Exception{
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        tweet.setTweetText(null);
        jsonTW = mapper.writeValueAsString(tweet);
        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>(), new LinkedHashMap<>());
        String jw = mapper.writeValueAsString(queryResponse);

        log.debug("Tweet: {}",tweet.toString());

        // given
        given(checkController.twitterTweet(Mockito.any(Tweet.class))).willReturn(queryResponse);

        // when
        MockHttpServletResponse response = mockMvc.perform(post(tweetUrl)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonTW)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse();

        // then
        log.debug("Response: {}", response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void noDebugJsonTest() throws Exception{
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        LinkedHashMap<String, Object> mResponse = new LinkedHashMap<>();
        mResponse.put("first", Pair.of("hej", "då"));
        mResponse.put("second", Pair.of("då", "hej"));
        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, mResponse, new LinkedHashMap<>());

        String jw = mapper.writerWithView(Views.NoDebug.class).writeValueAsString(queryResponse);
        String url = String.format(idUrl, tweet.getQueryId());
        log.debug("QR: {}", queryResponse);
        log.debug("Url: {}", url);
        log.debug("jw: {}", jw);

        // given
        given(checkController.findById(tweet.getQueryId())).willReturn(queryResponse);

        // when
        MockHttpServletResponse httpResponse = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse();

        QueryResponse response = mapper.readValue(httpResponse.getContentAsString(), QueryResponse.class);
        log.debug("Response: {}", httpResponse.getContentAsString());

        // then
        assertThat(httpResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getModuleResponseCode().size()).isEqualTo(0);
        assertThat(response.getModuleResponseCode().get("first")).isNull();
    }

    @Test
    public void debugJsonTest() throws Exception{
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        LinkedHashMap<String, Object> mResponse = new LinkedHashMap<>();
        mResponse.put("first", Pair.of("hej", "då"));
        mResponse.put("second", Pair.of("då", "hej"));
        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, mResponse, new LinkedHashMap<>());

        String jw = mapper.writerWithView(Views.Debug.class).writeValueAsString(queryResponse);
        String url = String.format(debugUrl, tweet.getQueryId());

        log.debug("QR: {}", queryResponse);
        log.debug("Url: {}", url);
        log.debug("jw: {}", jw);

        // given
        given(checkController.findById(tweet.getQueryId(),"debog")).willReturn(queryResponse);

        // when
        MockHttpServletResponse httpResponse = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse();

        log.debug("Response: {}", httpResponse.getContentAsString());
        QueryResponse response = mapper.readValue(httpResponse.getContentAsString(), QueryResponse.class);

        // then
        assertThat(httpResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getModuleResponseCode().size()).isEqualTo(2);
        assertThat(response.getModuleResponseCode().get("first")).isNotNull();
    }
/*
    @Test
    public void restCLientTest() {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        TweetLabelEvaluation tle = new TweetLabelEvaluation();
        tle.setReaction(ReactionLabel.agree);
        tle.setTweet_id("1181172459325800448");
        tle.setRated_credibility("not_credible");
        tle.setRated_moduleResponse("251b6a72cd3a3af314baf748abdfac93c076e54272dabcaef5b7b115eb65c848");

        LabelEvaluationImplementation levi = new LabelEvaluationImplementation(tle, UUID.randomUUID().toString());
        RestClient client = new RestClient(HttpMethod.POST, URI.create("https:///www.example.com"),levi);
        client.sendRequest();
    }*/
}
