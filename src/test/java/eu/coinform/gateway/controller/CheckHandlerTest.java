package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.model.TwitterUser;
import eu.coinform.gateway.service.CheckHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import eu.coinform.gateway.module.Module;

import java.lang.reflect.Field;
import java.util.Map;


@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(CheckHandler.class)
public class CheckHandlerTest {



    @Test
    public void twitterUserConsumer() {



    }

    @Test
    public void tweetConsumer() {
    }
}