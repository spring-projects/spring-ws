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

import java.util.Map;
import java.util.TreeMap;

import io.spring.javaformat.gradle.SpringJavaFormatPlugin;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/**
 * Conventions for the {@link JavaPlugin}.
 *
 * @author Andy Wilkinson
 */
class JavaPluginConventions {

	static final JavaVersion JAVA_BASELINE = JavaVersion.VERSION_17;

	void apply(Project project) {
		project.getPlugins().apply(SpringJavaFormatPlugin.class);
		JavaPluginExtension java = project.getExtensions().getByType(JavaPluginExtension.class);
		enableSourceAndJavadocJars(java);
		configureSourceAndTargetCompatibility(java);
		configureDependencyManagement(project);
		configureJarManifest(project);
		configureToolchain(project, java);
		configureJavadocClasspath(project, java);
		configureJUnitPlatform(project);
	}



	private void enableSourceAndJavadocJars(JavaPluginExtension java) {
		java.withSourcesJar();
		java.withJavadocJar();
	}

	private void configureSourceAndTargetCompatibility(JavaPluginExtension java) {
		java.setSourceCompatibility(JAVA_BASELINE);
		java.setTargetCompatibility(JAVA_BASELINE);
	}

	private void configureDependencyManagement(Project project) {
		ConfigurationContainer configurations = project.getConfigurations();
		Configuration dependencyManagement = configurations.create("dependencyManagement", (configuration) -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeResolved(false);
			configuration.setVisible(false);
		});
		configurations.matching((candidate) -> candidate.getName().endsWith("Classpath"))
			.all((classpath) -> classpath.extendsFrom(dependencyManagement));
		DependencyHandler dependencies = project.getDependencies();
		dependencyManagement.getDependencies()
			.add(dependencies.enforcedPlatform(dependencies.project(Map.of("path", ":spring-ws-platform"))));
	}

	private void configureJarManifest(Project project) {
		project.getTasks().named("jar", Jar.class, (jar) -> jar.manifest((manifest) -> {
			Map<String, Object> attributes = new TreeMap<>();
			attributes.put("Automatic-Module-Name", project.getName().replace("-", "."));
			attributes.put("Build-Jdk-Spec", JAVA_BASELINE.getMajorVersion());
			attributes.put("Built-By", "Spring");
			attributes.put("Implementation-Title", project.getDescription());
			attributes.put("Implementation-Version", project.getVersion());
			manifest.attributes(attributes);
		}));
	}

	private void configureToolchain(Project project, JavaPluginExtension java) {
		Object toolchainVersion = project.findProperty("toolchainVersion");
		if (toolchainVersion != null) {
			java.getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(toolchainVersion.toString()));
		}
	}

	private void configureJavadocClasspath(Project project, JavaPluginExtension java) {
		project.getConfigurations().create("javadocClasspath", (javadocClasspath) -> {
			javadocClasspath.setCanBeConsumed(true);
			javadocClasspath.setCanBeResolved(false);
			javadocClasspath.extendsFrom(project.getConfigurations()
				.getByName(java.getSourceSets()
					.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
					.getCompileClasspathConfigurationName()));
		});
	}

	private void configureJUnitPlatform(Project project) {
		project.getTasks().withType(Test.class).configureEach((task) -> task.useJUnitPlatform());
		project.getDependencies().add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher");
	}

}
