package uk.gov.hmcts.reform.pip.data.management.models.external;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The subscription model as per Subscription Management.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    /**
     * Unique subscription ID.
     */
    private UUID id;

    /**
     * P&I user id.
     */
    private String userId;

    /**
     * Search type of the subscription.
     */
    private SearchType searchType;

    /**
     * Search value relating to the search type of the subscription.
     */
    private String searchValue;

    /**
     * The channel the subscriber is to be informed on.
     */
    private Channel channel;

    /**
     * The date the subscription was created.
     */
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdDate;

    /**
     * The case number of the subscription.
     */
    private String caseNumber;

    /**
     * The case name of the subscription.
     */
    private String caseName;

    /**
     * The case urn of the subscription.
     */
    private String urn;

    /**
     * The court name of the subscription.
     */
    private String courtName;

}
