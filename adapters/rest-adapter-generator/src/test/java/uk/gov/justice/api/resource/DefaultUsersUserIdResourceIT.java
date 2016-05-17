package uk.gov.justice.api.resource;

import static com.jayway.jsonassert.JsonAssert.with;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.cxf.jaxrs.client.WebClient.create;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.ID;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.NAME;

import uk.gov.justice.api.QueryApiRestExampleApplication;
import uk.gov.justice.services.adapter.rest.JsonSchemaValidationInterceptor;
import uk.gov.justice.services.adapter.rest.RestProcessor;
import uk.gov.justice.services.adapter.rest.RestProcessorProducer;
import uk.gov.justice.services.adapter.rest.envelope.RestEnvelopeBuilderFactory;
import uk.gov.justice.services.adapter.rest.mapper.BadRequestExceptionMapper;
import uk.gov.justice.services.adapters.test.utils.dispatcher.AsynchronousRecordingDispatcher;
import uk.gov.justice.services.adapters.test.utils.dispatcher.SynchronousRecordingDispatcher;
import uk.gov.justice.services.core.json.JsonSchemaLoader;
import uk.gov.justice.services.core.json.JsonSchemaValidator;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.JsonObjectMetadata;
import uk.gov.justice.services.messaging.Metadata;

import java.util.Properties;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.Json;
import javax.ws.rs.core.Response;

import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for the generated JAX-RS classes.
 */
@EnableServices("jaxrs")
@RunWith(ApplicationComposer.class)
public class DefaultUsersUserIdResourceIT {

    private static final String CREATE_USER_MEDIA_TYPE = "application/vnd.people.command.create-user+json";
    private static final String UPDATE_USER_MEDIA_TYPE = "application/vnd.people.command.update-user+json";
    private static final String BASE_URI_PATTERN = "http://localhost:%d/rest-adapter-generator/query/api/rest/example";
    private static final String JSON = "{\"userUrn\" : \"test\"}";
    private static int port = -1;
    private static String BASE_URI;
    @Inject
    AsynchronousRecordingDispatcher asyncDispatcher;
    @Inject
    SynchronousRecordingDispatcher syncDispatcher;
    private Metadata metadata;

    @BeforeClass
    public static void beforeClass() {
        port = NetworkUtil.getNextAvailablePort();
        BASE_URI = String.format(BASE_URI_PATTERN, port);
    }

    @Before
    public void before() {
        metadata = JsonObjectMetadata.metadataFrom(Json.createObjectBuilder()
                .add(ID, UUID.randomUUID().toString())
                .add(NAME, "eventName")
                .build());

    }

