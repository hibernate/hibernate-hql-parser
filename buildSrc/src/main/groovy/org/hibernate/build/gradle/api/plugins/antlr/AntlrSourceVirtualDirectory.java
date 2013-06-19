/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibernate.build.gradle.api.plugins.antlr;

import groovy.lang.Closure;
import org.gradle.api.file.SourceDirectorySet;

/**
 * Contract for a Gradle "convention object" that acts as a handler for what I call a virtual directory mapping,
 * injecting a virtual directory named 'antlr' into the project's various {@link org.gradle.api.tasks.SourceSet source
 * sets}.  Its implementation gets pushed onto the {@link org.gradle.api.internal.DynamicObjectAware} portion of the
 * source set under the name 'antlr'.
 *
 * @author Steve Ebersole
 */
public interface AntlrSourceVirtualDirectory {
	public static final String NAME = "antlr";

	/**
	 * All Antlr source for this source set.
	 *
	 * @return The Antlr source.  Never returns null.
	 */
	public SourceDirectorySet getAntlr();

	/**
	 * Configures the Antlr source for this set. The given closure is used to configure the {@code SourceDirectorySet} (see
	 * {@link #getAntlr}) which contains the Antlr source.
	 *
	 * @param configureClosure The closure to use to configure the Antlr source.
	 * @return this
	 */
	public AntlrSourceVirtualDirectory antlr(Closure configureClosure);

}
