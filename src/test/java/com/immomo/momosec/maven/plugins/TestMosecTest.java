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

import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.resources.TestResources;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

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
        Assert.assertNotNull(pom);
        Assert.assertFalse(pom.exists());

        exceptionRule.expect(java.io.FileNotFoundException.class);
        exceptionRule.expectMessage("(No such file or directory)");

        this.rule.lookupMojo("test", pom.getCanonicalPath());
    }

    @Test
    public void validProjectTest() throws Exception {
        File projectCopy = this.resources.getBasedir("valid-project");
        File pom = new File(projectCopy, "pom.xml");

        Assert.assertNotNull(pom);
        Assert.assertTrue(pom.exists());

        MosecTest mosecTest = (MosecTest)this.rule.lookupMojo("test", pom);
        Assert.assertNotNull(mosecTest);
    }

}
