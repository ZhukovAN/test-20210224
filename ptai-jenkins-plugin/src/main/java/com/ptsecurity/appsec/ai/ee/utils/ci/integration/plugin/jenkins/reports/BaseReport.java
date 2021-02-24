package com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.reports;

import com.ptsecurity.appsec.ai.ee.utils.ci.integration.Resources;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.utils.Validator;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.exceptions.ApiException;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.v36.Reports;
import hudson.DescriptorExtensionList;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jvnet.localizer.LocaleProvider;
import org.kohsuke.stapler.QueryParameter;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public abstract class BaseReport extends AbstractDescribableImpl<BaseReport> implements Serializable, Cloneable {
    @Getter
    private static final DescriptorExtensionList<BaseReport, BaseReportDescriptor> all =
            DescriptorExtensionList.createDescriptorList(Jenkins.get(), BaseReport.class);

    /**
     * Method converts list of miscellaneous report defined for a job to Reports
     * instance. If there were conversion errors like JSON parse fail,
     * an ApiException will be thrown
     * @param reports List of miscellaneous reports defined for a job
     * @return Reports instance that containt all the reports defined for a job
     * @throws ApiException Exception that contains error details
     */
    public static Reports convert(final List<BaseReport> reports) throws ApiException {
        if (null == reports || reports.isEmpty()) return null;
        Reports res = new Reports();
        for (BaseReport r : reports) {
            if (r instanceof Data) {
                Data data = (Data) r;
                Reports.Data item = new Reports.Data();
                item.setFormat(Reports.Data.Format.valueOf(data.getFormat()));
                item.setLocale(Reports.Locale.valueOf(data.getLocale()));
                item.setFileName(data.getFileName());
                if (StringUtils.isNotEmpty(data.getFilter()))
                    item.setFilters(Reports.validateJsonFilter(data.getFilter()));
                res.getData().add(item);
            } else if (r instanceof Report) {
                Report data = (Report) r;
                Reports.Report item = new Reports.Report();
                item.setFormat(Reports.Report.Format.valueOf(data.getFormat()));
                item.setLocale(Reports.Locale.valueOf(data.getLocale()));
                item.setFileName(data.getFileName());
                item.setTemplate(data.getTemplate());
                if (StringUtils.isNotEmpty(data.getFilter()))
                    item.setFilters(Reports.validateJsonFilter(data.getFilter()));
                res.getReport().add(item);
            } else if (r instanceof RawData) {
                RawData data = (RawData) r;
                Reports.RawData item = new Reports.RawData();
                item.setFileName(data.getFileName());
                res.getRaw().add(item);
            } else if (r instanceof Json) {
                Json json = (Json) r;
                res.append(Reports.validateJsonReports(json.getJson()));
            }
        }
        return res;
    }

    public static abstract class BaseReportDescriptor extends Descriptor<BaseReport> {

        public FormValidation doCheckFileName(@QueryParameter("fileName") String fileName) {
            return Validator.doCheckFieldNotEmpty(fileName, Resources.validator_check_field_empty());
        }

        public FormValidation doCheckTemplate(@QueryParameter("template") String template) {
            return Validator.doCheckFieldNotEmpty(template, Resources.validator_check_field_empty());
        }

        public FormValidation doCheckFilter(@QueryParameter("filter") String filter) {
            if (Validator.doCheckFieldNotEmpty(filter))
                return Validator.doCheckFieldJsonIssuesFilter(filter, Resources.i18n_validator_reporting_issuesfilter_incorrect());
            else
                return FormValidation.ok();
        }

        public String getDefaultLocale() {
            Locale locale = LocaleProvider.getLocale();
            if (locale.getLanguage().equalsIgnoreCase(Reports.Locale.RU.name()))
                return Reports.Locale.RU.name();
            else
                return Reports.Locale.EN.name();
        }
    }

    @Override
    public BaseReport clone() throws CloneNotSupportedException {
        return (BaseReport) super.clone();
    }
}
