package eu.coinform.gateway.module.claimcredibility;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.coinform.gateway.module.ModuleRequestContent;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

public class ClaimCredibilityContent extends ModuleRequestContent {

    public ClaimCredibilityContent(String callbackBaseUrl, ArrayList<ClaimCredibilityTweet> tweets) {
        super(callbackBaseUrl);
        this.tweets = tweets;
    }

    @Getter
    @Setter
    private ArrayList<ClaimCredibilityTweet> tweets;

}

