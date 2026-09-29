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

import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.ConfigurationVariantDetails;
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaTestFixturesPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPomDeveloperSpec;
import org.gradle.api.publish.maven.MavenPomIssueManagement;
import org.gradle.api.publish.maven.MavenPomLicenseSpec;
import org.gradle.api.publish.maven.MavenPomOrganization;
import org.gradle.api.publish.maven.MavenPomScm;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ws.gradle.conventions.build.BuildSettings;
import org.springframework.ws.gradle.conventions.build.BuildType;

/**
 * Conventions for the {@link MavenPublishPlugin}.
 *
 * @author Andy Wilkinson
 */
class MavenPublishPluginConventions {

	private static final Logger logger = LoggerFactory.getLogger(MavenPublishPluginConventions.class);

	void apply(Project project) {
		PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
		configureRepositories(project, publishing);
		project.getPlugins().withType(JavaPlugin.class).all((javaPlugin) -> {
			publishing.getPublications().create("maven", MavenPublication.class, (publication) -> {
				publication.from(project.getComponents().getByName("java"));
				publication.versionMapping((strategy) -> {
					strategy.usage("java-api",
							(variantStrategy) -> variantStrategy.fromResolutionOf("runtimeClasspath"));
					strategy.usage("java-runtime", (variantStrategy) -> variantStrategy.fromResolutionResult());
				});
				configure(project, publication);
			});
		});
		project.getPlugins().withType(JavaPlatformPlugin.class).all((javaPlatformPlugin) -> {
			publishing.getPublications().create("maven", MavenPublication.class, (publication) -> {
				publication.from(project.getComponents().getByName("javaPlatform"));
				configure(project, publication);
			});
		});
		project.getPlugins()
			.withType(JavaTestFixturesPlugin.class, (testFixtures) -> disableTextFixturesPublishing(project));
	}

	private void disableTextFixturesPublishing(Project project) {
		ConfigurationContainer configurations = project.getConfigurations();
		AdhocComponentWithVariants javaComponent = (AdhocComponentWithVariants) project.getComponents()
			.getByName("java");
		javaComponent.withVariantsFromConfiguration(configurations.getByName("testFixturesApiElements"),
				ConfigurationVariantDetails::skip);
		javaComponent.withVariantsFromConfiguration(configurations.getByName("testFixturesRuntimeElements"),
				ConfigurationVariantDetails::skip);
	}

	void configureRepositories(Project project, PublishingExtension publishing) {
		Object deploymentRepository = project.findProperty("deploymentRepository");
		if (deploymentRepository != null) {
			publishing.getRepositories().maven((maven) -> {
				maven.setName("deployment");
				maven.setUrl(deploymentRepository);
			});
		}
	}

	void configure(Project project, MavenPublication mavenPublication) {
		configurePom(project, mavenPublication);
		project.getTasks().withType(GenerateModuleMetadata.class, (generate) -> generate.setEnabled(false));
	}

	void configurePom(Project project, MavenPublication mavenPublication) {
		BuildType buildType = BuildSettings.get(project).buildType();
		mavenPublication.pom((pom) -> {
			pom.getUrl().set("https://spring.io/projects/spring-ws");
			pom.getName().set(project.provider(project::getName));
			pom.getDescription().set(project.provider(project::getDescription));
			pom.licenses((licenses) -> customizeLicences(licenses, buildType));
			pom.organization(this::customizeOrganization);
			pom.developers(this::customizeDevelopers);
			pom.issueManagement((issueManagement) -> customizeIssueManagement(issueManagement, buildType));
			pom.scm((scm) -> customizeScm(scm, buildType));
		});
	}

	private void customizeLicences(MavenPomLicenseSpec licences, BuildType buildType) {
		licences.license((licence) -> {
			if (buildType == BuildType.OPEN_SOURCE) {
				licence.getName().set("Apache License, Version 2.0");
				licence.getUrl().set("https://www.apache.org/licenses/LICENSE-2.0");
			}
			else {
				licence.getName().set("Broadcom Foundation License");
			}
		});
	}

	private void customizeOrganization(MavenPomOrganization organization) {
		organization.getName().set("Broadcom Inc.");
		organization.getUrl().set("https://www.spring.io");
	}

	private void customizeDevelopers(MavenPomDeveloperSpec developers) {
		developers.developer((developer) -> {
			developer.getName().set("Spring");
			developer.getEmail().set("ask@spring.io");
			developer.getOrganization().set("Broadcom Inc.");
			developer.getOrganizationUrl().set("https://www.spring.io");
		});
	}

	private void customizeIssueManagement(MavenPomIssueManagement issueManagement, BuildType buildType) {
		if (buildType != BuildType.OPEN_SOURCE) {
			logger.debug("Skipping Maven POM SCM for non open source build type");
			return;
		}
		issueManagement.getSystem().set("GitHub");
		issueManagement.getUrl().set("https://github.com/spring-projects/spring-ws/issues");
	}

	private void customizeScm(MavenPomScm scm, BuildType buildType) {
		if (buildType != BuildType.OPEN_SOURCE) {
			logger.debug("Skipping Maven POM SCM for non open source build type");
			return;
		}
		scm.getConnection().set("scm:git:git://github.com/spring-projects/spring-ws.git");
		scm.getDeveloperConnection().set("scm:git:ssh://git@github.com:spring-projects/spring-ws.git");
		scm.getUrl().set("https://github.com/spring-projects/spring-ws");
	}

}
