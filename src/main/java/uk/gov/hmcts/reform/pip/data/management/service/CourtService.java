package uk.gov.hmcts.reform.pip.data.management.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.pip.data.management.database.CourtRepository;
import uk.gov.hmcts.reform.pip.data.management.database.CourtsAndHearings;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CourtNotFoundException;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.CsvParseException;
import uk.gov.hmcts.reform.pip.data.management.models.Court;
import uk.gov.hmcts.reform.pip.data.management.models.CourtMethods;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtCsv;
import uk.gov.hmcts.reform.pip.data.management.models.court.CourtReference;
import uk.gov.hmcts.reform.pip.data.management.models.court.NewCourt;
import uk.gov.hmcts.reform.pip.data.management.models.request.FilterRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service to handle the retrieval and filtering of courts.
 */
@Service
@Slf4j
public class CourtService {

    @Autowired
    private CourtRepository courtRepository;

    /**
     * Gets all courts.

     * @return List of Courts
     */
    public List<NewCourt> getAllCourts() {
        return courtRepository.findAll();
    }

    /**
     * Handles request to search for a court by court id.
     *
     * @param courtId The court ID to search for.
     * @return Court of the found court
     * @throws CourtNotFoundException when no court was found with the given court ID.
     */
    public NewCourt getCourtById(UUID courtId) {
        Optional<NewCourt> foundCourt = courtRepository.getNewCourtByCourtId(courtId);
        if (foundCourt.isEmpty()) {
            throw new CourtNotFoundException(String.format("No court found with the id: %s", courtId));
        } else {
            return foundCourt.get();
        }
    }

    /**
     * Handles request to search for a court by the court name.
     *
     * @param courtName the court name to search for.
     * @return Court of the found court.
     * @throws CourtNotFoundException when no court was found with the given search input.
     */
    public NewCourt getCourtByName(String courtName) {
        Optional<NewCourt> foundCourt = courtRepository.getNewCourtByCourtName(courtName);
        if (foundCourt.isEmpty()) {
            throw new CourtNotFoundException(String.format("No court found with the search: %s", courtName));
        } else {
            return foundCourt.get();
        }
    }

    /**
     * Handles filtering the courts based on region and jurisdiction.
     *
     * @param regions The list of regions to filter against.
     * @param jurisdictions The list of jurisdictions to filter against.
     * @return List of Court objects, can return empty List
     */
    public List<NewCourt> searchByRegionAndJurisdiction(List<String> regions, List<String> jurisdictions) {
        return courtRepository.findByRegionAndJurisdictionOrderByName(
            regions, StringUtils.join(jurisdictions, ','));
    }

    /**
     * This method will upload courts into the database.
     * @param courtList The court list file to upload.
     * @return The collection of new courts that have been created.
     */
    public Collection<NewCourt> uploadCourts(MultipartFile courtList) {

        try {
            Reader reader = new BufferedReader(new InputStreamReader(courtList.getInputStream()));
            CsvToBean<CourtCsv> csvToBean = new CsvToBeanBuilder<CourtCsv>(reader)
                .withType(CourtCsv.class)
                .build();

            List<CourtCsv> courtCsvList = csvToBean.parse();

            Map<Integer, NewCourt> courtCsvMap = new HashMap<>();
            for (CourtCsv courtCsv : courtCsvList) {
                if (courtCsvMap.containsKey(courtCsv.getUniqueId())) {
                    NewCourt newCourt = courtCsvMap.get(courtCsv.getUniqueId());
                    newCourt.addCourtReference(new CourtReference(courtCsv.getProvenance(), courtCsv.getProvenanceId()));
                } else {
                    NewCourt newCourt = new NewCourt(courtCsv);
                    courtCsvMap.put(courtCsv.getUniqueId(), newCourt);
                }
            }

            courtCsvMap.forEach((key, value) -> courtRepository.save(value));
            return courtCsvMap.values();

        } catch (Exception exception) {
            throw new CsvParseException(exception.getMessage());
        }

    }
}
