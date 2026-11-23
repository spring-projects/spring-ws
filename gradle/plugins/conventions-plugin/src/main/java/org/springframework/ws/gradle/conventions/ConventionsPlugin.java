/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.gradle.conventions;

import io.spring.gradle.nullability.NullabilityPlugin;
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
		project.getPlugins().withType(JavaPlugin.class).all((plugin) -> {
			project.getPlugins().apply(NullabilityPlugin.class);
			new JavaPluginConventions().apply(project);
		});
		project.getPlugins()
			.withType(MavenPublishPlugin.class)
			.all((plugin) -> new MavenPublishPluginConventions().apply(project));
	}

}
