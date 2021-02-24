package com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.descriptor;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.Messages;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.credentials.Credentials;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.credentials.CredentialsImpl;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.serversettings.ServerSettings;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.utils.Validator;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.v36.Utils;
import hudson.Extension;
import hudson.model.*;
import hudson.model.queue.Tasks;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import java.util.Collections;
import java.util.UUID;

import static com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.v36.Utils.TestResult.State;

@Extension
public class ServerSettingsDescriptor extends Descriptor<ServerSettings> {
    public ServerSettingsDescriptor() {
        super(ServerSettings.class);
    }

    public FormValidation doCheckServerUrl(@QueryParameter("serverUrl") String serverUrl) {
        FormValidation res = Validator.doCheckFieldNotEmpty(serverUrl, Messages.validator_check_field_empty());
        if (FormValidation.Kind.OK != res.kind) return res;
        return Validator.doCheckFieldUrl(serverUrl, Messages.validator_check_url_invalid());
    }

    public static String lowerFirstLetter(@NonNull final String text) {
        if (StringUtils.isEmpty(text)) return "";
        if (1 == text.length()) return text.toLowerCase();
        return String.valueOf(text.charAt(0)).toLowerCase() + text.substring(1);
    }

    public FormValidation doTestServer(
            @AncestorInPath Item item,
            @QueryParameter("serverUrl") final String serverUrl,
            @QueryParameter("serverCredentialsId") final String serverCredentialsId,
            @QueryParameter("serverInsecure") final boolean serverInsecure) {
        try {
            if (!Validator.doCheckFieldNotEmpty(serverUrl))
                throw new RuntimeException(Messages.validator_check_serverUrl_empty());
            boolean urlInvalid = !Validator.doCheckFieldUrl(serverUrl);
            if (!Validator.doCheckFieldNotEmpty(serverCredentialsId))
                throw new RuntimeException(Messages.validator_check_serverCredentialsId_empty());

            Credentials credentials = CredentialsImpl.getCredentialsById(item, serverCredentialsId);

            Utils client = new Utils();
            client.setUrl(serverUrl);
            client.setToken(credentials.getToken().getPlainText());
            if (!StringUtils.isEmpty(credentials.getServerCaCertificates()))
                client.setCaCertsPem(credentials.getServerCaCertificates());
            client.setInsecure(serverInsecure);
            client.init();

            Utils.TestResult res = client.testConnection();

            return State.ERROR.equals(res.state())
                    ? FormValidation.error(res.text())
                    : State.WARNING.equals(res.state())
                    ? FormValidation.warning(res.text())
                    : FormValidation.ok(res.text());
        } catch (Exception e) {
            return Validator.error(e);
        }
    }

    private static final Class<Credentials> BASE_CREDENTIAL_TYPE = Credentials.class;

    // Include any additional contextual parameters that you need in order to refine the
    // credentials list. For example, if the credentials will be used to connect to a remote server,
    // you might include the server URL form element as a @QueryParameter so that the domain
    // requirements can be built from that URL
    public ListBoxModel doFillServerCredentialsIdItems(
            @AncestorInPath Item item,
            @QueryParameter String serverCredentialsId) {
        if (item == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER) ||
                item != null && !item.hasPermission(Item.EXTENDED_READ))
            return new StandardListBoxModel().includeCurrentValue(serverCredentialsId);

        if (item == null)
            // Construct a fake project
            item = new FreeStyleProject((ItemGroup)Jenkins.get(), "fake-" + UUID.randomUUID().toString());
        return new StandardListBoxModel()
                .includeMatchingAs(
                        item instanceof Queue.Task
                                ? Tasks.getAuthenticationOf((Queue.Task) item)
                                : ACL.SYSTEM,
                        item,
                        BASE_CREDENTIAL_TYPE,
                        Collections.<DomainRequirement>emptyList(),
                        CredentialsMatchers.always());
    }
}

