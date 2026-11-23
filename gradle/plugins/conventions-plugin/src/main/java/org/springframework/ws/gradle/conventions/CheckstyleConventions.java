/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
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
			project.getTasks()
				.named("checkstyleTest", Checkstyle.class)
				.configure((task) -> task
					.setConfigFile(project.getRootProject().file("src/checkstyle/checkstyle-test.xml")));
			CheckstyleExtension checkstyle = project.getExtensions().getByType(CheckstyleExtension.class);
			checkstyle.setToolVersion("12.3.1");
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
