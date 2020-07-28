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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;

import java.security.InvalidParameterException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class ProjectDependencyCollector {

    private final MavenProject project;
    private final RepositorySystem repoSystem;
    private final DefaultRepositorySystemSession session;
    private final List<RemoteRepository> remoteRepositories;
    private final boolean includeProvidedDependencies;
    private final boolean onlyProvenance;

    private JsonObject tree;

    public ProjectDependencyCollector(MavenProject project,
                                      RepositorySystem repoSystem,
                                      RepositorySystemSession repoSession,
                                      List<RemoteRepository> remoteRepositories,
                                      boolean includeProvidedDependencies,
                                      boolean onlyProvenance) {
        if(project == null || repoSystem == null || repoSession == null) {
            throw new InvalidParameterException();
        }

        this.project = project;
        this.repoSystem = repoSystem;
        this.session = new DefaultRepositorySystemSession(repoSession);
        this.remoteRepositories = remoteRepositories;
        this.includeProvidedDependencies = includeProvidedDependencies;
        this.onlyProvenance = onlyProvenance;
    }

    public void collectDependencies() throws DependencyCollectionException {
        Artifact artifact = new DefaultArtifact(
                String.format("%s:%s:%s", project.getGroupId(), project.getArtifactId(), project.getVersion()));

        if (includeProvidedDependencies) {
            session.setDependencySelector(
                new AndDependencySelector(
                    new ScopeDependencySelector(
                        asList(JavaScopes.COMPILE, JavaScopes.PROVIDED),
                        singletonList(JavaScopes.TEST)
                    ),
                    new OptionalDependencySelector()
                )
            );
        }

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(remoteRepositories);

        CollectResult collectResult = repoSystem.collectDependencies(session, collectRequest);
        DependencyNode node = collectResult.getRoot();

        this.tree = createJsonTree(node, null);
    }

    private JsonObject createJsonTree(DependencyNode depNode, JsonArray ancestors) {
        Artifact artifact = depNode.getArtifact();
        JsonObject treeNode = createTreeNode(artifact, ancestors);

        if (this.onlyProvenance && treeNode.get("from").getAsJsonArray().size() > 1) {
            if (Boolean.FALSE.equals(treeNode.has("dependencies"))) {
                treeNode.add("dependencies", new JsonObject());
            }
            return treeNode;
        }

        List<DependencyNode> children = depNode.getChildren();
        JsonObject dependencies = new JsonObject();
        for(DependencyNode childDep : children) {
            Artifact childArtifact = childDep.getArtifact();
            JsonObject childNode = createJsonTree(childDep, treeNode.get("from").getAsJsonArray());
            dependencies.add(String.format("%s:%s", childArtifact.getGroupId(), childArtifact.getArtifactId()), childNode);
        }
        treeNode.add("dependencies", dependencies);

        return treeNode;
    }

    private JsonObject createTreeNode(Artifact artifact, JsonArray ancestors) {
        JsonObject treeNode = new JsonObject();

        treeNode.addProperty("version", artifact.getVersion());
        treeNode.addProperty("name", String.format("%s:%s", artifact.getGroupId(), artifact.getArtifactId()));

        JsonArray from = new JsonArray();
        if(ancestors != null) {
            from.addAll(ancestors);
        }
        from.add(String.format("%s:%s@%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()));
        treeNode.add("from", from);

        return treeNode;
    }

    public JsonObject getTree() { return this.tree; }
}
