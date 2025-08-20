package com.teroblaze.gtitemuntranslator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OriginalLanguageStore {

    private static final String DEBUG_KEY = null; // Specify the key for  debugging
    private static final Map<String, String> ORIGINAL_ENGLISH = new HashMap<>();
    private static boolean isLoaded = false;

    public static void init() {
        if (isLoaded) {
            System.out.println("[GT Item Untranslator] Language store already loaded.");
            return;
        }

        System.out.println("[GT Item Untranslator] Initializing language store...");

        // === Lang-files from jar (en_US.lang and others) ===
        String[] langPaths = { "/assets/gregtech/lang/en_US.lang", "/assets/bartworks/lang/en_US.lang",
            "/assets/gtnhlanth/lang/en_US.lang", "/assets/miscutils/lang/en_US.lang",
            "/assets/tectech/lang/en_US.lang" };

        int totalLoaded = 0;
        for (String langPath : langPaths) {
            int loadedFromFile = 0;
            System.out.println("[GT Item Untranslator] Loading: " + langPath);
            try (InputStream is = OriginalLanguageStore.class.getResourceAsStream(langPath)) {
                if (is == null) {
                    System.out.println("[GT Item Untranslator] Info: Lang file not found in classpath: " + langPath);
                    continue;
                }

                loadedFromFile = loadFromStream(is, langPath);
                System.out.println("[GT Item Untranslator] Loaded " + loadedFromFile + " entries from " + langPath);
                totalLoaded += loadedFromFile;
            } catch (Exception e) {
                System.err.println("[GT Item Untranslator] ERROR loading: " + langPath);
                e.printStackTrace();
            }
        }

        // === Loading GregTech.lang from .minecraft folder ===
        try {
            File mcDir = new File(".");
            File gtLangFile = new File(mcDir, "GregTech.lang");

            if (gtLangFile.exists() && gtLangFile.isFile()) {
                System.out
                    .println("[GT Item Untranslator] Found external GregTech.lang at: " + gtLangFile.getAbsolutePath());
                try (InputStream is = new FileInputStream(gtLangFile)) {
                    int loaded = loadFromStream(is, gtLangFile.getAbsolutePath());
                    totalLoaded += loaded;
                    System.out
                        .println("[GT Item Untranslator] Loaded " + loaded + " entries from external GregTech.lang");
                }
            } else {
                System.out.println(
                    "[GT Item Untranslator] External GregTech.lang not found in .minecraft folder ("
                        + gtLangFile.getAbsolutePath()
                        + ")");
            }
        } catch (Exception e) {
            System.err.println("[GT Item Untranslator] ERROR loading external GregTech.lang from .minecraft");
            e.printStackTrace();
        }

        isLoaded = true;
        System.out.println(
            "[GT Item Untranslator] Initialization complete. Total unique entries: " + ORIGINAL_ENGLISH.size());
    }

    private static int loadFromStream(InputStream is, String source) {
        int loadedFromFile = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("S:")) {
                    line = line.substring(2);
                }

                int eqPos = line.indexOf('=');
                if (eqPos > 0) {
                    String key = line.substring(0, eqPos)
                        .trim();
                    String value = line.substring(eqPos + 1);

                    boolean isDebugKey = DEBUG_KEY != null && DEBUG_KEY.equals(key);
                    if (isDebugKey) {
                        System.out.println(
                            "[GT Item Untranslator] [LOAD DEBUG] " + source
                                + " line "
                                + lineNumber
                                + " -> key='"
                                + key
                                + "', value='"
                                + value
                                + "'");
                    }

                    ORIGINAL_ENGLISH.put(key, value);
                    loadedFromFile++;
                }
            }
        } catch (Exception e) {
            System.err.println("[GT Item Untranslator] ERROR parsing lang file: " + source);
            e.printStackTrace();
        }
        return loadedFromFile;
    }

    public static boolean isInitialized() {
        return isLoaded;
    }

    public static String getOriginal(String key) {
        if (key == null || key.isEmpty()) return key;

        boolean isDebugKey = DEBUG_KEY != null && DEBUG_KEY.equals(key);
        if (isDebugKey) {
            System.out.println("[GT Item Untranslator] [GET DEBUG] getOriginal('" + key + "') called.");
            String value = ORIGINAL_ENGLISH.get(key);
            System.out.println("[GT Item Untranslator] [GET DEBUG] Found = '" + value + "'");
        }

        return ORIGINAL_ENGLISH.getOrDefault(key, key);
    }

    public static void unload() {
        ORIGINAL_ENGLISH.clear();
        isLoaded = false;
        System.out.println("[GT Item Untranslator] Language store unloaded.");
    }
}
