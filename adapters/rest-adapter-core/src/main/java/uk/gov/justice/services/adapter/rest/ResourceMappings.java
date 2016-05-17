package uk.gov.justice.services.adapter.rest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

public final class ResourceMappings {

    private ResourceMappings() {
    }

    private static final String MEDIA_TYPE_PREFIX = "application/vnd.";

    public static String nameFrom(final Map<String, String> mappings, final HttpHeaders headers) {
        if (headers == null) {
            throw new IllegalStateException("Cannot get media type from empty http headers");
        }

        if (headers.getMediaType() != null && hasValidMediaTypePrefix(headers.getMediaType())) {
            return mappings.get(headers.getMediaType().getType());
        } else if (headers.getAcceptableMediaTypes() != null) {
            return firstValidMessageNameFrom(mappings, headers.getAcceptableMediaTypes())
                    .orElseThrow(ResourceMappings::IncorrectMediaTypesException);
        }

        throw IncorrectMediaTypesException();
    }

    private static Optional<String> firstValidMessageNameFrom(final Map<String, String> mappings, final List<MediaType> mediaTypes) {
        return mediaTypes.stream()
                .filter(ResourceMappings::hasValidMediaTypePrefix)
                .findFirst()
                .map(mediaType -> mappings.get(mediaType.getType()));
    }

    private static boolean hasValidMediaTypePrefix(final MediaType mediaType) {
        return mediaType.getType().startsWith(MEDIA_TYPE_PREFIX);
    }

    private static IllegalStateException IncorrectMediaTypesException() {
        return new IllegalStateException("Incorrect media types set in http headers");
    }

}
