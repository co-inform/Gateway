package eu.coinform.gateway.util;

import eu.coinform.gateway.cache.ModuleResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuleEngineHelper {

    private RuleEngineHelper(){}

    /**
     * Populates a map with a flattening of the ModuleResponse.
     *
     * @param response The ModuleResponse to flatten
     * @param outputMap The map to populate with the flattened map
     * @param baseKey The base of the keys to add
     * @param divider The dividers added to mark accesses of inner objects
     */
    public static void flatResponseMap(ModuleResponse response, Map<String, Object> outputMap, String baseKey, String divider) {
        new ResponseParserObject(response.getResponse(), outputMap, baseKey + divider, divider);
    }

    private static class ResponseParserObject {

        @SuppressWarnings("unchecked") // jacksson data bind will return LinkedHashMap
        ResponseParserObject(LinkedHashMap<String, Object> jsonObject, Map<String, Object> flatMap, String keyBase, String divider) {

            if(jsonObject == null) {
                flatMap.put(keyBase.substring(0, keyBase.length() - 1), null);
                return;
            }

            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                if (entry.getValue() instanceof LinkedHashMap) {
                    new ResponseParserObject((LinkedHashMap<String, Object>) entry.getValue(),
                            flatMap,
                            String.format("%s%s%s", keyBase, nameReformatter(entry.getKey()), divider),
                            divider);
                } else if (entry.getValue() instanceof ArrayList) {
                    new ResponseParserArray((ArrayList<Object>) entry.getValue(),
                            flatMap,
                            String.format("%s%s%s", keyBase, nameReformatter(entry.getKey()), divider),
                            divider);
                } else {
                    flatMap.put(String.format("%s%s", keyBase, nameReformatter(entry.getKey())), entry.getValue());
                }
            }
        }

        private String nameReformatter(String name) {
            return name.toLowerCase().replaceAll("-", "_");
        }
    }

    private static class ResponseParserArray {

        @SuppressWarnings("unchecked") // jacksson data bind will return LinkedHashMap
        ResponseParserArray(ArrayList<Object> jsonArray, Map<String, Object> flatMap, String keyBase, String divider) {

            if(jsonArray == null) {
                flatMap.put(keyBase.substring(0, keyBase.length() - 1), null);
                return;
            }

            for (int i = 0; i < jsonArray.size(); i++) {
                if (jsonArray.get(i) instanceof LinkedHashMap) {
                    new ResponseParserObject((LinkedHashMap<String, Object>) jsonArray.get(i),
                            flatMap,
                            String.format("%s%d%s", keyBase, i, divider),
                            divider);
                } else if (jsonArray.get(i) instanceof ArrayList) {
                    new ResponseParserArray((ArrayList<Object>) jsonArray.get(i),
                            flatMap,
                            String.format("%s%d%s", keyBase, i, divider),
                            divider);
                } else {
                    flatMap.put(String.format("%s%d", keyBase, i), jsonArray.get(i));
                }
            }
        }
    }

}
