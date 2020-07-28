package com.immomo.momosec.maven.plugins;

public class MosecLogHelper {
    private static final String YELLOW = "\033[1;33m";
    private static final String LIGHT_RED = "\033[1;31m";
    private static final String LIGHT_GREEN = "\033[1;32m";

    private static final String CANCEL_COLOR = "\033[0m";

    public String strongWarning(String content) {
        return YELLOW + content + CANCEL_COLOR;
    }

    public String strongError(String content) {
        return LIGHT_RED + content + CANCEL_COLOR;
    }

    public String strongInfo(String content) {
        return LIGHT_GREEN + content + CANCEL_COLOR;
    }
}
