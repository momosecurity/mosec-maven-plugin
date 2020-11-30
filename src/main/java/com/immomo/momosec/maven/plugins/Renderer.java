/*
 * Copyright 2020 momosecurity.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.immomo.momosec.maven.plugins;

import com.google.gson.*;
import com.immomo.momosec.maven.plugins.exceptions.FoundVulnerableException;
import com.immomo.momosec.maven.plugins.exceptions.NetworkErrorException;
import org.apache.maven.plugin.logging.Log;

import java.io.*;

public class Renderer {

    private final MosecLogHelper logHelper = new MosecLogHelper();

    private final Log log;
    private final Boolean failOnVuln;

    public Renderer(Log log, Boolean failOnVuln) {
        this.log = log;
        this.failOnVuln = failOnVuln;
    }

    public void renderResponse(InputStream in) throws NetworkErrorException, FoundVulnerableException {
        JsonParser parser = new JsonParser();
        JsonObject responseJson;
        try {
            responseJson = parser.parse(new BufferedReader(new InputStreamReader(in))).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            throw new NetworkErrorException(Constants.ERROR_ON_API);
        }

        if(responseJson.get("ok") != null && responseJson.get("ok").getAsBoolean()) {
            String ok = "✓ Tested %s dependencies, no vulnerable found.";
            getLog().info(logHelper.strongInfo(String.format(ok, responseJson.get("dependencyCount").getAsString())));
        } else if (responseJson.get("vulnerabilities") != null) {
            JsonArray vulns = responseJson.get("vulnerabilities").getAsJsonArray();

            for (JsonElement vuln : vulns) {
                printSingleVuln(vuln.getAsJsonObject());
            }

            String fail = "Tested %s dependencies, found %d vulnerable pathes.";
            getLog().warn(logHelper.strongWarning(String.format(fail, responseJson.get("dependencyCount").getAsString(), vulns.size())));
            if (failOnVuln) {
                throw new FoundVulnerableException(Constants.ERROR_ON_VULNERABLE);
            }
        }
    }

    private void printSingleVuln(JsonObject vuln) {
        String vuln_warn = "✗ %s severity (%s - %s) found on %s@%s";
        getLog().warn(logHelper.strongError(String.format(vuln_warn,
                vuln.get("severity").getAsString(),
                vuln.get("title").getAsString(),
                vuln.get("cve").getAsString(),
                vuln.get("packageName").getAsString(),
                vuln.get("version").getAsString()
        )));
        if(vuln.get("from") != null) {
            JsonArray fromArr = vuln.get("from").getAsJsonArray();
            StringBuilder fromStrb = new StringBuilder();
            for(int i = 0; i < fromArr.size(); i++) {
                fromStrb.append(fromArr.get(i).getAsString());
                fromStrb.append(" > ");
            }
            getLog().warn(String.format("- Path: %s" ,fromStrb.substring(0, fromStrb.length() - 3)));
        }
        if (vuln.get("target_version").getAsJsonArray().size() >= 0) {
            getLog().warn(logHelper.strongInfo(String.format("! Fix version %s", vuln.get("target_version").getAsJsonArray())));
        }
        getLog().warn("");
    }

    private Log getLog() {
        return log;
    }

    public static void writeToFile(String filename, String jsonTree) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            File dir = new File(file.getAbsoluteFile().getParent());
            dir.mkdirs();
            file.createNewFile();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(jsonTree.getBytes());
        outputStream.close();
    }
}
