package uk.gov.justice.services.adapters.rest.generator;

import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.restRamlWithDefaults;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;
import static uk.gov.justice.services.adapters.test.utils.config.GeneratorConfigUtil.configurationWithBasePackage;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.firstMethodOf;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.methodsOf;
import static uk.gov.justice.services.adapters.test.utils.reflection.ReflectionUtil.setField;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;

import uk.gov.justice.services.adapters.test.utils.builder.HttpActionBuilder;
import uk.gov.justice.services.core.dispatcher.AsynchronousDispatcher;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.impl.tl.ThreadLocalHttpHeaders;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

public class RestAdapterGenerator_POSTMethodBodyTest extends BaseRestAdapterGeneratorTest {

    private static final JsonObject NOT_USED_JSONOBJECT = Json.createObjectBuilder().build();

    @Mock
    private AsynchronousDispatcher dispatcher;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnResponseGeneratedByRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(HttpActionBuilder.httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        Response processorResponse = Response.ok().build();
        when(restProcessor.processAsynchronously(any(Consumer.class), anyString(), any(JsonObject.class), any(HttpHeaders.class),
                any(Map.class))).thenReturn(processorResponse);

        Method method = firstMethodOf(resourceClass);

        Object result = method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        assertThat(result, is(processorResponse));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldCallDispatcher() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(HttpActionBuilder.httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);

        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        ArgumentCaptor<Consumer> consumerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(restProcessor).processAsynchronously(consumerCaptor.capture(), anyString(), any(JsonObject.class), any(HttpHeaders.class),
                any(Map.class));

        JsonEnvelope envelope = envelopeFrom(null, null);
        consumerCaptor.getValue().accept(envelope);

        verify(dispatcher).dispatch(envelope);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassJsonObjectToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(HttpActionBuilder.httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        JsonObject jsonObject = Json.createObjectBuilder().add("dummy", "abc").build();

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, jsonObject);

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), eq(jsonObject), any(HttpHeaders.class), any(Map.class));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldPassHttpHeadersToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/path")
                                .with(HttpActionBuilder.httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultPathResource");
        Object resourceObject = instantiate(resourceClass);

        HttpHeaders headers = new ThreadLocalHttpHeaders();
        setField(resourceObject, "headers", headers);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, NOT_USED_JSONOBJECT);

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), any(JsonObject.class), eq(headers), any(Map.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamToRestProcessor() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{paramA}", "paramA")
                                .with(HttpActionBuilder.httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParamAResource");

        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValue1234", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), any(JsonObject.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(1));
        assertThat(pathParams.containsKey("paramA"), is(true));
        assertThat(pathParams.get("paramA"), is("paramValue1234"));

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithOnePathParamToRestProcessorWhenInvoking2ndMethod() throws Exception {

        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{p1}", "p1")
                                .with(HttpActionBuilder.httpAction(POST, "application/vnd.ctx.command.cmd-aa+json", "application/vnd.ctx.command.cmd-bb+json"))
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathP1Resource");

        Object resourceObject = instantiate(resourceClass);

        List<Method> methods = methodsOf(resourceClass);

        Method secondMethod = methods.get(1);
        secondMethod.invoke(resourceObject, "paramValueXYZ", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), any(JsonObject.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(1));
        assertThat(pathParams.containsKey("p1"), is(true));
        assertThat(pathParams.get("p1"), is("paramValueXYZ"));

    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void shouldPassMapWithTwoPathParamsToRestProcessor() throws Exception {
        generator.run(
                restRamlWithDefaults().with(
                        resource("/some/path/{param1}/{param2}", "param1", "param2")
                                .with(HttpActionBuilder.httpAction(POST).withHttpActionOfDefaultRequestType())
                ).build(),
                configurationWithBasePackage(BASE_PACKAGE, outputFolder, emptyMap()));

        Class<?> resourceClass = compiler.compiledClassOf(BASE_PACKAGE, "resource", "DefaultSomePathParam1Param2Resource");

        Object resourceObject = instantiate(resourceClass);

        Method method = firstMethodOf(resourceClass);
        method.invoke(resourceObject, "paramValueABC", "paramValueDEF", NOT_USED_JSONOBJECT);

        ArgumentCaptor<Map> pathParamsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(restProcessor).processAsynchronously(any(Consumer.class), anyString(), any(JsonObject.class), any(HttpHeaders.class),
                pathParamsCaptor.capture());

        Map pathParams = pathParamsCaptor.getValue();
        assertThat(pathParams.entrySet().size(), is(2));
        assertThat(pathParams.containsKey("param1"), is(true));
        assertThat(pathParams.get("param1"), is("paramValueABC"));

        assertThat(pathParams.containsKey("param2"), is(true));
        assertThat(pathParams.get("param2"), is("paramValueDEF"));
    }


    private Object instantiate(Class<?> resourceClass) throws InstantiationException, IllegalAccessException {
        Object resourceObject = resourceClass.newInstance();
        setField(resourceObject, "restProcessor", restProcessor);
        setField(resourceObject, "asyncDispatcher", dispatcher);
        return resourceObject;
    }


}
