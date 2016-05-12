package uk.gov.justice.services.core.dispatcher;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.when;


/**
 * Created by vagrant on 5/11/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TraceLoggerHelperTest {

    private static final String NAME = "test.command.do-something";


    private static final UUID UUID_1 = UUID.fromString("c5b56797-9f5e-4f9e-968b-88802f1e45d5");
    private static final UUID UUID_2 = UUID.fromString("b88d3604-9fe4-458b-b034-1b9480a20230");
    private static final UUID UUID_3 = UUID.fromString("54e170e8-95a1-452e-b8e5-aecee51202b9");

    private List<UUID> causations;

    @Mock
    private JsonEnvelope envelope;

    @Mock
    private Metadata metadata;

    private void makeCausations() {

        List<UUID> response = new ArrayList<>();
        response.add(UUID_1);
        response.add(UUID_2);
        this.causations =  response;
    }

    @Before
    public void setup() {

        makeCausations();

        when(envelope.metadata()).thenReturn(metadata);
        when(metadata.name()).thenReturn(NAME);
        when(metadata.causation()).thenReturn(causations);
        when(metadata.id()).thenReturn(UUID_3);
    }


    @Test
    public void shouldPrintAsTrace() throws Exception {

        String result = TraceLoggerHelper.trace("test", envelope);

        assertThat(result, containsString(NAME));
        assertThat(result, containsString(UUID_1.toString()));
        assertThat(result, containsString(UUID_2.toString()));
        assertThat(result, containsString(UUID_3.toString()));
        assertThat(result, Matchers.containsString(UUID_3.toString()));

    }



}