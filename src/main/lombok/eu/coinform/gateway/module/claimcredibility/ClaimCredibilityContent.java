package eu.coinform.gateway.module.claimcredibility;

import eu.coinform.gateway.module.ModuleRequestContent;
import lombok.Getter;
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

