package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import eu.coinform.gateway.controller.forms.ExternalEvaluationForm;
import eu.coinform.gateway.module.iface.AccuracyReview;
import eu.coinform.gateway.module.iface.FeedbackRequester;
import eu.coinform.gateway.module.iface.ReviewRating;
import eu.coinform.gateway.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class GatewayListenersTest {

    ObjectMapper mapper = new ObjectMapper();
    String response;
    String review;


    @Before
    public void setup() throws IOException {
        response = FileUtils.readFileToString(new File(Resources.getResource("postFactcheckerAccuracyReviewResponse.json").getFile()), Charset.defaultCharset());
        review = FileUtils.readFileToString(new File(Resources.getResource("somareview.json").getFile()), Charset.defaultCharset());
    }

    @Test
    public void testPostFactcheckerAccuracyReviewResponseParsing() throws JsonProcessingException {
        List<FeedbackRequester> feedbackRequesters = new LinkedList<>();
        mapper.readTree(response)
                .get("response")
                .get("relatedUserReviews")
                .get("usersWhoRequestedFactcheck")
                .elements().forEachRemaining(node -> {
            JsonNode ar = node.get("mostRecentAccuracyReview");
            feedbackRequesters.add(new FeedbackRequester(
                    node.get("author.url").asText(),
                    node.get("type").asText(),
                    new AccuracyReview(
                            ar.get("name").asText(),
                            new ReviewRating(ar.get("reviewRating").get("ratingValue").asText()),
                            ZonedDateTime.parse(ar.get("dateCreated").asText(),
                                    DateTimeFormatter.ISO_DATE_TIME),
                            ar.get("text").asText())));
        });

        assertThat(feedbackRequesters.size()).isEqualTo(1);
        assertThat(feedbackRequesters.get(0).getAuthorUUID()).isEqualTo("2094970a-2b0d-4f1a-999c-d0a41b3c677f");
    }

}
