/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.it;

import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * This is a test set for <a href="https://issues.apache.org/jira/browse/MNG-1349">MNG-1349</a>.
 *
 * @author Benjamin Bentmann
 *
 */
public class MavenITmng1349ChecksumFormatsTest extends AbstractMavenIntegrationTestCase {

    public MavenITmng1349ChecksumFormatsTest() {
        super("(2.0.10,2.1.0-M1),(2.1.0-M1,)");
    }

    /**
     * Tests that different checksum formats are supported.
     *
     * @throws Exception in case of failure
     */
    @Test
    public void testitMNG1349() throws Exception {
        File testDir = extractResources("/mng-1349");

        Verifier verifier = newVerifier(testDir.getAbsolutePath());
        verifier.setAutoclean(false);
        verifier.deleteArtifacts("org.apache.maven.its.mng1349");
        verifier.filterFile("settings-template.xml", "settings.xml");
        verifier.addCliArgument("--settings");
        verifier.addCliArgument("settings.xml");
        verifier.addCliArgument("validate");
        verifier.execute();
        verifier.verifyErrorFreeLog();

        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "md5-a", "0.1", "jar");
        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "md5-a", "0.1", "pom");

        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "md5-b", "0.1", "jar");
        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "md5-b", "0.1", "pom");

        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "md5-c", "0.1", "jar");
        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "md5-c", "0.1", "pom");

        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "sha1-a", "0.1", "jar");
        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "sha1-a", "0.1", "pom");

        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "sha1-b", "0.1", "jar");
        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "sha1-b", "0.1", "pom");

        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "sha1-c", "0.1", "jar");
        verifier.verifyArtifactPresent("org.apache.maven.its.mng1349", "sha1-c", "0.1", "pom");
    }
}
