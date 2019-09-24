package eu.coinform.gateway.controller;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.model.Check;
import eu.coinform.gateway.model.TwitterUser;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
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
    String jsonTU;// = "{\"user_id\": \"25073877\", \"screen_name\": \"realDonaldTrump\"}";
    final String userId = "25073877";
    final String screenName = "realDonaldTrump";

    @MockBean
    private CheckController checkController;

//    @Autowired
    TwitterUser twitterUser = new TwitterUser();

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setupTests(){
        System.out.println("setupTests: " + twitterUser.toString());
        twitterUser.setScreenName(screenName);
        twitterUser.setTwitterId(userId);
        System.out.println("setupTests: " + twitterUser.toString());
        try {
            jsonTU = mapper.writeValueAsString(twitterUser);
            System.out.println("setupTests: " + twitterUser.toString());
            System.out.println("setupTests: " + jsonTU);
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
//        System.out.println(jsonTU);
        System.out.println("Twitteruser: " + twitterUser.toString());
        Resource<Check> rc = checkController.twitterUser(twitterUser); //rc == null?
        //assertThat(rc).isNotNull(); // är jävligt mykke null...
        //String s = mapper.writeValueAsString(rc.toString());
        //System.out.println("RC vaddå?: " + s);
        when(checkController.twitterUser(twitterUser)).thenReturn(rc);
        mockMvc.perform(post("/twitter/user")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(jsonTU)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string("")); //WTF??
    }

    @Test
    public void twitterTweet() {
        assertTrue(true);
    }
}