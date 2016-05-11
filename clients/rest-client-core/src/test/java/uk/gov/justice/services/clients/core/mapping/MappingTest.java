package uk.gov.justice.services.clients.core.mapping;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.clients.core.mapping.Mapping.buildMapping;

import javax.naming.NamingException;

import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;

public class MappingTest {

    private Mapping mapping;

    @Before
    public void setup() {
        mapping = buildMapping()
                .withField("Field1", "Value1")
                .withField("Field2", "Value2")
                .withField("Field3", "Value3")
                .build();
    }

    @Test
    public void shouldReturnTheValuesForTheGivenFieldNames() throws Exception {
        assertThat(mapping.get("Field1"), is("Value1"));
        assertThat(mapping.get("Field2"), is("Value2"));
        assertThat(mapping.get("Field3"), is("Value3"));
    }

    @Test
    public void shouldReturnASetOfFieldNamesThatMatchGivenFieldNames() throws Exception {
        assertThat(mapping.getFieldNames(), containsInAnyOrder("Field1", "Field2", "Field3"));
    }

    @Test
    public void shouldReturnOptionalEmptyIfIncorrectFieldNameValueIsRequested() throws Exception {
        assertThat(mapping.get("Incorrect"), nullValue());
    }

    @SuppressWarnings({"squid:MethodCyclomaticComplexity", "squid:S1067", "squid:S00122"})
    @Test
    public void shouldTestEqualsAndHashCode() throws NamingException {
        Mapping mapping1 = buildMapping().withField("field1", "value1").build();
        Mapping mapping2 = buildMapping().withField("field1", "value1").build();
        Mapping mapping3 = buildMapping().withField("field2", "value2").build();

        new EqualsTester()
                .addEqualityGroup(mapping1, mapping2)
                .addEqualityGroup(mapping3)
                .testEquals();
    }

}