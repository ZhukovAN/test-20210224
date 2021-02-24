package com.ptsecurity.appsec.ai.ee.utils.ci.integration.cli.utils;

import picocli.CommandLine;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestVersionProvider implements CommandLine.IVersionProvider {
    public String[] getVersion() throws Exception {
        Enumeration<URL> res = CommandLine.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
        while (res.hasMoreElements()) {
            URL url = res.nextElement();
            try {
                Manifest manifest = new Manifest(url.openStream());
                if (isApplicableManifest(manifest)) {
                    Attributes attr = manifest.getMainAttributes();
                    return new String[] { new StringBuilder()
                            .append(get(attr, "Implementation-Title"))
                            .append(" v.")
                            .append(get(attr, "Implementation-Version"))
                            .append(" build ")
                            .append(get(attr, "Implementation-Build")).toString() };
                }
            } catch (IOException ex) {
                return new String[] { "Unable to read from " + url + ": " + ex };
            }
        }
        return new String[0];
    }

    private boolean isApplicableManifest(Manifest manifest) {
        Attributes attributes = manifest.getMainAttributes();
        return "ptai-cli-plugin".equals(get(attributes, "Implementation-Title"));
    }

    private static Object get(Attributes attributes, String key) {
        return attributes.get(new Attributes.Name(key));
    }
}