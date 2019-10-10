package eu.coinform.gateway.util;

import eu.coinform.gateway.cache.ModuleResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class RuleEngineHelper {

    /**
     * Populates a map with a flattening of the ModuleResponse.
     *
     * @param response The ModuleResponse to flatten
     * @param outputMap The map to populate with the flattened map
     * @param baseKey The base of the keys to add
     */
    public static void flatResponseMap(ModuleResponse response, Map<String, Object> outputMap, String baseKey) {
        new ResponseParserObject(response.getResponse(), outputMap, baseKey + ".");
    }

    private static class ResponseParserObject {

        ResponseParserObject(LinkedHashMap<String, Object> jsonObject, Map<String, Object> flatMap, String keyBase) {

            if(jsonObject == null) {
                return;
            }

            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                if (entry.getValue() instanceof LinkedHashMap) {
                    new ResponseParserObject((LinkedHashMap<String, Object>) entry.getValue(),
                            flatMap,
                            String.format("%s%s.", keyBase, entry.getKey().toLowerCase()));
                } else if (entry.getValue() instanceof ArrayList) {
                    new ResponseParserArray((ArrayList<Object>) entry.getValue(),
                            flatMap,
                            String.format("%s%s", keyBase, entry.getKey().toLowerCase()));
                } else {
                    flatMap.put(String.format("%s%s", keyBase, entry.getKey().toLowerCase()), entry.getValue());
                }
            }
        }
    }

    private static class ResponseParserArray {

        ResponseParserArray(ArrayList<Object> jsonArray, Map<String, Object> flatMap, String keyBase) {

            if(jsonArray == null) {
                return;
            }

            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.get(i) instanceof LinkedHashMap) {
                    new ResponseParserObject((LinkedHashMap<String, Object>) jsonArray.get(i),
                            flatMap,
                            String.format("%s[%d].", keyBase, i));
                } else if (jsonArray.get(i) instanceof ArrayList) {
                    new ResponseParserArray((ArrayList<Object>) jsonArray.get(i),
                            flatMap,
                            String.format("%s[%d]", keyBase, i));
                } else {
                    flatMap.put(String.format("%s[%d]", keyBase, i), jsonArray.get(i));
                }
            }
        }
    }

}
