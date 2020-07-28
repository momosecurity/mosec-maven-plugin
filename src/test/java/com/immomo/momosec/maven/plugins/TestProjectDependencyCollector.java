package com.immomo.momosec.maven.plugins;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;

public class TestProjectDependencyCollector {

    private final MavenProject project = mock(MavenProject.class);
    private final RepositorySystem repoSystem = mock(RepositorySystem.class);
    private final DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
    private final List<RemoteRepository> remoteRepositories = singletonList(
            (new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/")).build());

    private final Artifact parent = new DefaultArtifact("com.study.parent:parent:1.0.0");
    private final Artifact child = new DefaultArtifact("com.study.child:child:1.0.0");
    private final Artifact child_child = new DefaultArtifact("com.study.child_child:child_child:1.0.0");

    @Test
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public void createJsonTreeTest() throws Exception {
        Class collectorClass = ProjectDependencyCollector.class;
        Method method = collectorClass.getDeclaredMethod("createJsonTree", DependencyNode.class, JsonArray.class);
        method.setAccessible(true);

        DependencyNode parent_node = new DefaultDependencyNode(parent);
        DependencyNode child_node = new DefaultDependencyNode(child);
        DependencyNode child_child_node = new DefaultDependencyNode(child_child);
        child_node.setChildren(singletonList(child_child_node));
        parent_node.setChildren(singletonList(child_node));

        JsonObject parentJson = getJsonObject(parent);
        JsonObject childJson = getJsonObject(child);
        JsonObject child_childJson = getJsonObject(child_child);

        JsonArray parentFrom = new JsonArray();
        parentFrom.add(String.format("%s:%s@%s", parent.getGroupId(), parent.getArtifactId(), parent.getVersion()));
        parentJson.add("from", parentFrom);

        JsonArray childFrom =  new JsonArray();
        childFrom.addAll(parentFrom);
        childFrom.add(String.format("%s:%s@%s", child.getGroupId(), child.getArtifactId(), child.getVersion()));
        childJson.add("from", childFrom);

        JsonArray child_childFrom =  new JsonArray();
        child_childFrom.addAll(childFrom);
        child_childFrom.add(String.format("%s:%s@%s", child_child.getGroupId(), child_child.getArtifactId(), child_child.getVersion()));
        child_childJson.add("from", child_childFrom);

        JsonObject parentDependencies = new JsonObject();
        parentDependencies.add(String.format("%s:%s", child.getGroupId(), child.getArtifactId()), childJson);
        parentJson.add("dependencies", parentDependencies);

        JsonObject childDependencies = new JsonObject();
        childDependencies.add(String.format("%s:%s", child_child.getGroupId(), child_child.getArtifactId()), child_childJson);
        childJson.add("dependencies",  childDependencies);

        child_childJson.add("dependencies", new JsonObject());

        JsonObject actualJson;

        ProjectDependencyCollector collector_WithOnlyProvenance = new ProjectDependencyCollector(
                project, repoSystem, session, remoteRepositories, false, true
        );
        actualJson = (JsonObject)method.invoke(collector_WithOnlyProvenance, parent_node, null);
        Assert.assertNull(actualJson.getAsJsonObject("dependencies").getAsJsonObject("dependencies"));

        ProjectDependencyCollector collector_WithoutOnlyProvenance = new ProjectDependencyCollector(
                project, repoSystem, session, remoteRepositories, false, false
        );
        actualJson = (JsonObject)method.invoke(collector_WithoutOnlyProvenance, parent_node, null);
        Assert.assertEquals(parentJson, actualJson);
    }

    @Test
    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public void createTreeNodeTest() throws Exception {
        ProjectDependencyCollector collector = new ProjectDependencyCollector(
                project, repoSystem, session, remoteRepositories, false, true
        );
        Class collectorClass = ProjectDependencyCollector.class;
        Method method = collectorClass.getDeclaredMethod("createTreeNode", Artifact.class, JsonArray.class);
        method.setAccessible(true);


        JsonArray from = new JsonArray();
        from.add(String.format("%s:%s@%s", parent.getGroupId(), parent.getArtifactId(), parent.getVersion()));

        JsonObject expectJson = getJsonObject(child);

        JsonArray expectFrom = new JsonArray();
        expectFrom.addAll(from);
        expectFrom.add(String.format("%s:%s@%s", child.getGroupId(), child.getArtifactId(), child.getVersion()));
        expectJson.add("from", expectFrom);

        JsonObject json = (JsonObject)method.invoke(collector, child, from);
        Assert.assertEquals(expectJson, json);
    }

    private JsonObject getJsonObject(Artifact artifact) {
        JsonObject obj = new JsonObject();
        obj.addProperty("version", artifact.getVersion());
        obj.addProperty("name", String.format("%s:%s", artifact.getGroupId(), artifact.getArtifactId()));

        return obj;
    }
}
