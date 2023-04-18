pipeline {
	agent none

	triggers {
		pollSCM 'H/10 * * * *'
	}

	options {
		disableConcurrentBuilds()
		buildDiscarder(logRotator(numToKeepStr: '14'))
	}

	stages {
		stage('Publish OpenJDK 17 + jq docker image') {
			when {
				changeset "ci/Dockerfile"
			}
			agent any

			steps {
				script {
					def image = docker.build("springci/spring-ws-openjdk17-with-jq", "ci/")
					docker.withRegistry('', 'hub.docker.com-springbuildmaster') {
						image.push()
					}
				}
			}
		}

		stage("Test: baseline (jdk17)") {
			agent {
				docker {
					image 'openjdk:17-bullseye'
					args '-v $HOME/.m2:/root/.m2'
				}
			}
			environment {
				ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
			}
			steps {
				sh "PROFILE=jakarta-ee-10,distribute,convergence ci/test.sh"
			}
		}

		stage("Test other configurations") {
			parallel {
				stage("Test: spring-buildsnapshot (jdk17)") {
					agent {
						docker {
							image 'openjdk:17-bullseye'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					environment {
						ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
					}
					steps {
						sh "PROFILE=jakarta-ee-10,spring-buildsnapshot,convergence ci/test.sh"
					}
				}

				stage("Test: jakarta-ee-9 (jdk17)") {
					agent {
						docker {
							image 'openjdk:17-bullseye'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					environment {
						ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
					}
					steps {
						// Active Jakarta EE 9 profile and disable Jakarta EE 10 (default) profile
						sh "PROFILE=jakarta-ee-9,-jakarta-ee-10,convergence ci/test.sh"
					}
				}
			}
		}

		stage('Deploy') {
			agent {
				docker {
					image 'springci/spring-ws-openjdk17-with-jq:latest'
					args '-v $HOME/.m2:/tmp/jenkins-home/.m2'
				}
			}
			options { timeout(time: 20, unit: 'MINUTES') }

			environment {
				ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
				SONATYPE = credentials('oss-login')
				KEYRING = credentials('spring-signing-secring.gpg')
				PASSPHRASE = credentials('spring-gpg-passphrase')
				STAGING_PROFILE_ID = credentials('spring-data-release-deployment-maven-central-staging-profile-id')
			}

			steps {
				script {
					PROJECT_VERSION = sh(
							script: "ci/version.sh",
							returnStdout: true
					).trim()

					if (PROJECT_VERSION.matches(/.*-RC[0-9]+$/) || PROJECT_VERSION.matches(/.*-M[0-9]+$/)) {
						RELEASE_TYPE = "milestone"
					} else if (PROJECT_VERSION.endsWith('SNAPSHOT')) {
						RELEASE_TYPE = 'snapshot'
					} else if (PROJECT_VERSION.matches(/.*\.[0-9]+$/)) {
						RELEASE_TYPE = 'release'
					} else {
						RELEASE_TYPE = 'snapshot'
					}

					if (RELEASE_TYPE == 'release') {

						STAGING_REPOSITORY_ID = sh(
							script: "ci/rc-open.sh",
							returnStdout: true
						).readLines()
						  .findAll{ line -> line.contains("<repository>") && !line.contains("%s") }
						  .collect{ s -> s.substring(s.indexOf("<repository>") + "<repository>".length(), s.indexOf("</repository>")) }
						  .inject(0){ first, second -> second } // find the last entry, a.k.a. the most recent staging repository id

						sh "ci/build-and-deploy-to-maven-central.sh ${PROJECT_VERSION} ${STAGING_REPOSITORY_ID}"
						sh "ci/rc-close.sh ${STAGING_REPOSITORY_ID}"
						sh "ci/smoke-test-against-maven-central.sh ${PROJECT_VERSION} ${STAGING_REPOSITORY_ID}"
						sh "ci/rc-release.sh ${STAGING_REPOSITORY_ID}"

						slackSend(
								color: (currentBuild.currentResult == 'SUCCESS') ? 'good' : 'danger',
								channel: '#spring-ws',
								message: "Spring WS ${PROJECT_VERSION} is released to Maven Central!")
					} else {

						sh "ci/build-and-deploy-to-artifactory.sh ${RELEASE_TYPE}"

						// TODO: Resolve smoke testing against Artifactory
						// sh "ci/smoke-test-against-artifactory.sh ${PROJECT_VERSION}"
						
						if (RELEASE_TYPE == 'milestone') {
							slackSend(
									color: (currentBuild.currentResult == 'SUCCESS') ? 'good' : 'danger',
									channel: '#spring-ws',
									message: "Spring WS ${PROJECT_VERSION} is released to Artifactory!")
						}
					}
				}
			}
		}

		stage('Release documentation') {
			when {
				anyOf {
					branch 'main'
					branch 'release'
				}
			}
			agent {
				docker {
					image 'openjdk:17-bullseye'
					args '-v $HOME/.m2:/tmp/jenkins-home/.m2'
				}
			}
			options { timeout(time: 20, unit: 'MINUTES') }

			environment {
				ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
			}

			steps {
				script {
					sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -s settings.xml -Pjakarta-ee-10,distribute,docs ' +
							'-Dartifactory.server=https://repo.spring.io ' +
							"-Dartifactory.username=${ARTIFACTORY_USR} " +
							"-Dartifactory.password=${ARTIFACTORY_PSW} " +
							"-Dartifactory.distribution-repository=temp-private-local " +
							'-Dmaven.test.skip=true -Dmaven.deploy.skip=true deploy -B'
				}
			}
		}
	}

	post {
		changed {
			script {
				slackSend(
						color: (currentBuild.currentResult == 'SUCCESS') ? 'good' : 'danger',
						channel: '#spring-ws',
						message: "${currentBuild.fullDisplayName} - `${currentBuild.currentResult}`\n${env.BUILD_URL}")
				emailext(
						subject: "[${currentBuild.fullDisplayName}] ${currentBuild.currentResult}",
						mimeType: 'text/html',
						recipientProviders: [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']],
						body: "<a href=\"${env.BUILD_URL}\">${currentBuild.fullDisplayName} is reported as ${currentBuild.currentResult}</a>")
			}
		}
	}
}
