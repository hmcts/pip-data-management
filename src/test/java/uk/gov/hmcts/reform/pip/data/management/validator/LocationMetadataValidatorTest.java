package uk.gov.hmcts.reform.pip.data.management.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class LocationMetadataValidatorTest {

    private LocationMetadataValidator validator;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @BeforeEach
    void setUp() {
        validator = new LocationMetadataValidator();
        constraintValidatorContext = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testWhenCautionMessageProvided() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setCautionMessage("Test caution message");

        assertTrue(validator.isValid(locationMetadata, constraintValidatorContext),
                   "Validator should return true when cautionMessage is provided");
    }

    @Test
    void testWhenWelshCautionMessageProvided() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setWelshCautionMessage("Test Welsh caution message");

        assertTrue(validator.isValid(locationMetadata, constraintValidatorContext),
                   "Validator should return true when welshCautionMessage is provided");
    }

    @Test
    void testWhenNoListMessageProvided() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setNoListMessage("Test no list message");

        assertTrue(validator.isValid(locationMetadata, constraintValidatorContext),
                   "Validator should return true when noListMessage is provided");
    }

    @Test
    void testWhenWelshNoListMessageProvided() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setWelshNoListMessage("Test Welsh no list message");

        assertTrue(validator.isValid(locationMetadata, constraintValidatorContext),
                   "Validator should return true when welshNoListMessage is provided");
    }

    @Test
    void testWhenMultipleMessagesProvided() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setCautionMessage("Test caution message");
        locationMetadata.setWelshNoListMessage("Test Welsh no list message");

        assertTrue(validator.isValid(locationMetadata, constraintValidatorContext),
                   "Validator should return true when multiple messages are provided");
    }

    @Test
    void testWhenAllMessagesEmpty() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setCautionMessage("");
        locationMetadata.setWelshCautionMessage("");
        locationMetadata.setNoListMessage("");
        locationMetadata.setWelshNoListMessage("");

        assertFalse(validator.isValid(locationMetadata, constraintValidatorContext),
                    "Validator should return false when all messages are empty strings");
    }

    @Test
    void testWhenAllMessagesNull() {
        LocationMetadata locationMetadata = new LocationMetadata();

        assertFalse(validator.isValid(locationMetadata, constraintValidatorContext),
                    "Validator should return false when all messages are null");
    }

    @Test
    void testWhenSomeMessagesEmptySomeNull() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setCautionMessage("");
        locationMetadata.setWelshCautionMessage(null);
        locationMetadata.setNoListMessage("");
        locationMetadata.setWelshNoListMessage(null);

        assertFalse(validator.isValid(locationMetadata, constraintValidatorContext),
                    "Validator should return false when some messages are empty and some are null");
    }

    @Test
    void testWhenOneMessageEmptyOthersNull() {
        LocationMetadata locationMetadata = new LocationMetadata();
        locationMetadata.setCautionMessage("");

        assertFalse(validator.isValid(locationMetadata, constraintValidatorContext),
                    "Validator should return false when one message is empty and others are null");
    }
}
