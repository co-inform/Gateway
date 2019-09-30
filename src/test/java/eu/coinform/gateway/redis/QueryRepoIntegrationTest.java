package eu.coinform.gateway.redis;


import eu.coinform.gateway.cache.ModuleTransaction;
import eu.coinform.gateway.config.TestRedisConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestRedisConfiguration.class)
public class QueryRepoIntegrationTest {

    @Autowired
    private ModuleTransaction moduleTransaction;

    @Test
    public void testing(){

    }

}
