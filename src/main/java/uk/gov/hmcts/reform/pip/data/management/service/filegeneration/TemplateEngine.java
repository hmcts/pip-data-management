package uk.gov.hmcts.reform.pip.data.management.service.filegeneration;

import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import uk.gov.hmcts.reform.pip.data.management.config.ThymeleafConfiguration;
import uk.gov.hmcts.reform.pip.model.publication.ListType;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;

public final class TemplateEngine {
    private static final SpringTemplateEngine TEMPLATE_ENGINE = new ThymeleafConfiguration().templateEngine();

    private TemplateEngine() {
    }

    static String processTemplate(String listTypeName, IContext context) {
        String template = UPPER_UNDERSCORE.to(LOWER_CAMEL, getTemplateFileName(listTypeName)) + ".html";
        return TEMPLATE_ENGINE.process(template, context);
    }

    static String processNonStrategicTemplate(String listTypeName, IContext context) {
        String template = "non-strategic/"
            + UPPER_UNDERSCORE.to(LOWER_CAMEL, getTemplateFileName(listTypeName))
            + ".html";
        return TEMPLATE_ENGINE.process(template, context);
    }

    private static String getTemplateFileName(String listTypeName) {
        ListType listType = ListType.valueOf(listTypeName);
        return listType.getParentListType() == null
            ? listTypeName
            : listType.getParentListType().name();
    }
}
