package eu.coinform.gateway.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.net.http.HttpRequest;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
public class CheckHandlers {

    private String rootUrl;
    private int port;
    private ObjectWriter objectWriter;

    CheckHandlers(@Value("${md.server.url}") String rootUrl, @Value("${md.server.port}") int port, ObjectMapper objectMapper) {
        this.rootUrl = rootUrl;
        this.port = port;
        this.objectWriter = objectMapper.writer();
    }

    @Bean
    public Consumer<Source> sourceConsumer() {
        return source -> {

            log.debug("handle source object: {}", source);
            try {
                sendPost("source/", objectWriter.writeValueAsString(source), null, null);
            } catch (JsonProcessingException ex) {
                log.error("JsonProcessingException could not write {}: {}", source.toString(), ex.getMessage());
            }
        };
    }

    @Bean
    Consumer<TwitterUser> twitterUserConsumer() {
        return twitterUser -> {
            log.debug("handle review object: {}", twitterUser);
        };
    }

    @Bean
    Consumer<Tweet> tweetConsumer() {
        return tweet -> {
            log.debug("handle tweet object: {}", tweet);
        };
    }

    private void sendPost(String endpoint, String body, String pathVariable, Map<String, String> params) {
        URL url;
        String path = rootUrl + ":" + port + "/" + endpoint;
        try {
            if (pathVariable == null || pathVariable.isBlank()) {
                url = new URL(path);
            } else {
                url = new URL(path + "/" +pathVariable);
            }
        } catch (MalformedURLException ex) {
            log.error("Malformed URL {}: {}", path, ex.getMessage());
            return;
        }
        try {
            log.debug("post to {}{}: {}",url, getParamString(params), body);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUSH");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(getParamString(params));
            out.writeBytes(body);
            out.flush();
            out.close();

            log.debug("got response {}: {}", connection.getResponseCode(), connection.getResponseMessage());
        } catch (IOException ex) {
            log.error("Could not open url connection with url: {}", url);
        }
    }

    private String getParamString(Map<String, String> params) throws UnsupportedEncodingException {
        if(params == null || params.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry: params.entrySet()) {
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            sb.append("&");
        }

        String res = sb.toString();
        return res.substring(0, res.length()-1); // the last '&' should not be included
    }
}
