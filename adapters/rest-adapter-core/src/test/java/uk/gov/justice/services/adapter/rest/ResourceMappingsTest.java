package uk.gov.justice.services.adapter.rest;

import static net.trajano.commons.testing.UtilityClassTestUtil.assertUtilityClassWellDefined;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResourceMappingsTest {

    private static final String TEST_MEDIA_TYPE = "application/vnd.test.mediatype";
    private static final String TEST_ACTION = "test.action";
    private static final String INCORRECT_MEDIA_TYPE = "application/incorrect";

    @Mock
    HttpHeaders headers;

    @Mock
    MediaType incorrectMediaType;

    private final Map<String, String> mappings = new HashMap<>();

    @Before
    public void setup() {
        mappings.put(TEST_MEDIA_TYPE, TEST_ACTION);
    }

    @Test
    public void shouldBeWellDefinedUtilityClass() {
        assertUtilityClassWellDefined(ResourceMappings.class);
    }

    @Test
    public void shouldReturnMessageName() throws Exception {
        when(headers.getMediaType()).thenReturn(mediaTypeWith(TEST_MEDIA_TYPE));

        String name = ResourceMappings.nameFrom(mappings, headers);
        assertThat(name, is(TEST_ACTION));
    }

    @Test
    public void shouldGetFromAcceptableMediaTypesIfNullMediaType() throws Exception {
        when(headers.getMediaType()).thenReturn(null);
        when(headers.getAcceptableMediaTypes()).thenReturn(listOf(mediaTypeWith(TEST_MEDIA_TYPE)));

        String name = ResourceMappings.nameFrom(mappings, headers);
        assertThat(name, is(TEST_ACTION));
    }

    @Test
    public void shouldGetFromAcceptableMediaTypesIfIncorrectPrefix() throws Exception {
        when(headers.getMediaType()).thenReturn(incorrectMediaType);
        when(incorrectMediaType.getType()).thenReturn(INCORRECT_MEDIA_TYPE);
        when(headers.getAcceptableMediaTypes()).thenReturn(listOf(mediaTypeWith(TEST_MEDIA_TYPE)));

        String name = ResourceMappings.nameFrom(mappings, headers);
        assertThat(name, is(TEST_ACTION));
    }

    @Test
    public void shouldGetFromMultipleAcceptableMediaTypes() throws Exception {
        List<MediaType> mediaTypes = listOf(
                mediaTypeWith(INCORRECT_MEDIA_TYPE),
                mediaTypeWith(TEST_MEDIA_TYPE));

        when(headers.getMediaType()).thenReturn(incorrectMediaType);
        when(incorrectMediaType.getType()).thenReturn(INCORRECT_MEDIA_TYPE);
        when(headers.getAcceptableMediaTypes()).thenReturn(mediaTypes);

        String name = ResourceMappings.nameFrom(mappings, headers);
        assertThat(name, is(TEST_ACTION));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionForNullHeaders() throws Exception {
        ResourceMappings.nameFrom(mappings, null);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfAcceptableMediaTypesIsNull() throws Exception {
        when(headers.getMediaType()).thenReturn(null);
        when(headers.getAcceptableMediaTypes()).thenReturn(null);

        ResourceMappings.nameFrom(mappings, headers);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfAcceptableMediaTypesHasIncorrectPrefix() throws Exception {
        when(headers.getMediaType()).thenReturn(null);
        when(headers.getAcceptableMediaTypes()).thenReturn(Collections.singletonList(new MediaType("application/incorrect", null)));

        ResourceMappings.nameFrom(mappings, headers);
    }

    private MediaType mediaTypeWith(String type) {
        return new MediaType(type, null);
    }

    private List<MediaType> listOf(final MediaType... mediaTypes) {
        return Arrays.asList(mediaTypes);
    }

}