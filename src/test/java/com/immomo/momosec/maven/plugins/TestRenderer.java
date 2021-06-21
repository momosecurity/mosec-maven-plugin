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

import com.google.gson.JsonParser;
import com.immomo.momosec.maven.plugins.exceptions.FoundVulnerableException;
import com.immomo.momosec.maven.plugins.exceptions.NetworkErrorException;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


public class TestRenderer {

    @Rule
    @SuppressWarnings(value = {"deprecation"})
    public ExpectedException exceptionRule = ExpectedException.none();

    private final Log log = new DefaultLog(new ConsoleLogger());
    private final MosecLogHelper logHelper = new MosecLogHelper();

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private final String no_vulnerable_response =
        "{" +
        "  \"ok\": true," +
        "  \"dependencyCount\": 3," +
        "  \"vulnerabilities\": []" +
        "}";

    private final String vulnerable_response =
        "{" +
        "  \"ok\": false," +
        "  \"dependencyCount\": 3," +
        "  \"vulnerabilities\": [{" +
        "    \"severity\": \"High\"," +
        "    \"title\": \"Fake Vulnerable\"," +
        "    \"cve\": \"CVE-0001-0001\"," +
        "    \"packageName\": \"com.study.foo:bar\"," +
        "    \"version\": \"1.0.0\"," +
        "    \"target_version\": [\"1.1\"]" +
        "  }]" +
        "}";

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void renderResponseTest_NotFoundVuln() throws Exception {
        Renderer renderer = new Renderer(log, true);
        JsonParser parser = new JsonParser();
        renderer.renderResponse(parser.parse(no_vulnerable_response).getAsJsonObject());

        String expect = "[INFO] " + logHelper.strongInfo("✓ Tested 3 dependencies, no vulnerable found.") + "\n";
        Assert.assertEquals(expect, outContent.toString());
    }

    @Test
    public void renderResponseTest_FoundVulnWithFailOnVuln() throws Exception {
        exceptionRule.expect(FoundVulnerableException.class);
        exceptionRule.expectMessage(Constants.ERROR_ON_VULNERABLE);

        Renderer renderer = new Renderer(log, true);
        JsonParser parser = new JsonParser();
        renderer.renderResponse(parser.parse(vulnerable_response).getAsJsonObject());
    }

    @Test
    public void renderResponseTest_FoundVulnWithoutFailOnVuln() throws Exception {
        Renderer renderer = new Renderer(log, false);
        JsonParser parser = new JsonParser();
        renderer.renderResponse(parser.parse(vulnerable_response).getAsJsonObject());

        String expect =
                "[WARNING] " + logHelper.strongError("✗ High severity (Fake Vulnerable - CVE-0001-0001) found on com.study.foo:bar@1.0.0") + "\n" +
                "[WARNING] " + logHelper.strongInfo("! Fix version [\"1.1\"]") + "\n" +
                "[WARNING] \n" +
                "[WARNING] " + logHelper.strongWarning("Tested 3 dependencies, found 1 vulnerable pathes.") + "\n";
        Assert.assertEquals(expect, outContent.toString());
    }

}
