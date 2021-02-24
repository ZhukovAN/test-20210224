package com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.utils;

import com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.exceptions.ApiException;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.v36.Reports;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Base validator class that checks entities syntax and semantics
 */
@Slf4j
public class Validator {
    @Getter
    @Setter
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class Result {
        public static final Result OK = ok(null);

        protected final boolean status;
        protected String details = null;

        public boolean ok() {
            return status;
        }

        public boolean fail() {
            return !status;
        }

        @NonNull
        public static Result ok(final String message) {
            return new Result(true, message);
        }

        @NonNull
        public static Result fail(final String message) {
            return new Result(false, message);
        }
    }

    @NonNull
    public static Result validateNotEmpty(String value) {
        return new Result(!StringUtils.isEmpty(value));
    }

    @NonNull
    protected static Result validateViaException(@NonNull final Runnable call) {
        try {
            call.run();
            return Result.OK;
        } catch (ApiException e) {
            return Result.fail(e.getDetailedMessage());
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    @NonNull
    public static Result validateUrl(String value) {
        return new Result(UrlHelper.checkUrl(value));
    }

    @NonNull
    public static Result validateRegEx(String value) {
        return validateViaException(() -> Pattern.compile(value));
    }

    @NonNull
    public static Result validateJsonSettings(String value) {
        return validateViaException(() -> JsonSettingsHelper.verify(value));
    }

    @NonNull
    public static Result validateJsonPolicy(String value) {
        return validateViaException(() -> { if (validateNotEmpty(value).ok()) JsonPolicyHelper.verify(value); });
    }

    @NonNull
    public static Result validateJsonIssuesFilter(String value) {
        return validateViaException(() -> Reports.validateJsonFilter(value));
    }

    @NonNull
    public static Result validateJsonReports(String value) {
        return validateViaException(() -> Reports.validateJsonReports(value));
    }

    @NonNull
    public static Result validateReports(@NonNull final Reports value) {
        return validateViaException(() -> value.validate());
    }
}
