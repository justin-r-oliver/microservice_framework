package uk.gov.justice.services.core.dispatcher;

import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 * Created by vagrant on 5/11/16.
 */
public class TraceLoggerHelper {

    public static String printMessageAsJsonString(final JsonEnvelope envelope) {

        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("id", String.valueOf(envelope.metadata().id()))
                .add("name", envelope.metadata().name());

        JsonArrayBuilder causationBuilder = Json.createArrayBuilder();

        if(envelope.metadata().causation() == null) {

            return String.valueOf(builder.add("causation", causationBuilder).build());
        }

        envelope.metadata().causation().stream()
                .forEach(uuid -> causationBuilder.add(String.valueOf(uuid)));

        return String.valueOf(builder.add("causation", causationBuilder).build());

    }
}
