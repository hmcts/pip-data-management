package uk.gov.hmcts.reform.pip.data.management.models.publication;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtefactTest {

    @Test
    void testIncrement() {
        Artefact artefact = new Artefact();
        assertEquals(0, artefact.getSupersededCount(), "Initial superseded count is not valid");
        artefact.incrementSupersededCount();
        assertEquals(1, artefact.getSupersededCount(), "Incremented superseded count not valid");
    }

}
