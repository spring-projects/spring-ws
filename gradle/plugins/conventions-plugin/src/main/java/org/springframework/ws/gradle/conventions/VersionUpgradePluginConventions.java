/*
 * Copyright 2005-present the original author or authors.
 *
 * This product includes software developed by the Spring project
 * (https://spring.io/).
 */

package org.springframework.ws.gradle.conventions;

import java.util.Locale;

import com.github.benmanes.gradle.versions.VersionsPlugin;
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask;
import org.gradle.api.Project;

import org.springframework.ws.gradle.conventions.support.Version;
import org.springframework.ws.gradle.conventions.support.VersionUpgradePolicy;

/**
 * Conventions for {@code gradle-versions-plugin}.
 *
 * @author Stephane Nicoll
 */
class VersionUpgradePluginConventions {

	void apply(Project project) {
		project.getPlugins().apply(VersionsPlugin.class);
		project.getTasks().withType(DependencyUpdatesTask.class, (updateTask) -> {
			updateTask.doFirst((task) -> project.getRepositories()
				.removeIf((candidate) -> candidate.getName()
					.equals(JavaBasePluginConventions.SPRING_SNAPSHOT_REPOSITORY_NAME)));
			updateTask.setFilterConfigurations((configuration) -> !(configuration.getName().contains("_")
					&& configuration.getName().endsWith("+")));
			VersionUpgradePolicy upgradePolicy = getUpgradePolicy(project);
			updateTask.rejectVersionIf((candidate) -> {
				Version currentVersion = Version.from(candidate.getCurrentVersion());
				Version candidateVersion = Version.from(candidate.getCandidate().getVersion());
				return !upgradePolicy.isCandidate(currentVersion, candidateVersion);
			});
		});
	}

	private VersionUpgradePolicy getUpgradePolicy(Project project) {
		Object versionUpgradePolicy = project.findProperty("versionUpgradePolicy");
		if (versionUpgradePolicy == null) {
			return VersionUpgradePolicy.SAME_MINOR_VERSION;
		}
		else if (versionUpgradePolicy instanceof String value) {
			return VersionUpgradePolicy.valueOf(value.toUpperCase(Locale.ROOT));
		}
		throw new IllegalArgumentException("Unsupported version upgrade policy: " + versionUpgradePolicy);
	}

}
