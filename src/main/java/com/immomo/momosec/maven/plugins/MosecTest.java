/*
 * Copyright 2017 Snyk Ltd.
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
import com.immomo.momosec.maven.plugins.exceptions.NetworkErrorException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.repository.RemoteRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.immomo.momosec.maven.plugins.Renderer.writeToFile;

@Mojo(name = "test")
public class MosecTest extends AbstractMojo {

    @Component
    private RepositorySystem repositorySystem;

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    private RepositorySystemSession repositorySystemSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    private List<RemoteRepository> remoteProjectRepositories;

    @Parameter(defaultValue = "${project.remotePluginRepositories}", readonly = true)
    private List<RemoteRepository> remotePluginRepositories;

    @Parameter(defaultValue = "${settings}", readonly = true, required = true )
    private Settings settings;

    /**
     * 威胁等级 [High|Medium|Low]
     */
    @Parameter(property = "severity", defaultValue = "High")
    private String severityLevel;

    /**
     * 仅检查直接依赖
     */
    @Parameter(property = "onlyProvenance", defaultValue = "false")
    private Boolean onlyProvenance;

    /**
     * 发现漏洞即编译失败
     */
    @Parameter(property = "failOnVuln", defaultValue = "true")
    private Boolean failOnVuln;

    /**
     * 上报API
     */
    @Parameter(property = "endpoint")
    private String endpoint;

    /**
     * 是否包含Provided Scope依赖
     */
    @Parameter(property = "includeProvidedDependency", defaultValue = "false")
    private Boolean includeProvidedDependency;

    /**
     * 输出依赖树到文件
     */
    @Parameter(property = "outputDepToFile", defaultValue = "")
    private String outputDepToFile;

    /**
     * 仅分析依赖，不进行漏洞检查
     */
    @Parameter(property = "onlyAnalyze", defaultValue = "false")
    private Boolean onlyAnalyze;

    private static List<JsonObject> collectTree = new ArrayList<>();
    private static List<String> totalProjectsByGAV = null;

    public void execute() throws MojoExecutionException, MojoFailureException {
        String env_endpoint = System.getenv(Constants.MOSEC_ENDPOINT_ENV);
        if (env_endpoint != null) {
            endpoint = env_endpoint;
        }

        if (Boolean.FALSE.equals(onlyAnalyze) && endpoint == null) {
            throw new MojoFailureException(Constants.ERROR_ON_NULL_ENDPOINT);
        }

        if (remoteProjectRepositories == null) {
            remoteProjectRepositories = new ArrayList<>();
        }

        if (remotePluginRepositories == null) {
            remotePluginRepositories = new ArrayList<>();
        }

        try {
            for (RemoteRepository remoteProjectRepository : remoteProjectRepositories) {
                getLog().debug("Remote project repository: " + remoteProjectRepository);
            }
            for (RemoteRepository remotePluginRepository : remotePluginRepositories) {
                getLog().debug("Remote plugin repository: " + remotePluginRepository);
            }
            List<RemoteRepository> remoteRepositories = new ArrayList<>(remoteProjectRepositories);
            remoteRepositories.addAll(remotePluginRepositories);

            ProjectDependencyCollector collector = new ProjectDependencyCollector(
                    project,
                    repositorySystem,
                    repositorySystemSession,
                    remoteRepositories,
                    includeProvidedDependency,
                    onlyProvenance
            );
            collector.collectDependencies();
            JsonObject projectTree = collector.getTree();
            String jsonDepTree = new GsonBuilder().setPrettyPrinting().create().toJson(projectTree);
            getLog().debug(jsonDepTree);

            collectTree.add(projectTree.deepCopy());
            if (Boolean.TRUE.equals(onlyAnalyze)) {
                if (this.isAnalyzeTotalFinished()
                        && outputDepToFile != null
                        && !"".equals(outputDepToFile)
                ) {
                    writeToFile(outputDepToFile, new GsonBuilder().setPrettyPrinting().create().toJson(collectTree));
                }

                getLog().info("onlyAnalyze mode, Done.");
                return;
            }

            projectTree.addProperty("type", Constants.BUILD_TOOL_TYPE);
            projectTree.addProperty("language", Constants.PROJECT_LANGUAGE);
            projectTree.addProperty("severityLevel", severityLevel);

            HttpPost request = new HttpPost(endpoint);
            request.addHeader("content-type", Constants.CONTENT_TYPE_JSON);
            HttpEntity entity = new StringEntity(projectTree.toString());
            request.setEntity(entity);

            HttpClientHelper httpClientHelper = new HttpClientHelper(getLog(), settings);
            HttpClient client = httpClientHelper.buildHttpClient();
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() >= 400) {
                throw new NetworkErrorException(response.getStatusLine().getReasonPhrase());
            }

            JsonParser parser = new JsonParser();
            JsonObject responseJson;
            try {
                responseJson = parser.parse(new BufferedReader(new InputStreamReader(response.getEntity().getContent()))).getAsJsonObject();
                JsonObject lastTree = collectTree.get(collectTree.size() - 1);
                lastTree.add("result", responseJson);
            } catch (JsonParseException | IllegalStateException e) {
                throw new NetworkErrorException(Constants.ERROR_ON_API);
            }

            if (outputDepToFile != null && !"".equals(outputDepToFile)) {
                writeToFile(outputDepToFile, new GsonBuilder().setPrettyPrinting().create().toJson(collectTree));
            }

            Renderer renderer = new Renderer(getLog(), failOnVuln);
            renderer.renderResponse(responseJson);

        } catch (DependencyCollectionException e) {
            throw new MojoFailureException(e.getMessage(), e.fillInStackTrace());
        } catch(MojoFailureException e) {
            throw e;
        } catch(Exception e) {
            if (getLog().isDebugEnabled()) {
                getLog().error(Constants.ERROR_GENERAL, e);
            } else {
                getLog().error(Constants.ERROR_GENERAL);
                getLog().error(Constants.ERROR_RERUN_WITH_DEBUG);
            }
            throw new MojoFailureException(e.getMessage(), e.fillInStackTrace());
        }
    }

    @SuppressWarnings("unchecked")
    private boolean isAnalyzeTotalFinished() {
        if (totalProjectsByGAV == null) {
            Object key = repositorySystemSession.getWorkspaceReader().getRepository().getKey();
            if (key instanceof HashSet) {
                HashSet<String> gavs = (HashSet<String>) key;
                totalProjectsByGAV = (List<String>) gavs.stream().collect(Collectors.toList());
            } else {
                return false;
            }
        }
        List<String> analyzedProjectsByGAV = collectTree.stream()
                .map(o -> String.format("%s:%s", o.get("name").getAsString(), o.get("version").getAsString()))
                .collect(Collectors.toList());

        if (totalProjectsByGAV == null
                || analyzedProjectsByGAV == null
                || totalProjectsByGAV.size() != analyzedProjectsByGAV.size()
        ) {
            return false;
        }
        return new TreeSet<String>(totalProjectsByGAV).equals(new TreeSet<String>(analyzedProjectsByGAV));
    }
}
