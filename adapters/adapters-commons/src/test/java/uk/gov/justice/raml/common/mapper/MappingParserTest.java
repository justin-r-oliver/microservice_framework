package uk.gov.justice.raml.common.mapper;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.raml.common.mapper.Mapping.buildMapping;
import static uk.gov.justice.raml.common.mapper.MappingParser.mappingParserForGet;
import static uk.gov.justice.raml.common.mapper.MappingParser.mappingParserForPost;

import java.util.Map;

import org.junit.Test;

public class MappingParserTest {

    private static final String POST_MAPPING_PART =
            "        (mapping):\n" +
                    "            requestType: %s\n" +
                    "            name: %s\n";

    private static final String[] POST_FIELD_NAMES = new String[]{"requestType", "name"};

    private static final Mapping POST_MAPPING_1 = buildMapping()
            .withField("requestType", "mediaType1")
            .withField("name", "name1")
            .build();

    private static final Mapping POST_MAPPING_2 = buildMapping()
            .withField("requestType", "mediaType2")
            .withField("name", "name2")
            .build();

    private static final String GET_MAPPING_PART =
            "        (mapping):\n" +
                    "            responseType: %s\n" +
                    "            name: %s\n";

    private static final String[] GET_FIELD_NAMES = new String[]{"responseType", "name"};

    private static final Mapping GET_MAPPING_1 = buildMapping()
            .withField("responseType", "mediaType1")
            .withField("name", "name1")
            .build();

    private static final Mapping GET_MAPPING_2 = buildMapping()
            .withField("responseType", "mediaType2")
            .withField("name", "name2")
            .build();

    @Test
    public void shouldReturnNoMappingsForBlankDescription() throws Exception {
        final Map<String, Mapping> mappings = mappingParserForPost().parseFromDescription("");
        assertThat(mappings.size(), is(0));
    }

    @Test
    public void shouldReturnNoMappingsForNullDescription() throws Exception {
        final Map<String, Mapping> mappings = mappingParserForPost().parseFromDescription(null);
        assertThat(mappings.size(), is(0));
    }

    @Test
    public void shouldReturnNoMappingsForDescriptionWithNoMappings() throws Exception {
        final Map<String, Mapping> mappings = mappingParserForPost().parseFromDescription("Description\n with no mappings\n");
        assertThat(mappings.size(), is(0));
    }

    @Test
    public void shouldReturnNoMappingsForDescriptionWithEmptyMappingSection() throws Exception {
        final Map<String, Mapping> mappings = mappingParserForPost().parseFromDescription("...\n \n...\n");
        assertThat(mappings.size(), is(0));
    }

    @Test
    public void shouldReturnSingleMappingFromDescriptionWithSingleMappingDefined() throws Exception {
        String description = descriptionWithMappingsForPost(POST_MAPPING_1);

        final Map<String, Mapping> mappings = mappingParserForPost().parseFromDescription(description);

        assertThat(mappings.values(), containsInAnyOrder(POST_MAPPING_1));
    }

    @Test
    public void shouldReturnMultipleMappingsFromDescriptionWithSingleMappingDefined() throws Exception {
        String description = descriptionWithMappingsForPost(POST_MAPPING_1, POST_MAPPING_2);

        final Map<String, Mapping> mappings = mappingParserForPost().parseFromDescription(description);

        assertThat(mappings.values(), containsInAnyOrder(POST_MAPPING_1, POST_MAPPING_2));
    }

    @Test
    public void shouldReturnMultipleMappingsFromGetDescriptionWithSingleMappingDefined() throws Exception {
        String description = descriptionWithMappingsForGet(GET_MAPPING_1, GET_MAPPING_2);

        final Map<String, Mapping> mappings = mappingParserForGet().parseFromDescription(description);

        assertThat(mappings.values(), containsInAnyOrder(GET_MAPPING_1, GET_MAPPING_2));
    }

    @Test
    public void shouldReturnFromMultipleMappingSectionsInDescription() throws Exception {
        String description = "Pre Text\n" +
                descriptionWithMappingsForPost(POST_MAPPING_1) +
                "middle text\n" +
                descriptionWithMappingsForPost(POST_MAPPING_2) +
                "post text.\n";

        final Map<String, Mapping> mappings = mappingParserForPost().parseFromDescription(description);

        assertThat(mappings.values(), containsInAnyOrder(POST_MAPPING_1, POST_MAPPING_2));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionIfExpectedFieldsAreNotPresent() throws Exception {
        String description = "...\n" +
                "        (mapping):\n" +
                "            requestType: %s\n" +
                "...\n";

        mappingParserForPost().parseFromDescription(description);
    }

    private String descriptionWithMappingsForPost(final Mapping... mappings) {
        return descriptionWithMappings(POST_MAPPING_PART, POST_FIELD_NAMES, mappings);
    }

    private String descriptionWithMappingsForGet(final Mapping... mappings) {
        return descriptionWithMappings(GET_MAPPING_PART, GET_FIELD_NAMES, mappings);
    }

    private String descriptionWithMappings(final String mappingPart, final String[] fieldNames, final Mapping... mappings) {
        StringBuilder builder = new StringBuilder();
        builder.append("...\n");

        stream(mappings).forEach(mapping -> builder.append(
                format(mappingPart,
                        mapping.get(fieldNames[0]),
                        mapping.get(fieldNames[1]))));

        builder.append("...\n");
        return builder.toString();
    }
}
