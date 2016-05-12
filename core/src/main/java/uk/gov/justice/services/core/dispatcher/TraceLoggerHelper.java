package uk.gov.justice.services.core.dispatcher;

import org.slf4j.Logger;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by vagrant on 5/11/16.
 */
public class TraceLoggerHelper {

    public static String trace(final JsonEnvelope envelope) {

        String tmp = "{ \"id\": %S, \"name\": %s, }";
        String traceMessgae = String.format("%s dispatching message with ID {%s} and NAME {%s}",
                envelope.metadata().id(),
                envelope.metadata().name());

        if(envelope.metadata().causation() != null) {
            return String.format("%s with causation message IDs {%s}", traceMessgae, envelope.metadata().causation()
                    .stream()
                    .map((uuid) -> {
                        return uuid.toString();
                    })
                    .collect(Collectors.joining(", ")));
        } else {
            return traceMessgae;
        }
    }

}
