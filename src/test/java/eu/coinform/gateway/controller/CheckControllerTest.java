package eu.coinform.gateway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.model.QueryResponseAssembler;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

import java.util.LinkedHashMap;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
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
    //private final Long userId = 25073877L;
    private final String screenName = "realDonaldTrump";
    private final String idUrl = "/response/%s";
    private final String tweetUrl = "/twitter/tweet";
    private final String userUrl = "/twitter/user";
    final Function<StackWalker, String> methodName = s -> s.walk(sfs -> sfs.skip(1).findFirst().get().getMethodName());

    @MockBean
    private CheckController checkController;

    private TwitterUser twitterUser = new TwitterUser();
    private Tweet tweet = new Tweet();
    private JacksonTester<Resource<QueryResponse>> jsonTester;
    private QueryResponseAssembler queryResponseAssembler = new QueryResponseAssembler();

    @Autowired
    private MockMvc mockMvc;

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

        QueryResponse queryResponse = new QueryResponse(twitterUser.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>());
        Resource<QueryResponse> queryResorce = queryResponseAssembler.toResource(queryResponse);

        log.debug("Twitteruser: " + twitterUser.toString());
        assertThat(queryResorce).isNotNull();

        // given
        given(checkController.twitterUser(Mockito.any(TwitterUser.class)))
                .willReturn(queryResorce);

        // when
        MockHttpServletResponse response = mockMvc.perform(post(userUrl)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTU)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn().getResponse();

        // then
        log.debug("Response {}", response.getContentAsString());
        log.debug("checkResource: {}", jsonTester.write(queryResorce).getJson());
        QueryResponse resp = mapper.readValue(response.getContentAsString(), QueryResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(resp).isEqualTo(queryResponse);
    }

    @Test
    public void twitterTweet() throws Exception{
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>());
        Resource<QueryResponse> queryResource = queryResponseAssembler.toResource(queryResponse);

        log.debug("Tweet: {}",tweet.toString());
        assertThat(queryResource).isNotNull();

        // given
        given(checkController.twitterTweet(Mockito.any(Tweet.class))).willReturn(queryResource);

        // when
        MockHttpServletResponse response = mockMvc.perform(post(tweetUrl)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTW)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                        .andReturn().getResponse();

        // then
        log.debug("Response: {}", response.getContentAsString());
        log.debug("checkResource: {}", jsonTester.write(queryResource).getJson());
        QueryResponse resp = mapper.readValue(response.getContentAsString(), QueryResponse.class);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(resp).isEqualTo(queryResponse);
    }

    @Test
    public void malformedTwitterUser() throws Exception {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>());
        Resource<QueryResponse> queryResorce = queryResponseAssembler.toResource(queryResponse);

        log.debug("TwitterUser: {}", twitterUser.toString());
        assertThat(queryResorce).isNotNull();

        // given
        given(checkController.twitterUser((Mockito.any(TwitterUser.class)))).willReturn(queryResorce);

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

        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>());
        Resource<QueryResponse> queryResorce = queryResponseAssembler.toResource(queryResponse);

        log.debug("TwitterTweet: {}", tweet.toString());
        assertThat(queryResorce).isNotNull();

        // given
        given(checkController.twitterTweet(Mockito.any(Tweet.class))).willReturn(queryResorce);

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

        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>());
        Resource<QueryResponse> queryResource = queryResponseAssembler.toResource(queryResponse);

        String url = String.format(idUrl, tweet.getQueryId());
        assertThat(queryResource).isNotNull();
        // given
        given(checkController.findById(tweet.getQueryId())).willReturn(queryResource);

        // when
        MockHttpServletResponse httpResponse = mockMvc.perform(get(url)
                    .accept(MediaType.APPLICATION_JSON_UTF8))
                    .andReturn().getResponse();

        QueryResponse response = mapper.readValue(httpResponse.getContentAsString(), QueryResponse.class);

        // then
        assertThat(httpResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response).isEqualTo(queryResponse);
    }

    @Test
    public void nullIdGet() throws Exception {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));

        QueryResponse queryResponse = new QueryResponse("", QueryResponse.Status.in_progress, null, new LinkedHashMap<>());
        Resource<QueryResponse> queryResource = queryResponseAssembler.toResource(queryResponse);

        String url = String.format(idUrl, "");
        assertThat(queryResource).isNotNull();

        log.debug("url: {}", url);
        // given
        given(checkController.findById("")).willReturn(queryResource);

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
        QueryResponse queryResponse = new QueryResponse(tweet.getQueryId(), QueryResponse.Status.in_progress, null, new LinkedHashMap<>());
        Resource<QueryResponse> queryResource = queryResponseAssembler.toResource(queryResponse);

        log.debug("Tweet: {}",tweet.toString());
        assertThat(queryResource).isNotNull();

        // given
        given(checkController.twitterTweet(Mockito.any(Tweet.class))).willReturn(queryResource);

        // when
        MockHttpServletResponse response = mockMvc.perform(post(tweetUrl)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(jsonTW)
                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andReturn().getResponse();

        // then
        log.debug("Response: {}", response.getContentAsString());
        log.debug("checkResource: {}", jsonTester.write(queryResource).getJson());

        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        try {
            QueryResponse resp = mapper.readValue(response.getContentAsString(), QueryResponse.class);
        } catch (MismatchedInputException e){
            assertThat(true).isTrue();
        }

    }
}
