package uk.gov.justice.services.adapters.rest.validator;

import static org.raml.model.ActionType.GET;
import static org.raml.model.ActionType.POST;
import static uk.gov.justice.services.adapters.test.utils.builder.RamlBuilder.raml;
import static uk.gov.justice.services.adapters.test.utils.builder.ResourceBuilder.resource;

import uk.gov.justice.raml.common.validator.RamlValidationException;
import uk.gov.justice.raml.common.validator.RamlValidator;
import uk.gov.justice.services.adapters.test.utils.builder.HttpActionBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResponseContentTypeRamlValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private RamlValidator validator = new ResponseContentTypeRamlValidator();

    @Test
    public void shouldPassIfResponseContentTypeContainsAValidQueryName() throws Exception {

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(HttpActionBuilder.httpAction(GET).withResponseTypes("application/vnd.ctx.query.query1+json"))
                ).build());
    }

    @Test
    public void shouldIgnoreInvalidResponseContentTypeInNonGETActions() throws Exception {

        validator.validate(
                raml()
                        .with(resource("/some/path")
                                .with(HttpActionBuilder.httpAction(GET).withResponseTypes("application/vnd.ctx.query.query1+json")))
                        .with(resource("/some/path")
                                .with(HttpActionBuilder.httpAction(POST).withResponseTypes("application/vnd.ctx.invalid.aa+json")))
                        .build());
    }

    @Test
    public void shouldThrowExceptionIfResponseContentTypeNotSet() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Response type not set");

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(HttpActionBuilder.httpAction(GET))
                ).build());

    }

    @Test
    public void shouldThrowExceptionIfdResponseContentTypeInvalid() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid response type: application/vnd.people.invalid.abc1+json");


        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(HttpActionBuilder.httpAction(GET).withResponseTypes("application/vnd.people.invalid.abc1+json"))
                ).build());

    }


    @Test
    public void shouldThrowExceptionIfdResponseContentTypeDoesNotContainContext() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid response type: application/vnd.people.invalid.abc1+json");


        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(HttpActionBuilder.httpAction(GET).withResponseTypes("application/vnd.people.invalid.abc1+json"))
                ).build());

    }


    @Test
    public void shouldThrowExceptionIfMediaTypeDoesNotContainContext() throws Exception {

        exception.expect(RamlValidationException.class);
        exception.expectMessage("Invalid response type: application/vnd.query.query1+json");

        validator.validate(
                raml().with(
                        resource("/some/path")
                                .with(HttpActionBuilder.httpAction(GET).withResponseTypes("application/vnd.query.query1+json"))
                ).build());

    }

}
