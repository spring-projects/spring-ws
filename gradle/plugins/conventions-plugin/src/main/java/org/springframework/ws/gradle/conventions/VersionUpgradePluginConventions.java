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
