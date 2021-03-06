/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webcohesion.enunciate.artifacts;

import com.webcohesion.enunciate.Enunciate;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * An artifact that can be exported by Enunciate.
 *
 * @author Ryan Heaton
 */
public interface Artifact extends Comparable<Artifact> {

  /**
   * The id of the artifact.
   *
   * @return The id of the artifact.
   */
  String getId();

  /**
   * The name of the artifact.
   *
   * @return The name of the artifact.
   */
  String getName();

  /**
   * The description of the artifact.
   *
   * @return The description of the artifact.
   */
  String getDescription();

  /**
   * When this artifact was created.
   *
   * @return When this artifact was created.
   */
  Date getCreated();

  /**
   * The name of the module that published this artifact.
   *
   * @return The name of the module that published this artifact.
   */
  String getModule();

  /**
   * Exports this artifact to the specified file or directory.
   *
   * @param fileOrDirectory The file or directory to export to.
   * @param enunciate The enunciate mechanism to use for utilities and properties as necessary.
   * @throws java.io.IOException If an error occurs exporting it.
   */
  void exportTo(File fileOrDirectory, Enunciate enunciate) throws IOException;

  /**
   * The size, in bytes, of this artifact.
   *
   * @return The size, in bytes, of this artifact.
   */
  long getSize();

  /**
   * Whether this artifact is bundled with others in an {@link ArtifactBundle}.
   *
   * @return Whether this artifact is bundled elsewhere.
   */
  boolean isPublic();

  /**
   * Whether the artifact belongs on the server-side classpath
   *
   * @return Whether the artifact belongs on the server-side classpath
   */
  boolean isBelongsOnServerSideClasspath();

  /**
   * The list of dependencies for this artifact.
   *
   * @return The list of dependencies for this artifact.
   */
  List<ArtifactDependency> getDependencies();

  /**
   * A list of aliases for this artifact.
   *
   * @return The set of aliases for this artifact.
   */
  Set<String> getAliases();

}
