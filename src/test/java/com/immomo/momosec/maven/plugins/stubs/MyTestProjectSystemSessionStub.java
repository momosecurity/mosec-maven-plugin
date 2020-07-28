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
package com.immomo.momosec.maven.plugins.stubs;

import org.codehaus.plexus.PlexusTestCase;
import org.eclipse.aether.*;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.collection.*;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.ArtifactDescriptorPolicy;
import org.eclipse.aether.resolution.ResolutionErrorPolicy;
import org.eclipse.aether.transfer.TransferListener;

import java.util.HashMap;
import java.util.Map;

public class MyTestProjectSystemSessionStub implements RepositorySystemSession {
    private final Map<String, String> systemProperties;
    private final Map<String, String> userProperties;
    private final Map<String, Object> configProperties;
    private final MirrorSelector mirrorSelector;
    private final ProxySelector proxySelector;
    private final AuthenticationSelector authenticationSelector;
    private LocalRepositoryManager localRepositoryManager;

    @SuppressWarnings(value = {"unchecked", "rawtypes"})
    public MyTestProjectSystemSessionStub() {
        LocalRepository repository = new LocalRepository(PlexusTestCase.getBasedir());
        try {
            LocalRepositoryManager localRepositoryManager = new SimpleLocalRepositoryManagerFactory().newInstance(this, repository);
            this.setLocalRepositoryManager(localRepositoryManager);
        } catch (NoLocalRepositoryManagerException e) {
            e.printStackTrace();
        }

        this.systemProperties = new HashMap();
        this.userProperties = new HashMap();
        this.configProperties = new HashMap();
        this.mirrorSelector = MyTestProjectSystemSessionStub.NullMirrorSelector.INSTANCE;
        this.proxySelector = MyTestProjectSystemSessionStub.NullProxySelector.INSTANCE;
        this.authenticationSelector = MyTestProjectSystemSessionStub.NullAuthenticationSelector.INSTANCE;
    }

    @Override
    public boolean isOffline() {
        return false;
    }

    @Override
    public boolean isIgnoreArtifactDescriptorRepositories() {
        return false;
    }

    @Override
    public ResolutionErrorPolicy getResolutionErrorPolicy() {
        return null;
    }

    @Override
    public ArtifactDescriptorPolicy getArtifactDescriptorPolicy() {
        return null;
    }

    @Override
    public String getChecksumPolicy() {
        return null;
    }

    @Override
    public String getUpdatePolicy() {
        return null;
    }

    @Override
    public LocalRepository getLocalRepository() {
        return null;
    }

    @Override
    public LocalRepositoryManager getLocalRepositoryManager() {
        return this.localRepositoryManager;
    }

    public MyTestProjectSystemSessionStub setLocalRepositoryManager(LocalRepositoryManager localRepositoryManager) {
        this.localRepositoryManager = localRepositoryManager;
        return this;
    }

    @Override
    public WorkspaceReader getWorkspaceReader() {
        return null;
    }

    @Override
    public RepositoryListener getRepositoryListener() {
        return null;
    }

    @Override
    public TransferListener getTransferListener() {
        return null;
    }

    @Override
    public Map<String, String> getSystemProperties() {
        return this.systemProperties;
    }

    @Override
    public Map<String, String> getUserProperties() {
        return this.userProperties;
    }

    @Override
    public Map<String, Object> getConfigProperties() {
        return this.configProperties;
    }

    @Override
    public MirrorSelector getMirrorSelector() {
        return this.mirrorSelector;
    }

    @Override
    public ProxySelector getProxySelector() {
        return this.proxySelector;
    }

    @Override
    public AuthenticationSelector getAuthenticationSelector() {
        return this.authenticationSelector;
    }

    @Override
    public ArtifactTypeRegistry getArtifactTypeRegistry() {
        return null;
    }

    @Override
    public DependencyTraverser getDependencyTraverser() {
        return null;
    }

    @Override
    public DependencyManager getDependencyManager() {
        return null;
    }

    @Override
    public DependencySelector getDependencySelector() {
        return null;
    }

    @Override
    public VersionFilter getVersionFilter() {
        return null;
    }

    @Override
    public DependencyGraphTransformer getDependencyGraphTransformer() {
        return null;
    }

    @Override
    public SessionData getData() {
        return null;
    }

    @Override
    public RepositoryCache getCache() {
        return null;
    }

    static final class NullArtifactTypeRegistry implements ArtifactTypeRegistry {
        public static final ArtifactTypeRegistry INSTANCE = new MyTestProjectSystemSessionStub.NullArtifactTypeRegistry();

        NullArtifactTypeRegistry() {
        }

        public ArtifactType get(String typeId) {
            return null;
        }
    }

    static class NullAuthenticationSelector implements AuthenticationSelector {
        public static final AuthenticationSelector INSTANCE = new MyTestProjectSystemSessionStub.NullAuthenticationSelector();

        NullAuthenticationSelector() {
        }

        public Authentication getAuthentication(RemoteRepository repository) {
            return repository.getAuthentication();
        }
    }

    static class NullMirrorSelector implements MirrorSelector {
        public static final MirrorSelector INSTANCE = new MyTestProjectSystemSessionStub.NullMirrorSelector();

        NullMirrorSelector() {
        }

        public RemoteRepository getMirror(RemoteRepository repository) {
            return null;
        }
    }

    static class NullProxySelector implements ProxySelector {
        public static final ProxySelector INSTANCE = new MyTestProjectSystemSessionStub.NullProxySelector();

        NullProxySelector() {
        }

        public Proxy getProxy(RemoteRepository repository) {
            return repository.getProxy();
        }
    }
}
