package com.ptsecurity.appsec.ai.ee.utils.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Policy {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Scopes {
        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Rules {
            @JsonProperty("Field")
            protected String field;
            @JsonProperty("Value")
            protected String value;
            @JsonProperty("IsRegex")
            protected boolean isRegex;
        }
        @JsonProperty("Rules")
        protected Rules[] rules;
    }
    @JsonProperty("CountToActualize")
    protected int countToActualize;
    @JsonProperty("Scopes")
    protected Scopes[] scopes;

}
