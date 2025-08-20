package com.teroblaze.gtitemuntranslator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OriginalLanguageStore {

    // --- КОНФИГУРАЦИЯ ОТЛАДКИ ---
    private static final String DEBUG_KEY = null; // Установите сюда ключ для точечной отладки
    // --- КОНЕЦ КОНФИГУРАЦИИ ---

    private static final Map<String, String> ORIGINAL_ENGLISH = new HashMap<>();
    private static boolean isLoaded = false;

    public static void init() {
        if (isLoaded) {
            System.out.println("[GT Item Untranslator] Language store already loaded.");
            return;
        }

        System.out.println("[GT Item Untranslator] Initializing language store...");
        // Пути к оригинальным .lang файлам.
        String[] langPaths = {
            "/assets/gregtech/lang/en_US.lang",
            "/assets/gregtech/lang/GregTech.lang",
            "/assets/bartworks/lang/en_US.lang",
            "/assets/gtnhlanth/lang/en_US.lang",
            "/assets/miscutils/lang/en_US.lang",
            "/assets/tectech/lang/en_US.lang"
        };

        int totalLoaded = 0;
        for (String langPath : langPaths) {
            int loadedFromFile = 0;
            System.out.println("[GT Item Untranslator] Loading: " + langPath);
            try (InputStream is = OriginalLanguageStore.class.getResourceAsStream(langPath)) {
                if (is == null) {
                    System.out.println("[GT Item Untranslator] Info: Lang file not found in classpath: " + langPath);
                    continue;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    int lineNumber = 0;
                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;

                        String processedLine = line;
                        if (line.startsWith("S:")) {
                            processedLine = line.substring(2); // убираем префикс "S:"
                        }

                        int eqPos = processedLine.indexOf('=');
                        if (eqPos > 0) {
                            String key = processedLine.substring(0, eqPos).trim();
                            String value = processedLine.substring(eqPos + 1);

                            boolean isDebugKey = DEBUG_KEY != null && DEBUG_KEY.equals(key);
                            if (isDebugKey) {
                                System.out.println("[GT Item Untranslator] [LOAD DEBUG] " + langPath + " line " + lineNumber
                                    + " -> key='" + key + "', value='" + value + "'");
                            }

                            ORIGINAL_ENGLISH.put(key, value);
                            loadedFromFile++;
                        }
                    }
                }
                System.out.println("[GT Item Untranslator] Loaded " + loadedFromFile + " entries from " + langPath);
                totalLoaded += loadedFromFile;
            } catch (Exception e) {
                System.err.println("[GT Item Untranslator] ERROR loading: " + langPath);
                e.printStackTrace();
            }
        }

        isLoaded = true;
        System.out.println("[GT Item Untranslator] Initialization complete. Total unique entries: " + ORIGINAL_ENGLISH.size());
    }

    public static boolean isInitialized() {
        return isLoaded;
    }

    /**
     * Получает оригинальное английское значение по ключу.
     * @param key Ключ локализации (например, "gt.metaitem.01.11305.name").
     * @return Оригинальное значение из .lang файла или сам ключ, если не найден.
     */
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
