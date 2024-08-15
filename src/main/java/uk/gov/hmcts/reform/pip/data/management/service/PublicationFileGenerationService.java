package uk.gov.hmcts.reform.pip.data.management.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.util.XRLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.pip.data.management.errorhandling.exceptions.ProcessingException;
import uk.gov.hmcts.reform.pip.data.management.models.PublicationFiles;
import uk.gov.hmcts.reform.pip.data.management.models.location.Location;
import uk.gov.hmcts.reform.pip.data.management.models.publication.Artefact;
import uk.gov.hmcts.reform.pip.data.management.service.filegeneration.FileConverter;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.DateHelper;
import uk.gov.hmcts.reform.pip.data.management.service.helpers.LanguageResourceHelper;
import uk.gov.hmcts.reform.pip.data.management.service.publication.ArtefactService;
import uk.gov.hmcts.reform.pip.model.publication.Language;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@SuppressWarnings("PMD.PreserveStackTrace")
public class PublicationFileGenerationService {
    private static final int MAX_FILE_SIZE = 2_000_000;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ArtefactService artefactService;
    private final LocationService locationService;
    private final ListConversionFactory listConversionFactory;

    @Value("${pdf.font}")
    private String pdfFont;

    @Autowired
    public PublicationFileGenerationService(ArtefactService artefactService,
                                            LocationService locationService,
                                            ListConversionFactory listConversionFactory) {
        this.artefactService = artefactService;
        this.locationService = locationService;
        this.listConversionFactory = listConversionFactory;
        XRLog.setLoggerImpl(new Slf4jLogger());
    }

    public static String maskDataSourceName(String provenance) {
        return "SNL".equals(provenance) ? "ListAssist" : provenance;
    }

    /**
     * Generate publication files for a given artefact.
     *
     * @param artefactId The artefact ID to generate the files for.
     * @param payload The payload of the artefact.
     * @return all generated files (primary PDF + additional PDF + Excel).
     * @throws ProcessingException error.
     */
    public Optional<PublicationFiles> generate(UUID artefactId, String payload) {
        String rawJson = payload == null ? artefactService.getPayloadByArtefactId(artefactId) : payload;
        Artefact artefact = artefactService.getMetadataByArtefactId(artefactId);
        Location location = locationService.getLocationById(Integer.valueOf(artefact.getLocationId()));
        JsonNode topLevelNode;

        try {
            topLevelNode = MAPPER.readTree(rawJson);
            FileConverter fileConverter = listConversionFactory.getFileConverter(artefact.getListType());

            if (fileConverter == null) {
                log.error("Failed to find converter for list type");
                return Optional.empty();
            }

            byte[] excel = fileConverter.convertToExcel(topLevelNode, artefact.getListType());

            // Generate the English and/or Welsh PDFs and store in Azure blob storage
            Pair<byte[], byte[]> pdfs = generatePdfs(topLevelNode, artefact, location);
            return Optional.of(new PublicationFiles(pdfs.getLeft(), pdfs.getRight(), excel));

        } catch (IOException ex) {
            throw new ProcessingException(String.format("Failed to generate files for artefact id %s", artefactId));
        }
    }

    /**
     * Generate the English and/or Welsh PDF for a given artefact.
     *
     * @param topLevelNode The data node.
     * @param artefact The artefact.
     * @param location The location.
     * @return a byte array of the generated pdf.
     * @throws IOException error.
     */
    private Pair<byte[], byte[]> generatePdfs(JsonNode topLevelNode, Artefact artefact, Location location)
        throws IOException {
        Language language = artefact.getLanguage();

        if (artefact.getListType().hasAdditionalPdf() && language != Language.ENGLISH) {
            byte[] englishPdf = generatePdf(topLevelNode, artefact, location, Language.ENGLISH, true);
            if (englishPdf.length > MAX_FILE_SIZE) {
                englishPdf = generatePdf(topLevelNode, artefact, location, Language.ENGLISH, false);
            }

            byte[] welshPdf = generatePdf(topLevelNode, artefact, location, Language.WELSH, true);
            if (welshPdf.length > MAX_FILE_SIZE) {
                welshPdf = generatePdf(topLevelNode, artefact, location, Language.WELSH, false);
            }

            return Pair.of(englishPdf, welshPdf);
        }

        byte[] pdf = generatePdf(topLevelNode, artefact, location, language, true);
        if (pdf.length > MAX_FILE_SIZE) {
            pdf = generatePdf(topLevelNode, artefact, location, language, false);
        }

        return Pair.of(pdf, new byte[0]);
    }

    /**
     * Generate the PDF for a given artefact.
     *
     * @param topLevelNode The data node.
     * @param artefact The artefact.
     * @param location The location where the artefact is uploaded to.
     * @param language The language of the artefact.
     * @param accessibility If the pdf should be accessibility generated.
     * @return a byte array of the generated pdf.
     * @throws IOException Throw if error generating.
     */
    private byte[] generatePdf(JsonNode topLevelNode, Artefact artefact, Location location, Language language,
                               boolean accessibility) throws IOException {
        Map<String, Object> languageResource = LanguageResourceHelper.getLanguageResources(
            artefact.getListType(), language);
        String html = listConversionFactory.getFileConverter(artefact.getListType())
            .convert(topLevelNode, buildArtefactMetadata(artefact, location, language), languageResource);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode()
                .usePdfAConformance(PdfRendererBuilder.PdfAConformance.PDFA_1_A);

            File file = new File(pdfFont);
            if (file.exists()) {
                builder.useFont(file, "openSans");
            } else {
                builder.useFont(new File(Thread.currentThread().getContextClassLoader()
                                             .getResource("openSans.ttf").getFile()), "openSans");
            }
            builder.usePdfUaAccessbility(accessibility);
            builder.withHtmlContent(html, null)
                .toStream(baos)
                .run();
            return baos.toByteArray();
        }
    }

    private Map<String, String> buildArtefactMetadata(Artefact artefact, Location location, Language language) {
        String locationName = (language == Language.ENGLISH) ? location.getName() : location.getWelshName();
        String provenance = maskDataSourceName(artefact.getProvenance());
        return Map.of(
            "contentDate", DateHelper.formatLocalDateTimeToBst(artefact.getContentDate()),
            "provenance", provenance,
            "locationName", locationName,
            "region", String.join(", ", location.getRegion()),
            "regionName", String.join(", ", location.getRegion()),
            "language", language.toString(),
            "listType", artefact.getListType().name()
        );
    }

}