package uk.gov.justice.services.adapter.rest;

import static com.jayway.jsonassert.JsonAssert.with;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;

import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for the {@link RestProcessor} class.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestProcessorTest {

    private static final JsonObject NOT_USED_PAYLOAD = Json.createObjectBuilder().build();
    private static final HashMap<String, String> NOT_USED_PATH_PARAMS = new HashMap<>();
    private static final HttpHeaders NOT_USED_HEADERS = new ResteasyHttpHeaders(new MultivaluedMapImpl<>());
    private static final String NOT_USED_ACTION = "actionABC";

    @Mock
    private Consumer<JsonEnvelope> consumer;

    @Mock
    private Function<JsonEnvelope, JsonEnvelope> function;

    private RestProcessor restProcessor;

    private Metadata metadata;

    @Before
    public void setup() {
        restProcessor = new RestProcessor(new RestEnvelopeBuilderFactory(), envelope -> envelope.payload().toString());

        metadata = JsonObjectMetadata.metadataFrom(Json.createObjectBuilder()
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, "eventName")
                .build());
    }

    @Test
    public void shouldReturn202ResponseOnAsyncProcessing() throws Exception {
        Response response = restProcessor.processAsynchronously(consumer, NOT_USED_ACTION, NOT_USED_PAYLOAD, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(202));
    }

    @Test
    public void shouldPassEnvelopeWithPayloadToConsumerOnAsyncProcessing() throws Exception {
        JsonObject payload = Json.createObjectBuilder().add("key123", "value45678").build();
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("paramABC", "paramValueBCD");

        restProcessor.processAsynchronously(consumer, NOT_USED_ACTION, payload, NOT_USED_HEADERS, pathParams);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(consumer).accept(envelopeCaptor.capture());

        JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.payloadAsJsonObject().getString("key123"), is("value45678"));
        assertThat(envelope.payloadAsJsonObject().getString("paramABC"), is("paramValueBCD"));
    }

    @Test
    public void shouldPassEnvelopeWithMetadataToConsumerOnAsyncProcessing() throws Exception {

        restProcessor.processAsynchronously(consumer, "some.action", NOT_USED_PAYLOAD,NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);

        verify(consumer).accept(envelopeCaptor.capture());

        JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is("some.action"));
    }

    @Test
    public void shouldReturn200ResponseOnSyncProcessing() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(
                envelopeFrom(metadata, Json.createObjectBuilder().build()));
        Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(200));
    }

    @Test
    public void shouldReturn404ResponseOnSyncProcessingIfPayloadIsJsonValueNull() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(envelopeFrom(null, JsonValue.NULL));
        Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(404));
    }

    @Test
    public void shouldReturn500ResponseOnSyncProcessingIfEnvelopeIsNull() throws Exception {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(null);
        Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        assertThat(response.getStatus(), equalTo(500));
    }

    @Test
    public void shouldPassEnvelopeWithMetadataToFunctionOnSyncProcessing() throws Exception {
        String action = "somecontext.somequery";
        restProcessor.processSynchronously(function, action, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(function).apply(envelopeCaptor.capture());

        JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.metadata().name(), is(action));
    }

    @Test
    public void shouldPassEnvelopeWithPayloadToFunctionOnSyncProcessing() throws Exception {
        HashMap<String, String> pathParams = new HashMap<>();
        pathParams.put("param1", "paramValue345");

        restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, pathParams);

        ArgumentCaptor<JsonEnvelope> envelopeCaptor = ArgumentCaptor.forClass(JsonEnvelope.class);
        verify(function).apply(envelopeCaptor.capture());

        JsonEnvelope envelope = envelopeCaptor.getValue();
        assertThat(envelope.payloadAsJsonObject().getString("param1"), is("paramValue345"));

    }

    @Test
    public void shouldReturnPayloadOfEnvelopeReturnedByFunction() {
        when(function.apply(any(JsonEnvelope.class))).thenReturn(
                envelopeFrom(metadata, Json.createObjectBuilder().add("key11", "value33").add("key22", "value55").build()));

        Response response = restProcessor.processSynchronously(function, NOT_USED_ACTION, NOT_USED_HEADERS, NOT_USED_PATH_PARAMS);
        String responseEntity = (String) response.getEntity();
        with(responseEntity)
                .assertThat("key11", equalTo("value33"))
                .assertThat("key22", equalTo("value55"));
    }


}
