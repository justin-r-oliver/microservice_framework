package uk.gov.justice.services.adapter.rest;

import static javax.ws.rs.HttpMethod.GET;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;


public class BasicActionMapper {

    private static final String MEDIA_TYPE_PREFIX = "application/vnd.";

    private Map<String, Map<String, String>> methodToMediatypeAndActionMap = new HashMap<>();


    protected void add(String methodName, String mediaType, String actionName) {
        Map<String, String> mediaTypeToActionNameMap = methodToMediatypeAndActionMap.get(methodName);
        if (mediaTypeToActionNameMap == null) {
            mediaTypeToActionNameMap = new HashMap<>();
            methodToMediatypeAndActionMap.put(methodName, mediaTypeToActionNameMap);
        }
        mediaTypeToActionNameMap.put(mediaType, actionName);

    }

    public String actionOf(String methodName, String httpMethod, HttpHeaders headers) {
        Map<String, String> mediaTypeToActionMap = methodToMediatypeAndActionMap.get(methodName);
        return mediaTypeToActionMap.get(mediaTypeOf(httpMethod, headers));
    }


    private String mediaTypeOf(String httpMethod, HttpHeaders headers) {
        if (GET.equals(httpMethod)) {
            return headers.getAcceptableMediaTypes().stream()
                    .map(MediaType::toString)
                    .filter(mt -> mt.startsWith(MEDIA_TYPE_PREFIX))
                    .findFirst()
                    .get();
        } else {
            return headers.getMediaType().toString();
        }

    }


}
