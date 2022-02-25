package uk.gov.hmcts.reform.pip.data.management.models.external;


import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
public class Subscription {

    /**
     * Unique subscription ID.
     */
    private UUID id;

    /**
     * P&I user id.
     */
    private String userId;

    private SearchType searchType;


    private String searchValue;

    private Channel channel;


    private LocalDateTime createdDate = LocalDateTime.now();

    private String caseNumber;

    private String caseName;

    private String urn;

    private String courtName;

}
