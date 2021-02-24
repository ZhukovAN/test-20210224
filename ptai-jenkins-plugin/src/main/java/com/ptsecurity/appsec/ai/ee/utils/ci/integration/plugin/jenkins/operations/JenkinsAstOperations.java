package com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.operations;

import com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.JenkinsAstJob;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.utils.RemoteFileUtils;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.domain.Transfers;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.utils.FileCollector;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.v36.Project;
import com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.v36.operations.AstOperations;
import hudson.FilePath;
import hudson.Util;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

@Builder
public class JenkinsAstOperations implements AstOperations {

    @NonNull
    protected final JenkinsAstJob owner;

    @SneakyThrows
    public File createZip() {
        Transfers transfers = new Transfers();

        for (com.ptsecurity.appsec.ai.ee.utils.ci.integration.plugin.jenkins.Transfer transfer : owner.getTransfers())
            transfers.addTransfer(com.ptsecurity.appsec.ai.ee.utils.ci.integration.ptaiserver.domain.Transfer.builder()
                    .excludes(replaceMacro(transfer.getExcludes()))
                    .flatten(transfer.isFlatten())
                    .useDefaultExcludes(transfer.isUseDefaultExcludes())
                    .includes(replaceMacro(transfer.getIncludes()))
                    .patternSeparator(transfer.getPatternSeparator())
                    .removePrefix(replaceMacro(transfer.getRemovePrefix()))
                    .build());
        // Upload project sources
        FilePath remoteZip = RemoteFileUtils.collect(owner.getLauncher(), owner.getListener(), transfers, owner.getWorkspace().getRemote(), owner.isVerbose());
        File zip = FileCollector.createTempFile();
        try (OutputStream fos = new FileOutputStream(zip)) {
            remoteZip.copyTo(fos);
            remoteZip.delete();
        }
        return zip;
    }

    public void scanStartedCallback(@NonNull final Project project, @NonNull UUID scanResultId) {
    }

    public void scanCompleteCallback()  {
    }

    public String replaceMacro(@NonNull String value) {
        return replaceMacro(value, owner.getBuildInfo().getEnvVars());
    }

    public String replaceMacro(@NonNull String value, Map<String, String> replacements) {
        return Util.replaceMacro(value, replacements);
    }
}
