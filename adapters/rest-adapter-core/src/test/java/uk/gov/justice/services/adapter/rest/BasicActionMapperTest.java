package uk.gov.justice.services.adapter.rest;


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.adapters.test.utils.builder.HeadersBuilder.headersWith;

import org.junit.Test;

public class BasicActionMapperTest {

    @Test
    public void shouldReturnActionForGETRequests() throws Exception {
        TestActionMapper mapping = new TestActionMapper();

        assertThat(mapping.actionOf("methodA", "GET",
                headersWith("Accept", "application/vnd.blah+json")), is("actionNameA"));
        assertThat(mapping.actionOf("methodA", "GET",
                headersWith("Accept", "application/vnd.blah2+json")), is("actionNameB"));
        assertThat(mapping.actionOf("methodA", "GET",
                headersWith("Accept", "application/vnd.blah3+json")), is("actionNameB"));
        assertThat(mapping.actionOf("methodB", "GET",
                headersWith("Accept", "application/vnd.blah+json")), is("actionNameC"));
    }

    @Test
    public void shouldReturnActionForPOSTRequests() throws Exception {
        TestActionMapper mapping = new TestActionMapper();

        assertThat(mapping.actionOf("methodA", "POST",
                headersWith("Content-Type", "application/vnd.blah+json")), is("actionNameA"));
        assertThat(mapping.actionOf("methodA", "POST",
                headersWith("Content-Type", "application/vnd.blah2+json")), is("actionNameB"));
        assertThat(mapping.actionOf("methodA", "POST",
                headersWith("Content-Type", "application/vnd.blah3+json")), is("actionNameB"));
        assertThat(mapping.actionOf("methodB", "POST",
                headersWith("Content-Type", "application/vnd.blah+json")), is("actionNameC"));


    }


    private static class TestActionMapper extends BasicActionMapper {

        public TestActionMapper() {
            add("methodA", "application/vnd.blah+json", "actionNameA");
            add("methodA", "application/vnd.blah2+json", "actionNameB");
            add("methodA", "application/vnd.blah3+json", "actionNameB");
            add("methodB", "application/vnd.blah+json", "actionNameC");
        }


    }
}