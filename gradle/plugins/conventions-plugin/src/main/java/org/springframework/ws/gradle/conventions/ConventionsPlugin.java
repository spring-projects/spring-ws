/*
 * Copyright 2005-present the original author or authors.
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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * {@link Plugin} to apply conventions to a {@link Project} by reacting to its plugins and
 * configuring them.
 *
 * @author Andy Wilkinson
 */
public class ConventionsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.setGroup("org.springframework.ws");
		project.getPlugins().withType(JavaBasePlugin.class).all((plugin) -> {
			new JavaBasePluginConventions().apply(project);
			new CheckstyleConventions().apply(project);
		});
		project.getPlugins().withType(JavaPlugin.class).all((plugin) -> new JavaPluginConventions().apply(project));
		project.getPlugins()
			.withType(MavenPublishPlugin.class)
			.all((plugin) -> new MavenPublishPluginConventions().apply(project));
	}

}
