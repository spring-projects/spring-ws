/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.gradle.optional;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;

/**
 * Plugin that adds support for "optional" dependencies. An optional dependency is
 * available for compilation and at runtime when building and testing the project but is
 * not exposed to consumers.
 *
 * @author Andy Wilkinson
 */
public class OptionalDependenciesPlugin implements Plugin<Project> {

	@Override
	public void apply(Project target) {
		target.getPlugins().withType(JavaPlugin.class, (plugin) -> {
			ConfigurationContainer configurations = target.getConfigurations();
			configurations.create("optional", (optional) -> {
				optional.setCanBeConsumed(false);
				optional.setCanBeResolved(false);
				target.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().all((sourceSet) -> {
					configurations.getByName(sourceSet.getCompileClasspathConfigurationName()).extendsFrom(optional);
					configurations.getByName(sourceSet.getRuntimeClasspathConfigurationName()).extendsFrom(optional);
				});
			});
		});
	}

}
