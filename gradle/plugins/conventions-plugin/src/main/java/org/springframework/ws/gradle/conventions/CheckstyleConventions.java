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

import io.spring.javaformat.gradle.SpringJavaFormatPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;

/**
 * {@link Plugin} that applies conventions for checkstyle.
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 */
public class CheckstyleConventions {

	/**
	 * Applies the Spring Java Format and Checkstyle plugins with the project conventions.
	 * @param project the current project
	 */
	public void apply(Project project) {
		project.getPlugins().withType(JavaPlugin.class, (java) -> {
			project.getPlugins().apply(CheckstylePlugin.class);
			project.getTasks()
				.withType(Checkstyle.class)
				.forEach((checkstyle) -> checkstyle.getMaxHeapSize().set("1g"));
			project.getTasks().named("checkstyleTest").configure((task) -> task.setEnabled(false));
			CheckstyleExtension checkstyle = project.getExtensions().getByType(CheckstyleExtension.class);
			checkstyle.setToolVersion("10.24.0");
			checkstyle.getConfigDirectory().set(project.getRootProject().file("src/checkstyle"));
			String version = SpringJavaFormatPlugin.class.getPackage().getImplementationVersion();
			DependencySet checkstyleDependencies = project.getConfigurations()
				.getByName("checkstyle")
				.getDependencies();
			checkstyleDependencies.add(
					project.getDependencies().create("com.puppycrawl.tools:checkstyle:" + checkstyle.getToolVersion()));
			checkstyleDependencies
				.add(project.getDependencies().create("io.spring.javaformat:spring-javaformat-checkstyle:" + version));
		});
	}

}
