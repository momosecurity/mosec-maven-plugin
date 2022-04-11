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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.DependencyNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClientHelper.class, MosecTest.class})
public class TestMosecTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Rule
    public TestResources resources = new TestResources("src/test/resources/projects", "target/test-projects");

    @Rule
    @SuppressWarnings(value = {"deprecation"})
    public ExpectedException exceptionRule = ExpectedException.none();


    @Test
    public void invalidProjectTest() throws Exception {
        File projectCopy = this.resources.getBasedir("empty-dir");
        File pom = new File(projectCopy, "pom.xml");

        exceptionRule.expect(java.io.FileNotFoundException.class);
        exceptionRule.expectMessage("(No such file or directory)");

        this.rule.lookupMojo("test", pom.getCanonicalPath());
    }

    @Test
    public void validProjectTest() throws Exception {
        File pom = getPom("valid-project", "pom.xml");

        MosecTest mosecTest = (MosecTest)this.rule.lookupMojo("test", pom);
        Assert.assertNotNull(mosecTest);
    }

    @Test
    public void onlyAnalyzeWithoutEndpointPom() throws Exception {
        File pom = getPom("valid-project", "onlyAnalyzeWithoutEndpointPom.xml");

        MosecTest mosecTest = spy((MosecTest) this.rule.lookupMojo("test", pom));

        RepositorySystem mockRepositorySystem = mock(RepositorySystem.class);
        CollectResult mockCollectResult = mock(CollectResult.class);
        DependencyNode mockRoot = new DefaultDependencyNode(
                new DefaultArtifact("com.immomo.momosec", "MyTestProject", "jar", "1.0.0"));

        when(mosecTest.getLog()).thenReturn(mock(Log.class));
        when(mockRepositorySystem.collectDependencies(any(), any())).thenReturn(mockCollectResult);
        when(mockCollectResult.getRoot()).thenReturn(mockRoot);

        Field repoSystemField = mosecTest.getClass().getDeclaredField("repositorySystem");
        repoSystemField.setAccessible(true);
        repoSystemField.set(mosecTest, mockRepositorySystem);

        mosecTest.execute();
    }

    @Test
    public void onlyAnalyzeWithEndpointPom() throws Exception {
        File pom = getPom("valid-project", "onlyAnalyzeWithEndpointPom.xml");

        MosecTest mosecTest = spy((MosecTest) this.rule.lookupMojo("test", pom));

        RepositorySystem mockRepositorySystem = mock(RepositorySystem.class);
        CollectResult mockCollectResult = mock(CollectResult.class);
        DependencyNode mockRoot = new DefaultDependencyNode(
                new DefaultArtifact("com.immomo.momosec", "MyTestProject", "jar", "1.0.0"));

        when(mosecTest.getLog()).thenReturn(mock(Log.class));
        when(mockRepositorySystem.collectDependencies(any(), any())).thenReturn(mockCollectResult);
        when(mockCollectResult.getRoot()).thenReturn(mockRoot);

        Field repoSystemField = mosecTest.getClass().getDeclaredField("repositorySystem");
        repoSystemField.setAccessible(true);
        repoSystemField.set(mosecTest, mockRepositorySystem);

        mosecTest.execute();
    }

    @Test
    public void testFailOnVulnWithTruePom() throws Exception {
        File pom = getPom("valid-project", "failOnVulnWithTruePom.xml");
        exceptionRule.expectMessage("Dependency Vulnerable Found!");
        failOnVulnPomRunner(pom);
    }

    @Test
    public void testFailOnVulnWithFalsePom() throws Exception {
        File pom = getPom("valid-project", "failOnVulnWithFalsePom.xml");
        failOnVulnPomRunner(pom);
    }

    private void failOnVulnPomRunner(File pom) throws Exception {
        MosecTest mosecTest = spy((MosecTest) this.rule.lookupMojo("test", pom));

        RepositorySystem mockRepositorySystem = mock(RepositorySystem.class);
        CollectResult mockCollectResult = mock(CollectResult.class);
        DependencyNode mockRoot = new DefaultDependencyNode(
                new DefaultArtifact("com.immomo.momosec", "MyTestProject", "jar", "1.0.0"));
        HttpClientHelper mockHttpClientHelper = mock(HttpClientHelper.class);
        HttpClient mockHttpClient = mock(HttpClient.class);
        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);
        HttpEntity mockHttpEntity = mock(HttpEntity.class);

        when(mosecTest.getLog()).thenReturn(mock(Log.class));
        when(mockRepositorySystem.collectDependencies(any(), any())).thenReturn(mockCollectResult);
        when(mockCollectResult.getRoot()).thenReturn(mockRoot);
        whenNew(HttpClientHelper.class).withAnyArguments().thenReturn(mockHttpClientHelper);
        when(mockHttpClientHelper.buildHttpClient()).thenReturn(mockHttpClient);
        when(mockHttpClient.execute(any())).thenReturn(mockHttpResponse);
        when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        String vuln = "{\"ok\":false, \"dependencyCount\": 2, \"vulnerabilities\":[{" +
                "\"severity\": \"High\"," +
                "\"title\": \"Fastjson RCE\"," +
                "\"cve\": \"CVE-0000-0001\"," +
                "\"packageName\": \"com.alibaba:fastjson\"," +
                "\"version\": \"1.2.33\"," +
                "\"target_version\": [\"1.2.80\"]" +
                "}]}";
        InputStream httpResponseContent = new ByteArrayInputStream(vuln.getBytes());
        when(mockHttpResponse.getEntity()).thenReturn(mockHttpEntity);
        when(mockHttpEntity.getContent()).thenReturn(httpResponseContent);

        Field repoSystemField = mosecTest.getClass().getDeclaredField("repositorySystem");
        repoSystemField.setAccessible(true);
        repoSystemField.set(mosecTest, mockRepositorySystem);

        mosecTest.execute();
    }

    public File getPom(String baseDir, String fn) throws IOException {
        File projectCopy = this.resources.getBasedir(baseDir);
        File pom = new File(projectCopy, fn);

        Assert.assertNotNull(pom);
        Assert.assertTrue(pom.exists());

        return pom;
    }

}
