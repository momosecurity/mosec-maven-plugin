package com.immomo.momosec.maven.plugins.stubs;

import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyTestProjectSettingsStub extends Settings {
    public List<Proxy> getProxies()
    {
        return new ArrayList<>();
    }
}
