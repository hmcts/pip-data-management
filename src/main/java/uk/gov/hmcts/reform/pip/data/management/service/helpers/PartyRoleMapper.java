package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public final class PartyRoleMapper {
    private static final Map<String, List<String>> MAPPINGS = Map.of(
        "APPLICANT_PETITIONER", List.of("APL", "APP", "CLP20", "CRED", "OTH", "PET"),
        "APPLICANT_PETITIONER_REPRESENTATIVE", List.of("CREP", "CREP20"),
        "RESPONDENT", List.of("DEBT", "DEF", "DEF20", "RES"),
        "RESPONDENT_REPRESENTATIVE", List.of("DREP", "DREP20", "RREP"),
        "PROSECUTING_AUTHORITY", emptyList(),
        "DEFENDANT", emptyList(),
        "CLAIMANT_PETITIONER", emptyList(),
        "CLAIMANT_PETITIONER_REPRESENTATIVE", emptyList(),
        "DEFENDANT_REPRESENTATIVE", emptyList()
    );

    private PartyRoleMapper() {
    }

    public static String convertPartyRole(String nonConvertedPartyRole) {
        for (Map.Entry<String, List<String>> partyRole : MAPPINGS.entrySet()) {
            if (partyRole.getKey().equals(nonConvertedPartyRole)
                || partyRole.getValue().contains(nonConvertedPartyRole)) {
                return partyRole.getKey();
            }
        }

        return  "";
    }
}
