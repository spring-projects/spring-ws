/*
 * Copyright 2005-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ws.gradle.conventions;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.CoreJavadocOptions;
import org.gradle.external.javadoc.MinimalJavadocOptions;

/**
 * Conventions for the {@link JavaBasePlugin}.
 *
 * @author Andy Wilkinson
 */
class JavaBasePluginConventions {

	void apply(Project project) {
		configureRepositories(project);
		project.getTasks().withType(Javadoc.class).configureEach((javadoc) -> {
			MinimalJavadocOptions options = javadoc.getOptions();
			options.quiet();
			options.source(JavaPluginConventions.JAVA_BASELINE.getMajorVersion());
			if (options instanceof CoreJavadocOptions coreOptions) {
				coreOptions.addBooleanOption("Xdoclint:-missing", true);
			}
		});
	}

	private void configureRepositories(Project project) {
		project.getRepositories().mavenCentral();
		String version = project.getVersion().toString();
		if (version.contains("-")) {
			project.getRepositories().maven((repository) -> {
				repository.setName("Spring Milestones");
				repository.setUrl("https://repo.spring.io/milestone");
			});
		}
		if (version.endsWith("-SNAPSHOT")) {
			project.getRepositories().maven((repository) -> {
				repository.setName("Spring Snapshots");
				repository.setUrl("https://repo.spring.io/snapshot");
			});
		}
	}

}
