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
package org.apache.maven.wagon.providers.coreit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.maven.wagon.AbstractWagon;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Shamelessly copied from ScpExternalWagon in this same project...
 */
@Component(role = org.apache.maven.wagon.Wagon.class, hint = "coreit", instantiationStrategy = "per-lookup")
public class CoreItWagon extends AbstractWagon {
    @Override
    public void get(String resourceName, File destination)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        InputData inputData = new InputData();

        Resource resource = new Resource(resourceName);

        fireGetInitiated(resource, destination);

        inputData.setResource(resource);

        fillInputData(inputData);

        InputStream is = inputData.getInputStream();

        if (is == null) {
            throw new TransferFailedException(
                    getRepository().getUrl() + " - Could not open input stream for resource: '" + resource + "'");
        }

        createParentDirectories(destination);

        getTransfer(inputData.getResource(), destination, is);
    }

    @Override
    public boolean getIfNewer(String resourceName, File destination, long timestamp)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        return false;
    }

    @Override
    public void put(File source, String resourceName)
            throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        OutputData outputData = new OutputData();

        Resource resource = new Resource(resourceName);

        firePutInitiated(resource, source);

        outputData.setResource(resource);

        fillOutputData(outputData);

        OutputStream os = outputData.getOutputStream();

        if (os == null) {
            throw new TransferFailedException(
                    getRepository().getUrl() + " - Could not open output stream for resource: '" + resource + "'");
        }

        putTransfer(outputData.getResource(), source, os, true);
    }

    @Override
    public void closeConnection() throws ConnectionException {
        File f = new File("target/wagon-data");
        try {
            f.getParentFile().mkdirs();
            f.createNewFile();
        } catch (IOException e) {
            throw new ConnectionException(e.getMessage(), e);
        }
    }

    public void fillInputData(InputData inputData) throws TransferFailedException, ResourceDoesNotExistException {
        try {
            String resName = inputData.getResource().getName();
            InputStream is = null;
            if (resName.endsWith(".sha1")) {
                is = new ByteArrayInputStream("c96e29be962f9d8123b584b8f51d66b347d268d4".getBytes("UTF-8"));
            } else if (resName.endsWith(".md5")) {
                is = new ByteArrayInputStream("d2b637ab8965308490bc6482c860dfc5".getBytes("UTF-8"));
            } else {
                is = new ByteArrayInputStream("<metadata />".getBytes("UTF-8"));
            }
            inputData.setInputStream(is);
        } catch (IOException e) {
            throw new TransferFailedException("Broken JVM", e);
        }
    }

    public void fillOutputData(OutputData outputData) throws TransferFailedException {
        Properties props = new Properties();

        if (getRepository().getPermissions() != null) {
            String dirPerms = getRepository().getPermissions().getDirectoryMode();
            put(props, "directory.mode", dirPerms);

            String filePerms = getRepository().getPermissions().getFileMode();
            put(props, "file.mode", filePerms);
        }

        AuthenticationInfo auth = getAuthenticationInfo();
        if (auth != null) {
            put(props, "username", auth.getUserName());
            put(props, "password", auth.getPassword());
            put(props, "privateKey", auth.getPrivateKey());
            put(props, "passphrase", auth.getPassphrase());
        }

        try {
            File file = new File(System.getProperty("user.dir"), "target/wagon.properties").getAbsoluteFile();
            file.getParentFile().mkdirs();

            try (OutputStream os = new FileOutputStream(file)) {
                props.store(os, "MAVEN-CORE-IT-WAGON");
            }
        } catch (IOException e) {
            throw new TransferFailedException(e.getMessage(), e);
        }

        outputData.setOutputStream(new ByteArrayOutputStream());
    }

    @Override
    public void openConnection() throws ConnectionException, AuthenticationException {
        // ignore
    }

    @Override
    protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
        // ignore
    }

    private void put(Properties props, String key, String value) {
        if (value != null) {
            props.setProperty(key, value);
        }
    }
}
