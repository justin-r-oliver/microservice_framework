package uk.gov.justice.raml.common.mapper;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MappingParser {

    public static final String OUTPUT_TYPE_FIELD = "responseType";
    public static final String INPUT_TYPE_FIELD = "requestType";
    public static final String NAME_FIELD = "name";

    private static final Pattern MAPPING_SECTION_PATTERN = Pattern
            .compile("(?!\\.\\.\\.)(.*)(?!\\.\\.\\.)", Pattern.DOTALL);
    private static final String FIELD_PATTERN = "%s: (.*)";
    private static final int FIELD_VALUE_GROUP = 1;

    private final List<String> fieldNames;
    private final Map<String, Pattern> patterns;

    public static MappingParser mappingParserForGet() {
        return new MappingParser(OUTPUT_TYPE_FIELD, NAME_FIELD);
    }

    public static MappingParser mappingParserForPost() {
        return new MappingParser(INPUT_TYPE_FIELD, NAME_FIELD);
    }

    private MappingParser(final String... fieldNames) {
        this.fieldNames = asList(fieldNames);
        this.patterns = this.fieldNames.stream()
                .collect(toMap(fieldName -> fieldName, this::patternForField));
    }

    public Map<String, Mapping> parseFromDescription(final String description) {
        if (description == null) {
            return Collections.emptyMap();
        }

        final Matcher matcher = MAPPING_SECTION_PATTERN.matcher(description);

        matcher.find();
        return range(0, matcher.groupCount())
                .mapToObj(index -> parseMappingSection(matcher.group(index)))
                .flatMap(mappingStream -> mappingStream)
                .collect(toMap(this::keyFromMapping, mapping -> mapping));
    }

    private String keyFromMapping(final Mapping mapping) {
        final String inputTypeValue = mapping.get(INPUT_TYPE_FIELD);
        if(inputTypeValue != null) {
            return inputTypeValue;
        } else {
            return mapping.get(OUTPUT_TYPE_FIELD);
        }
    }

    private Stream<Mapping> parseMappingSection(final String mappingSection) {
        return stream(mappingSection.split("\\(mapping\\):"))
                .filter(messagePart -> messagePart.contains(fieldNames.get(0)))
                .map(this::parseMapping);
    }

    private Mapping parseMapping(final String mappingPart) {
        final Mapping.Builder builder = Mapping.buildMapping();

        fieldNames.stream().forEach(fieldName -> {
            final String fieldValue = parseFieldFrom(fieldName, mappingPart)
                    .orElseThrow(() -> new IllegalStateException(format("No %s: field set in description", fieldName)));
            builder.withField(fieldName, fieldValue);
        });

        return builder.build();
    }

    private Optional<String> parseFieldFrom(final String fieldName, final String mappingPart) {
        final Matcher matcher = patterns.get(fieldName).matcher(mappingPart);

        if (matcher.find()) {
            return Optional.of(matcher.group(FIELD_VALUE_GROUP));
        }

        return Optional.empty();
    }

    private Pattern patternForField(final String fieldName) {
        return Pattern.compile(format(FIELD_PATTERN, fieldName));
    }
}
