package com.enioka.scanner.sdk.zebra.dw;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ZebraDwConfig {
    private final String profileName;
    private final Map<String, Map<String, String>> plugins = new HashMap<>();

    ZebraDwConfig(String profileName) {
        this.profileName = profileName;
    }

    void addConfigItem(String pluginName, String key, String value) {
        // Cannot use computeIfAbsent in API 19
        Map<String, String> pluginConfig = plugins.get(pluginName);
        if (pluginConfig == null) {
            pluginConfig = new HashMap<String, String>();
            plugins.put(pluginName, pluginConfig);
        }
        pluginConfig.put(key, value);
    }

    String getParameter(String pluginName, String key) {
        Map<String, String> pluginConfig = plugins.get(pluginName);
        if (pluginConfig == null) {
            return null;
        }
        return pluginConfig.get(key);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Profile configuration");
        sb.append(profileName);
        sb.append("\n");

        for (String pluginName : plugins.keySet()) {
            sb.append("\tPlugin: ");
            sb.append(pluginName);
            sb.append("\n");

            Map<String, String> pluginConfig = plugins.get(pluginName);
            if (pluginConfig == null) {
                continue; // For linter, otherwise stupid.
            }

            List<String> sortedKeys = new ArrayList<>(pluginConfig.keySet());
            Collections.sort(sortedKeys);
            for (String key : sortedKeys) {
                sb.append("\t\t");
                sb.append(key);
                sb.append(" - ");
                sb.append(pluginConfig.get(key));
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}
