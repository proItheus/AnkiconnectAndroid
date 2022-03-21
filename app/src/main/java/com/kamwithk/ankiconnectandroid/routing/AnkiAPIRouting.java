package com.kamwithk.ankiconnectandroid.routing;

import com.google.gson.JsonObject;
import com.kamwithk.ankiconnectandroid.ankidroid_api.DeckAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.IntegratedAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.MediaAPI;
import com.kamwithk.ankiconnectandroid.ankidroid_api.ModelAPI;
import com.kamwithk.ankiconnectandroid.request_parsers.Parser;
import fi.iki.elonen.NanoHTTPD;

import java.util.HashMap;
import java.util.Map;

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;

public class AnkiAPIRouting {
    private final IntegratedAPI integratedAPI;
    private final DeckAPI deckAPI;
    private final ModelAPI modelAPI;
    private final MediaAPI mediaAPI;

    public AnkiAPIRouting(IntegratedAPI integratedAPI) {
        this.integratedAPI = integratedAPI;
        deckAPI = integratedAPI.deckAPI;
        modelAPI = integratedAPI.modelAPI;
        mediaAPI = integratedAPI.mediaAPI;
    }

    public NanoHTTPD.Response findRoute(String json_string) throws Exception {
        JsonObject raw_json = Parser.parse(json_string);

        switch (Parser.get_action(raw_json)) {
            case "version":
                return version();
            case "deckNames":
                return deckNames();
            case "deckNamesAndIds":
                return deckNamesAndIds();
            case "modelNames":
                return modelNames();
            case "modelNamesAndIds":
                return modelNamesAndIds();
            case "modelFieldNames":
                return modelFieldNames(raw_json);
            case "canAddNotes":
                return canAddNotes(raw_json);
            case "addNote":
                return addNote(raw_json);
            case "storeMediaFile":
                return storeMediaFile(raw_json);
            default:
                return default_version();
        }
    }

    public NanoHTTPD.Response findRouteHandleError(String json_string) {
        try {
            return findRoute(json_string);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("result", null);
            response.put("error", e.toString());

            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(response));
        }
    }

    private NanoHTTPD.Response version() {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", "6");
    }

    private NanoHTTPD.Response default_version() {
        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", "AnkiConnect v.6");
    }

    private NanoHTTPD.Response deckNames() throws Exception {
        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "text/json",
                Parser.gson.toJson(deckAPI.deckNames())
        );
    }

    private NanoHTTPD.Response deckNamesAndIds() throws Exception {
        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "text/json",
                Parser.gson.toJson(deckAPI.deckNamesAndIds())
        );
    }

    private NanoHTTPD.Response modelNames() throws Exception {
        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "text/json",
                Parser.gson.toJson(modelAPI.modelNames())
        );
    }

    private NanoHTTPD.Response modelNamesAndIds() throws Exception {
        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "text/json",
                Parser.gson.toJson(modelAPI.modelNamesAndIds(0))
        );
    }

    private NanoHTTPD.Response modelFieldNames(JsonObject raw_json) throws Exception {
        String model_name = Parser.getModelNameFromParam(raw_json);
        if (model_name != null && !model_name.equals("")) {
            Long model_id = modelAPI.getModelID(model_name, 0);

            return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "text/json",
                    Parser.gson.toJson(modelAPI.modelFieldNames(model_id))
            );
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("result", null);
            response.put("error", "model was not found: ");

            return newFixedLengthResponse(
                    NanoHTTPD.Response.Status.OK,
                    "text/json",
                    Parser.gson.toJson(response)
            );
        }
    }

//    TODO: Implement
    private NanoHTTPD.Response canAddNotes(JsonObject raw_json) throws Exception {
        Map<String, boolean[]> response = new HashMap<>();
        response.put("result", Parser.getNoteTrues(raw_json));
        response.put("error", null);

        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(response));
    }

    private NanoHTTPD.Response addNote(JsonObject raw_json) throws Exception {
        return newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK,
                "text/json",
                String.valueOf(integratedAPI.addNote(
                        Parser.getNoteValues(raw_json),
                        Parser.getDeckName(raw_json),
                        Parser.getModelName(raw_json)
                ))
        );
    }

    private NanoHTTPD.Response storeMediaFile(JsonObject raw_json) throws Exception {
        Map<String, String> response = new HashMap<>();

        response.put("result", integratedAPI.storeMediaFile(
                Parser.getMediaFilename(raw_json),
                Parser.getMediaData(raw_json)
        ));
        response.put("error", null);

        return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "text/json", Parser.gson.toJson(response));
    }
}