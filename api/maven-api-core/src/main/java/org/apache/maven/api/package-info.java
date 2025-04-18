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

/**
 * <h2>Maven Core API</h2>
 *
 * <h3>Session</h3>
 *
 * <p>The {@link org.apache.maven.api.Session} interface is the main entry point for Maven operations.
 * It maintains the state of a Maven execution and provides access to all core services and components.
 * Sessions are thread-safe and can be obtained in session-scoped components using the
 * {@link org.apache.maven.api.di.SessionScoped} annotation.</p>
 *
 * <p>Key capabilities provided through the Session include:</p>
 * <ul>
 *   <li>Access to the current {@link org.apache.maven.api.Project}</li>
 *   <li>Access to the {@link org.apache.maven.api.LocalRepository} and {@link org.apache.maven.api.RemoteRepository} configurations</li>
 *   <li>Access to Maven services through {@link org.apache.maven.api.Session#getService(Class)}</li>
 *   <li>Build configuration and settings</li>
 * </ul>
 *
 * <h3>Dependency management</h3>
 *
 * <p>{@link org.apache.maven.api.ArtifactCoordinates} instances are used to locate artifacts in a repository.
 * Each instance identifies an artifact or version range of artifacts in the Maven repository system.</p>
 *
 * <p>{@link org.apache.maven.api.Artifact} instances represent artifacts in the repository.
 * They are created when <dfn>resolving</dfn> an {@code ArtifactCoordinates} object. Resolving is the process
 * that selects a particular version and downloads the artifact into the local repository.
 * The {@link org.apache.maven.api.DownloadedArtifact} sub-interface is used when
 * an artifact has been resolved and the {@link org.apache.maven.api.ProducedArtifact} sub-interface when
 * an artifact is being produced by a project.</p>
 *
 * <p>{@link org.apache.maven.api.DependencyCoordinates} instances represent a dependency element in a POM.
 * A {@code DependencyCoordinates} extends {@code ArtifactCoordinates} with additional information about how
 * the artifact will be used: type, scope and obligation (whether the dependency is optional or mandatory).
 * The version and the obligation may not be defined precisely.</p>
 *
 * <p>{@link org.apache.maven.api.Dependency} instances represent artifacts in the repository
 * that are dependencies of the project.
 * They are created when <dfn>resolving</dfn> a {@code DependencyCoordinates}.
 * Resolving is the process that clarifies the obligation (optional or mandatory),
 * selects a particular version, and downloads the artifact into the local repository.</p>
 *
 * <p>{@link org.apache.maven.api.Node} is the main output of the <dfn>dependency collection</dfn> process.
 * It's a node in the dependency graph and contains a {@code Dependency} instance. The {@code Dependency} instances are the outputs of the
 * collection process, part of the graph computed from one or more {@code DependencyCoordinates}.</p>
 *
 * <p>{@link org.apache.maven.api.DependencyScope} defines when/how a given dependency will be used by the
 * project. This includes compile-time only, runtime, test time, and various other combinations.</p>
 *
 * <h3>Resolution</h3>
 *
 * <p><dfn>Version resolution</dfn> is the process of finding, for a given artifact, a list of
 * versions that match the input {@linkplain org.apache.maven.api.VersionConstraint version constraint}
 * in the list of remote repositories. This is done either explicitly using the
 * {@link org.apache.maven.api.services.VersionResolver VersionResolver} service, or implicitly when resolving
 * an artifact.</p>
 *
 * <p><dfn>Artifact resolution</dfn> is the process of {@linkplain org.apache.maven.api.services.VersionResolver
 * resolving the version} and then downloading the file.</p>
 *
 * <p><dfn>Dependency collection</dfn> builds a graph of {@link org.apache.maven.api.Node} objects containing
 * all the dependencies.</p>
 *
 * <p>The <dfn>Dependency graph flattening</dfn> process in Maven reduces a complex,
 * multi-level dependency graph to a map of ordered lists that can be turned into classpaths.
 * During this process only the most relevant version of each artifact
 * (based on group ID and artifact ID) is retained, resolving conflicts and eliminating duplicates to ensure
 * that each dependency is included only once in the final build.</p>
 *
 * <p><dfn>Dependency resolution</dfn> is the process of collecting dependencies, flattening the result graph,
 * and then resolving the artifacts.</p>
 *
 * <h3>Repositories</h3>
 *
 * <p>In Maven, <dfn>{@linkplain org.apache.maven.api.Repository repositories}</dfn> are locations where project artifacts (such as JAR files, POM files, and other
 * resources) are stored and retrieved. There are two primary types of repositories:<ul>
 *     <li><dfn>{@linkplain org.apache.maven.api.LocalRepository local repository}</dfn>: A directory on the developer's machine where Maven caches
 *     downloaded artifacts.</li>
 *     <li><dfn>{@linkplain org.apache.maven.api.RemoteRepository remote repository}</dfn>: A central or distributed location from which Maven can download artifacts
 *     when they are not available locally.</li>
 * </ul>
 *
 * <p>When resolving artifacts, Maven follows this order:</p><ol>
 * <li>Check Local Repository: Maven first checks if the artifact is available in the local repository.</li>
 * <li>Check Remote Repositories: If the artifact is not found locally, Maven queries the configured remote repositories in the order they are listed.</li>
 * <li>Download and Cache: If Maven finds the artifact in a remote repository, it downloads it and stores it in the local repository for future use.</li>
 * </ol>
 * <p>By caching artifacts in the local repository, Maven minimizes the need to repeatedly download the same artifacts, thus optimizing the build process.</p>
 *
 * <h3>Projects</h3>
 *
 * <p>{@link org.apache.maven.api.Project} instances are loaded by Maven from the local
 * file system (those projects are usually about to be built) or from the local repository
 * (they are usually downloaded during dependency collection). Those projects are loaded
 * from a Project Object Model (POM).</p>
 *
 * <p><dfn>Project Object Model</dfn> or <dfn>POM</dfn> refers to the information describing
 * all the information needed to build or consume a project. Those are usually loaded from
 * a file named {@code pom.xml} and loaded into a {@link org.apache.maven.api.model.Model Model}
 * instances.</p>
 *
 * <p><dfn>Project aggregation</dfn> allows building several projects together. This is only
 * for projects that are built, hence available on the file system. One project,
 * called the <dfn>aggregator project</dfn> lists one or more <dfn>modules</dfn>
 * which are relative pointers on the file system to other projects. This is done using
 * the {@code /project/modules/module} elements of the POM in the aggregator project.
 * Note that the aggregator project is required to have a {@code pom} packaging.</p>
 *
 * <p><dfn>Project inheritance</dfn> defines a parent-child relationship between projects.
 * The <dfn>child project</dfn> inherits all the information from the <dfn>parent project</dfn>
 * POM.</p>
 *
 */
package org.apache.maven.api;