    @Configuration
    public Properties properties() {
        return new PropertiesBuilder()
                .p("httpejbd.port", Integer.toString(port))
                .p(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true")
                .build();
    }

    @Module
    @Classes(cdi = true, value = {
            RestProcessor.class,
            RestProcessorProducer.class,
            RestEnvelopeBuilderFactory.class,
            AsynchronousRecordingDispatcher.class,
            SynchronousRecordingDispatcher.class,
            JsonObjectEnvelopeConverter.class,
            BadRequestExceptionMapper.class,
            JsonSchemaValidationInterceptor.class,
            JsonSchemaValidator.class,
            JsonSchemaLoader.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("rest-adapter-generator")
                .addServlet("TestApp", Application.class.getName())
                .addInitParam("TestApp", "javax.ws.rs.Application", QueryApiRestExampleApplication.class.getName());
    }

    @Test
    public void shouldReturn202CreatingUser() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/1234")
                .post(entity(JSON, CREATE_USER_MEDIA_TYPE));

        assertThat(response.getStatus(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldDispatchCreateUserCommand() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/567-8910")
                .post(entity("{\"userUrn\" : \"1234\"}", CREATE_USER_MEDIA_TYPE));

        JsonEnvelope jsonEnvelope = asyncDispatcher.awaitForEnvelopeWithPayloadOf("userId", "567-8910");
        assertThat(jsonEnvelope.metadata().name(), is("people.command.create-user"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userId"), is("567-8910"));
        assertThat(jsonEnvelope.payloadAsJsonObject().getString("userUrn"), is("1234"));
    }

    @Test
    public void shouldReturn400ForJsonNotAdheringToSchema() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/1234")
                .post(entity("{\"blah\" : \"1234\"}", CREATE_USER_MEDIA_TYPE));

        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturn202UpdatingUser() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/1234")
                .post(entity(JSON, UPDATE_USER_MEDIA_TYPE));

        assertThat(response.getStatus(), is(ACCEPTED.getStatusCode()));
    }

    @Test
    public void shouldReturn200ResponseContainingUserDataReturnedByDispatcher() {
        syncDispatcher.setupResponse("userId", "4444-5556",
                envelopeFrom(metadata, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.query.get-user+json")
                .get();

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        String responseBody = response.readEntity(String.class);
        with(responseBody)
                .assertThat("userName", equalTo("userName"));
    }


    @Test
    public void shouldDispatchUpdateUserCommand() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/4444-9876")
                .post(entity("{\"userUrn\" : \"5678\"}", UPDATE_USER_MEDIA_TYPE));

        JsonEnvelope envelope = asyncDispatcher.awaitForEnvelopeWithPayloadOf("userId", "4444-9876");
        assertThat(envelope.metadata().name(), is("people.command.update-user"));
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-9876"));
        assertThat(envelope.payloadAsJsonObject().getString("userUrn"), is("5678"));

    }

    @Test
    public void shouldDispatchGetUserCommand() throws Exception {
        Response response = create(BASE_URI)
                .path("/users/4444-5555")
                .header("Accept", "application/vnd.people.query.get-user+json")
                .get();
        JsonEnvelope envelope = syncDispatcher.awaitForEnvelopeWithPayloadOf("userId", "4444-5555");
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-5555"));
        assertThat(envelope.metadata().name(), is("people.get-user1"));

    }

    @Test
    public void shouldDispatchGetUserCommandWithOtherMediaType() throws Exception {
        syncDispatcher.setupResponse("userId", "4444-5555", envelopeFrom(metadata, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users/4444-5555")
                .header("Accept", "application/vnd.people.query.get-user2+json")
                .get();

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        JsonEnvelope envelope = syncDispatcher.awaitForEnvelopeWithPayloadOf("userId", "4444-5555");
        assertThat(envelope.payloadAsJsonObject().getString("userId"), is("4444-5555"));
        assertThat(envelope.metadata().name(), is("people.get-user2"));

    }

    @Test
    public void shouldReturn406ifQueryTypeNotRecognised() throws Exception {

        Response response = create(BASE_URI)
                .path("/users/4444-5555")
                .header("Accept", "application/vnd.people.query.unknown+json")
                .get();

        assertThat(response.getStatus(), is(NOT_ACCEPTABLE.getStatusCode()));

    }

    @Test
    public void shouldReturnUserDataReturnedByDispatcher() {
        syncDispatcher.setupResponse("userId", "4444-5556", envelopeFrom(metadata, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.query.get-user+json")
                .get();

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        String responseBody = response.readEntity(String.class);
        with(responseBody)
                .assertThat("userName", equalTo("userName"));

    }

    @Test
    public void shouldReturnResponseWithContentType() {
        syncDispatcher.setupResponse("userId", "4444-5556", envelopeFrom(metadata, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.query.get-user+json")
                .get();
        assertThat(response.getStatus(), is(OK.getStatusCode()));
        assertThat(response.getMediaType().toString(), is("application/vnd.people.query.get-user+json"));
    }

    @Test
    public void shouldReturnResponseWithSecondContentType() {
        syncDispatcher.setupResponse("userId", "4444-5556", envelopeFrom(metadata, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users/4444-5556")
                .header("Accept", "application/vnd.people.query.get-user2+json")
                .get();
        assertThat(response.getStatus(), is(OK.getStatusCode()));
        assertThat(response.getMediaType().toString(), is("application/vnd.people.query.get-user2+json"));
    }

    @Test
    public void shouldDispatchUsersQueryWithQueryParam() throws Exception {
        syncDispatcher.setupResponse("lastname", "lastname", envelopeFrom(metadata, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users")
                .query("lastname", "lastname")
                .query("firstname", "firstname")
                .header("Accept", "application/vnd.people.query.search-users+json")
                .get();

        assertThat(response.getStatus(), is(OK.getStatusCode()));
        JsonEnvelope jsonEnvelope = syncDispatcher.awaitForEnvelopeWithPayloadOf("lastname", "lastname");
        assertThat(jsonEnvelope.metadata().name(), is("people.query.search-users"));

    }

    @Test
    public void shouldReturn400IfRequiredQueryParamIsNotProvided() throws Exception {

        Response response = create(BASE_URI)
                .path("/users")
                .query("firstname", "firstname")
                .header("Accept", "application/vnd.people.query.search-users+json")
                .get();

        assertThat(response.getStatus(), is(BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void shouldReturn200WhenOptionalParameterIsNotProvided() throws Exception {
        syncDispatcher.setupResponse("lastname", "lastname", envelopeFrom(metadata, createObjectBuilder().add("userName", "userName").build()));

        Response response = create(BASE_URI)
                .path("/users")
                .query("lastname", "lastname")
                .header("Accept", "application/vnd.people.query.search-users+json")
                .get();

        assertThat(response.getStatus(), is(OK.getStatusCode()));

    }

}
