package uk.gov.hmcts.reform.pip.data.management.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.pip.data.management.models.location.LocationMetadata;
import uk.gov.hmcts.reform.pip.data.management.validation.annotations.LocationMetadataRequired;

public class LocationMetadataValidator implements ConstraintValidator<LocationMetadataRequired, LocationMetadata> {

    @Override
    public boolean isValid(LocationMetadata locationMetadata, ConstraintValidatorContext context) {
        return
            (locationMetadata.getCautionMessage() != null
                && !locationMetadata.getCautionMessage().isEmpty())
                || (locationMetadata.getWelshCautionMessage() != null
                    && !locationMetadata.getWelshCautionMessage().isEmpty())
                || (locationMetadata.getNoListMessage() != null
                    && !locationMetadata.getNoListMessage().isEmpty())
                || (locationMetadata.getWelshNoListMessage() != null
                    && !locationMetadata.getWelshNoListMessage().isEmpty());
    }
}
