package uk.gov.hmcts.reform.pip.data.management.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import uk.gov.hmcts.reform.pip.data.management.validation.validator.LocationMetadataValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = LocationMetadataValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LocationMetadataRequired {
    String message() default "At least one message must be provided "
        + "(cautionMessage, welshCautionMessage, noListMessage, or welshNoListMessage)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
