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
import org.gradle.api.plugins.JavaPlatformPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;

/**
 * Conventions for the {@link MavenPublishPlugin}.
 *
 * @author Andy Wilkinson
 */
class MavenPublishPluginConventions {

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
				configurePom(project, publication);
			});
		});
		project.getPlugins().withType(JavaPlatformPlugin.class).all((javaPlatformPlugin) -> {
			publishing.getPublications().create("maven", MavenPublication.class, (publication) -> {
				publication.from(project.getComponents().getByName("javaPlatform"));
				configurePom(project, publication);
			});
		});
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

	void configurePom(Project project, MavenPublication mavenPublication) {
		String organizationName = "Broadcom Inc.";
		String organizationUrl = "https://www.spring.io";
		mavenPublication.pom((pom) -> {
			pom.getUrl().set("https://spring.io/projects/spring-ws");
			pom.getName().set(project.provider(project::getName));
			pom.getDescription().set(project.provider(project::getDescription));
			pom.developers((developers) -> developers.developer((developer) -> {
				developer.getName().set("Spring");
				developer.getEmail().set("ask@spring.io");
				developer.getOrganization().set(organizationName);
				developer.getOrganizationUrl().set(organizationUrl);
			}));
			pom.licenses((licenses) -> licenses.license((license) -> {
				license.getName().set("The Apache License, Version 2.0");
				license.getUrl().set("http://www.apache.org/licenses/LICENSE-2.0.txt");
			}));
			pom.organization((organization) -> {
				organization.getName().set(organizationName);
				organization.getUrl().set(organizationUrl);
			});
			pom.scm((scm) -> {
				scm.getConnection().set("scm:git:git://github.com/spring-projects/spring-ws.git");
				scm.getDeveloperConnection().set("scm:git:ssh://git@github.com:spring-projects/spring-ws.git");
				scm.getUrl().set("https://github.com/spring-projects/spring-ws");
			});
		});
	}

}
