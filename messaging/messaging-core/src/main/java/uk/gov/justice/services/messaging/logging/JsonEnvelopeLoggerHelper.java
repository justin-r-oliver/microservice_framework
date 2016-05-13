package uk.gov.justice.services.messaging.logging;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import java.util.List;
import java.util.UUID;

/**
 * Helper class to provide trace string for logging of JsonEnvelopes
 */
public class JsonEnvelopeLoggerHelper {

    public static String toEnvelopeTraceString(final JsonEnvelope envelope) {

        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", String.valueOf(envelope.metadata().id()))
                .add("name", envelope.metadata().name());

        JsonArrayBuilder causationBuilder = Json.createArrayBuilder();

        List<UUID> causes = envelope.metadata().causation();

        if(causes != null) {
            envelope.metadata().causation().stream()
                    .forEach(uuid -> causationBuilder.add(String.valueOf(uuid)));
        }
        return builder.add("causation", causationBuilder).build().toString();
    }
}
