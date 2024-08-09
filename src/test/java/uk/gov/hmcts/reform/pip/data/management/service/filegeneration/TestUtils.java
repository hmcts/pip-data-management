package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public final class TestUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String LANGUAGE_FILE_PATH = "templates/languages/";

    private TestUtils() {
    }

    public static Map<String, Object> getLanguageResources(ListType listType, String language) throws IOException {
        Map<String, Object> languageResources = readResources(listType, language);

        if (listType.getParentListType() != null) {
            Map<String, Object> parentLanguageResources = readResources(listType.getParentListType(), language);
            parentLanguageResources.putAll(languageResources);
            return parentLanguageResources;
        }
        return languageResources;
    }

    private static Map<String, Object> readResources(ListType listType, String language) throws IOException {
        String languageFileName = UPPER_UNDERSCORE.to(LOWER_CAMEL, listType.name());

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(LANGUAGE_FILE_PATH + language + "/" + languageFileName + ".json")) {
            return OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }
}
