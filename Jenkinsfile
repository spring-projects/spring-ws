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
		stage("Test: baseline (jdk8)") {
			agent {
				docker {
					image 'adoptopenjdk/openjdk8:latest'
					args '-v $HOME/.m2:/root/.m2'
				}
			}
			steps {
				sh "PROFILE=distribute,convergence ci/test.sh"
			}
		}

		stage("Test other configurations") {
			parallel {
				stage("Test: springnext (jdk8)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk8:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=springnext,convergence ci/test.sh"
					}
				}
				stage("Test: springnext-buildsnapshot (jdk8)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk8:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=springnext-buildsnapshot,convergence ci/test.sh"
					}
				}
				stage("Test: spring-buildsnapshot (jdk8)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk8:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=spring-buildsnapshot,convergence ci/test.sh"
					}
				}
				stage("Test: baseline (jdk11)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk11:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=distribute,java11,convergence ci/test.sh"
					}
				}
				stage("Test: springnext (jdk11)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk11:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=springnext,java11,convergence ci/test.sh"
					}
				}
				stage("Test: springnext-buildsnapshot (jdk11)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk11:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=springnext-buildsnapshot,java11,convergence ci/test.sh"
					}
				}
				stage("Test: spring-buildsnapshot (jdk11)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk11:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=spring-buildsnapshot,java11,convergence ci/test.sh"
					}
				}
				stage("Test: baseline (jdk15)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk15:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=distribute,java11,convergence ci/test.sh"
					}
				}
				stage("Test: springnext (jdk15)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk15:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=springnext,java11,convergence ci/test.sh"
					}
				}
				stage("Test: springnext-buildsnapshot (jdk15)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk15:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=springnext-buildsnapshot,java11,convergence ci/test.sh"
					}
				}
				stage("Test: spring-buildsnapshot (jdk15)") {
					agent {
						docker {
							image 'adoptopenjdk/openjdk15:latest'
							args '-v $HOME/.m2:/root/.m2'
						}
					}
					steps {
						sh "PROFILE=spring-buildsnapshot,java11,convergence ci/test.sh"
					}
				}
			}
		}

		stage('Deploy') {
			agent {
				docker {
					image 'springci/spring-ws-openjdk8-with-jq:latest'
					args '-v $HOME/.m2:/tmp/jenkins-home/.m2'
				}
			}
			options { timeout(time: 20, unit: 'MINUTES') }

			environment {
				ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
				SONATYPE = credentials('oss-login')
				KEYRING = credentials('spring-signing-secring.gpg')
				PASSPHRASE = credentials('spring-gpg-passphrase')
			}

			steps {
				script {
					PROJECT_VERSION = sh(
							script: "ci/version.sh",
							returnStdout: true
					).trim()

					RELEASE_TYPE = 'milestone' // .RC? or .M?

					if (PROJECT_VERSION.endsWith('BUILD-SNAPSHOT')) {
						RELEASE_TYPE = 'snapshot'
					} else if (PROJECT_VERSION.endsWith('RELEASE')) {
						RELEASE_TYPE = 'release'
					} else {
						RELEASE_TYPE = 'snapshot'
					}

					if (RELEASE_TYPE == 'release') {
						sh "PROFILE=distribute,central ci/build-and-deploy-to-maven-central.sh ${PROJECT_VERSION}"

						slackSend(
							color: (currentBuild.currentResult == 'SUCCESS') ? 'good' : 'danger',
							channel: '#spring-ws',
							message: "Spring WS ${PROJECT_VERSION} is staged on Sonatype awaiting closure and release.")
					} else {
						sh "PROFILE=distribute,${RELEASE_TYPE} ci/build-and-deploy-to-artifactory.sh"
					}
				}
			}
		}

		stage('Release documentation') {
			when {
				branch 'release-3.x'
			}
			agent {
				docker {
					image 'adoptopenjdk/openjdk8:latest'
					args '-v $HOME/.m2:/tmp/jenkins-home/.m2'
				}
			}
			options { timeout(time: 20, unit: 'MINUTES') }

			environment {
				ARTIFACTORY = credentials('02bd1690-b54f-4c9f-819d-a77cb7a9822c')
			}

			steps {
				script {
					sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ./mvnw -Pdistribute,docs ' +
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
