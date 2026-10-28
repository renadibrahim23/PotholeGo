package com.community.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Reads settings from real environment variables first, then from a .env file
// in the working directory (the PotholeGo folder when run from IntelliJ or mvnw).
public final class EnvConfig {
    private static Map<String, String> dotEnv;

    private EnvConfig() {
    }

    public static String get(String name) {
        String value = System.getenv(name);
        if (value != null && !value.isBlank()) {
            return value;
        }
        value = dotEnv().get(name);
        return value == null || value.isBlank() ? null : value;
    }

    private static synchronized Map<String, String> dotEnv() {
        if (dotEnv == null) {
            dotEnv = new HashMap<>();
            Path file = Path.of(".env");
            if (Files.isRegularFile(file)) {
                try {
                    parse(Files.readAllLines(file));
                } catch (IOException e) {
                    throw new IllegalStateException("Could not read " + file.toAbsolutePath(), e);
                }
            }
        }
        return dotEnv;
    }

    private static void parse(List<String> lines) {
        for (String line : lines) {
            String l = line.trim();
            if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) {
                continue;
            }
            String key = l.substring(0, l.indexOf('=')).trim();
            String value = l.substring(l.indexOf('=') + 1).trim();
            if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                    || value.startsWith("'") && value.endsWith("'"))) {
                value = value.substring(1, value.length() - 1);
            }
            dotEnv.put(key, value);
        }
    }
}
