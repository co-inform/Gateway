package eu.coinform.gateway.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.coinform.gateway.cache.ModuleResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
public class RuleEngineHelperTest {

    private final Function<StackWalker, String> methodName = s -> s.walk(sfs -> sfs.skip(1).findFirst().get().getMethodName());
    private ObjectMapper mapper = new ObjectMapper();
    private ModuleResponse response;
    private JsonNode jsonResponse;
    private final String KEY = "TEST";

    private JacksonTester<ModuleResponse> jsonTester;

    @Before
    public void setup() {
        JacksonTester.initFields(this,mapper);
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            File file = new File(Objects.requireNonNull(cl.getResource("response.json")).getFile());
            response = mapper.readValue(file,ModuleResponse.class);
            jsonResponse = mapper.readTree(file);
            log.debug("response: {}", jsonTester.write(response).getJson());
            log.debug("jsonResponse: {}",jsonResponse);
        } catch (IOException e){
            log.error("{} threw {}", methodName.apply(StackWalker.getInstance()),e.getMessage());
        }

    }

    @Test
    public void contextLoads(){
        assertThat(jsonTester).isNotNull();
        assertThat(response).isNotNull();
        assertThat(jsonResponse).isNotNull();
    }

    @Test
    public void testResponseParserObject() throws IOException, JSONException {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, JsonNode> jsonMap = new JsonFlattener(jsonResponse).flatten();

        RuleEngineHelper.flatResponseMap(response, result, KEY+".response");

        log.debug("jsonMap; {}", mapper.writeValueAsString(jsonMap));
        log.debug("result: {}", mapper.writeValueAsString(result));

        JSONAssert.assertEquals(mapper.writeValueAsString(jsonMap),mapper.writeValueAsString(result), JSONCompareMode.STRICT);
    }

    @Test
    public void testResponseParserObjectEmpty() throws IOException, JSONException {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, JsonNode> jsonMap = new JsonFlattener(mapper.readTree("{\"response\": {}}")).flatten();

        RuleEngineHelper.flatResponseMap(mapper.readValue("{\"response\": {}}", ModuleResponse.class), result, KEY+".response");

        log.debug("jsonMap; {}", mapper.writeValueAsString(jsonMap));
        log.debug("result: {}", mapper.writeValueAsString(result));

        JSONAssert.assertEquals(mapper.writeValueAsString(jsonMap),mapper.writeValueAsString(result), JSONCompareMode.STRICT);
    }

    @Test
    public void testResponseParserObjectNull() throws IOException, JSONException {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, JsonNode> jsonMap = new JsonFlattener(mapper.readTree("{\"response\": null}")).flatten();

        RuleEngineHelper.flatResponseMap(mapper.readValue("{\"response\": null}", ModuleResponse.class), result, KEY+".response");

        log.debug("jsonMap; {}", mapper.writeValueAsString(jsonMap));
        log.debug("result: {}", mapper.writeValueAsString(result));
        //todo: might have to change RuleEngineHelper to comply to how null is handled accordign to JSON spec
        // https://stackoverflow.com/questions/21120999/representing-null-in-json and
        // http://www.json.org/
        JSONAssert.assertEquals(mapper.writeValueAsString(jsonMap),mapper.writeValueAsString(result), JSONCompareMode.STRICT);
    }


    @Test
    public void testResponseParserWithArrays() throws IOException, JSONException {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));
        Map<String, Object> result = new LinkedHashMap<>();
        String jsonString ="{\"response\": {\"arr\": [\"test\", \"hello\"]}}";
        Map<String, JsonNode> jsonMap = new JsonFlattener(mapper.readTree(jsonString)).flatten();

        RuleEngineHelper.flatResponseMap(mapper.readValue(jsonString, ModuleResponse.class), result, KEY+".response");

        log.debug("jsonMap; {}", mapper.writeValueAsString(jsonMap));
        log.debug("result: {}", mapper.writeValueAsString(result));

        JSONAssert.assertEquals(mapper.writeValueAsString(jsonMap),mapper.writeValueAsString(result), JSONCompareMode.STRICT);
    }

    @Test
    public void testResponseParserWithEmptyArray() throws IOException, JSONException {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));
        Map<String, Object> result = new LinkedHashMap<>();
        String jsonString ="{\"response\": {\"arr\": []}}";
        Map<String, JsonNode> jsonMap = new JsonFlattener(mapper.readTree(jsonString)).flatten();

        RuleEngineHelper.flatResponseMap(mapper.readValue(jsonString, ModuleResponse.class), result, KEY+".response");

        log.debug("jsonMap: {}", mapper.writeValueAsString(jsonMap));
        log.debug("result: {}", mapper.writeValueAsString(result));

        JSONAssert.assertEquals(mapper.writeValueAsString(jsonMap),mapper.writeValueAsString(result), JSONCompareMode.STRICT);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void testResponseParserWithoutResponse() throws IOException {
        log.debug("In {}", methodName.apply(StackWalker.getInstance()));
        Map<String, Object> result = new LinkedHashMap<>();
        String jsonString =" {\"arr\": [\"test\", \"hello\"]}";
        Map<String, JsonNode> jsonMap = new JsonFlattener(mapper.readTree(jsonString)).flatten();

        RuleEngineHelper.flatResponseMap(mapper.readValue(jsonString, ModuleResponse.class), result, KEY+".response");
    }

    // Unashamedly nicked from https://stackoverflow.com/questions/58008267/flattening-a-3-level-nested-json-string-in-java
    // slightly modified...
    class JsonFlattener {

        private final Map<String, JsonNode> json = new LinkedHashMap<>();
        private final JsonNode root;

        JsonFlattener(JsonNode node) {
            this.root = Objects.requireNonNull(node);
        }

        public Map<String, JsonNode> flatten() {
            process(root, KEY);
            return json;
        }

        private void process(JsonNode node, String prefix) {
            if (node.isObject()) {
                ObjectNode object = (ObjectNode) node;
                object
                        .fields()
                        .forEachRemaining(
                                entry -> process(entry.getValue(), prefix + "." + entry.getKey().toLowerCase()));
            } else if (node.isArray()) {
                ArrayNode array = (ArrayNode) node;
                AtomicInteger counter = new AtomicInteger();
                array
                        .elements()
                        .forEachRemaining(
                                item -> process(item, prefix + "." + counter.getAndIncrement()));
            } else {
                json.put(prefix, node);
            }
        }
    }
}