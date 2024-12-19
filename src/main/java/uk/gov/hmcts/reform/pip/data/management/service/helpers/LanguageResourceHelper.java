package uk.gov.hmcts.reform.pip.data.management.service.helpers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.pip.model.publication.Language;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

public final class LanguageResourceHelper {
    private static final String PATH_TO_LANGUAGES = "templates/languages/";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LanguageResourceHelper() {
    }

    /**
     * Get language resources for a particular list type.
     *
     * @param listType The list type.
     * @param language The language.
     * @return all language resources for the list.
     * @throws IOException thrown if error in getting language.
     */
    public static Map<String, Object> getLanguageResources(ListType listType, Language language) throws IOException {
        Map<String, Object> languageResources = readResources(listType, language);
        if (listType.getParentListType() != null) {
            Map<String, Object> parentLanguageResources = readResources(listType.getParentListType(), language);
            parentLanguageResources.putAll(languageResources);
            return parentLanguageResources;
        }
        return languageResources;
    }

    private static Map<String, Object> readResources(ListType listType, Language language) throws IOException {
        String resourceName = UPPER_UNDERSCORE.to(LOWER_CAMEL, listType.name());
        Map<String, Object> languageResources = new ConcurrentHashMap<>();
        languageResources.putAll(readResourcesFromPath(resourceName, language));
        return languageResources;
    }

    public static Map<String, Object> readResourcesFromPath(String resourceName, Language language) throws IOException {
        String path =  PATH_TO_LANGUAGES
            + (language.equals(Language.ENGLISH) ? "en/" : "cy/")
            + resourceName
            + ".json";

        try (InputStream languageFile = Thread.currentThread()
            .getContextClassLoader().getResourceAsStream(path)) {
            if (languageFile == null) {
                return Collections.emptyMap();
            }
            return OBJECT_MAPPER.readValue(
                Objects.requireNonNull(languageFile).readAllBytes(), new TypeReference<>() {
                });
        }
    }
}
