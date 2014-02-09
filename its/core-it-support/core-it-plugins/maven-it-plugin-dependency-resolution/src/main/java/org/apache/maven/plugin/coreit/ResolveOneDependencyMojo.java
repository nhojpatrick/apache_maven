package org.apache.maven.plugin.coreit;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Attempts to resolve a single artifact from dependencies, and logs the results for the Verifier to look at.
 * 
 * @goal resolve-one-dependency
 * @requiresDependencyResolution runtime
 * @author bimargulies
 * @version $Id$
 */
public class ResolveOneDependencyMojo
    extends AbstractDependencyMojo
{

    /**
     * Group ID of the artifact to resolve.
     * 
     * @parameter
     * @required
     */
    private String groupId;
    /**
     * Artifact ID of the artifact to resolve.
     * 
     * @parameter
     * @required
     */
    private String artifactId;
    /**
     * Version  of the artifact to resolve.
     * 
     * @parameter
     * @required
     */
    private String version;
    /**
     * Type of the artifact to resolve.
     * 
     * @parameter
     * @required
     */
    private String type;
    /**
     * Classifier of the artifact to resolve.
     * 
     * @parameter
     */
    private String classifier;

    /**
     * The scope to resolve for.
     * 
     * @parameter
     * @required
     */
    private String scope;

    /**
     * @parameter default-value="${project}"
     * @required
     */
    MavenProject project;

    /**
     * @component
     */
    private ArtifactResolver resolver;

    /**
     * @component
     * @readonly
     * @required
     */
    private ArtifactFactory artifactFactory;

    /**
     * The Maven session.
     * 
     * @parameter default-value="${session}"
     * @readonly
     * @required
     */
    private MavenSession session;

    /**
     * Metadata source object.
     * 
     * @component
     */
    private ArtifactMetadataSource metadataSource;

    /**
     * Runs this mojo.
     * 
     * @throws MojoExecutionException If the output file could not be created or any dependency could not be resolved.
     */
    public void execute()
        throws MojoExecutionException
    {

        Artifact projectArtifact = project.getArtifact();
        if ( projectArtifact == null )
        {
            projectArtifact =
                artifactFactory.createProjectArtifact( project.getGroupId(), project.getArtifactId(),
                                                       project.getVersion() );
        }

        Set depArtifacts = new HashSet();
        Artifact artifact = artifactFactory.createArtifactWithClassifier( groupId, artifactId, version, type, classifier );
        depArtifacts.add( artifact );

        ScopeArtifactFilter scopeFilter = new ScopeArtifactFilter( scope );

        ArtifactResolutionResult result;
        try
        {
            result =
                resolver.resolveTransitively( depArtifacts, projectArtifact, project.getManagedVersionMap(),
                                              session.getLocalRepository(), project.getRemoteArtifactRepositories(),
                                              metadataSource, scopeFilter );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MojoExecutionException( "RESOLVE-ONE-DEPENDENCY ArtifactResolutionException exception ", e );
        }
        catch ( ArtifactNotFoundException e )
        {
            throw new MojoExecutionException( "RESOLVE-ONE-DEPENDENCY ArtifactNotFoundException exception ", e );
        }

        if ( result == null )
        {
            getLog().info( "RESOLVE-ONE-DEPENDENCY null result" );
        }
        else
        {
            Set resolvedArtifacts = result.getArtifacts();
            Iterator it = resolvedArtifacts.iterator();
            /*
             * Assume that the user of this is not interested in transitive deps and such, just report the one.
             */
            while ( it.hasNext() )
            {
                Artifact a = (Artifact) it.next();
                if ( a.equals( artifact ) )
                {
                    File file = a.getFile();
                    if ( file == null )
                    {
                        getLog().info( " RESOLVE-ONE-DEPENDENCY " + a.toString() + " $ NO-FILE" );
                    }
                    else
                    {
                        getLog().info( " RESOLVE-ONE-DEPENDENCY " + a.toString() + " $ " + file.getAbsolutePath() );
                    }
                    return;
                }
            }
            getLog().info(" RESOLVE-ONE-DEPENDENCY " + artifact.toString() + " $ NOT-RESOLVED" );
        }
    }
}
