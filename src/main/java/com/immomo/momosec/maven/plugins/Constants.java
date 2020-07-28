package com.immomo.momosec.maven.plugins;

public class Constants {

    public static final String ERROR_GENERAL = "There was a problem with the Mosec plugin.";

    public static final String ERROR_RERUN_WITH_DEBUG = "Re-run Maven using the -X switch to enable full debug logging.";

    public static final String ERROR_ON_VULNERABLE = "Dependency Vulnerable Found!";

    public static final String ERROR_ON_API = "API return data format error.";

    public static final String ERROR_ON_NULL_ENDPOINT = "API endpoint not setting. Setting by <endpoint></endpoint> or MOSEC_ENDPOINT env.";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String PROJECT_LANGUAGE = "java";

    public static final String BUILD_TOOL_TYPE = "Maven";

    public static final String MOSEC_ENDPOINT_ENV = "MOSEC_ENDPOINT";
}
