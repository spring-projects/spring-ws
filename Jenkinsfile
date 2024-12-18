def p = [:]
node {
	checkout scm
	p = readProperties interpolate: true, file: 'ci/pipeline.properties'
}

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
		stage('Publish Eclipse Temurin (main) + gpg docker image') {
			when {
				changeset "ci/Dockerfile"
			}
			agent any

			steps {
				script {
				    docker.withRegistry('', "${p['dockerhub.credentials']}") {
					    def image = docker.build("${p['docker.java.build.image']}", "ci/")
                        image.push()
				    }
                }
			}
		}
		stage("Test: baseline (main)") {
			agent any
			options { timeout(time: 30, unit: 'MINUTES')}
			environment {
				ARTIFACTORY = credentials("${p['artifactory.credentials']}")
				DEVELOCITY_ACCESS_KEY = credentials("${p['develocity.access-key']}")
			}
			steps {
				script {
				    docker.withRegistry('', "${p['dockerhub.credentials']}") {
					    docker.image(p['docker.java.main.image']).inside(p['docker.java.inside.basic']) {
						    sh "PROFILE=jakarta-ee-10,distribute,convergence ci/test.sh"
					    }
                    }
				}
			}
		}

		stage("Test other configurations") {
			when {
				beforeAgent(true)
				branch(pattern: "main|(\\d\\.\\d\\.x)|release(-.+)?|issue/.+", comparator: "REGEXP")
			}

			parallel {
				stage("Test: spring-buildsnapshot (main)") {
					agent any
					options { timeout(time: 30, unit: 'MINUTES')}
					environment {
						ARTIFACTORY = credentials("${p['artifactory.credentials']}")
						DEVELOCITY_ACCESS_KEY = credentials("${p['develocity.access-key']}")
					}
					steps {
						script {
						    docker.withRegistry('', "${p['dockerhub.credentials']}") {
							    docker.image(p['docker.java.main.image']).inside(p['docker.java.inside.basic']) {
								    sh "PROFILE=jakarta-ee-10,spring-buildsnapshot,convergence ci/test.sh"
							    }
                            }
						}
					}
				}

				stage("Test: wss4j-next (main)") {
					agent any
					options { timeout(time: 30, unit: 'MINUTES')}
					environment {
						ARTIFACTORY = credentials("${p['artifactory.credentials']}")
						DEVELOCITY_ACCESS_KEY = credentials("${p['develocity.access-key']}")
					}
					steps {
						script {
						    docker.withRegistry('', "${p['dockerhub.credentials']}") {
							    docker.image(p['docker.java.main.image']).inside(p['docker.java.inside.basic']) {
								    sh "PROFILE=jakarta-ee-10,wss4j-next,convergence ci/test.sh"
							    }
                            }
						}
					}
				}

				stage("Test: spring-next-gen (main)") {
					agent any
					options { timeout(time: 30, unit: 'MINUTES')}
					environment {
						ARTIFACTORY = credentials("${p['artifactory.credentials']}")
						DEVELOCITY_ACCESS_KEY = credentials("${p['develocity.access-key']}")
					}
					steps {
						script {
						    docker.withRegistry('', "${p['dockerhub.credentials']}") {
							    docker.image(p['docker.java.main.image']).inside(p['docker.java.inside.basic']) {
								    sh "PROFILE=jakarta-ee-10,spring-next-gen,convergence ci/test.sh"
							    }
                            }
						}
					}
				}

				stage("Test: spring-next-gen-snapshot (main)") {
					agent any
					options { timeout(time: 30, unit: 'MINUTES')}
					environment {
						ARTIFACTORY = credentials("${p['artifactory.credentials']}")
						DEVELOCITY_ACCESS_KEY = credentials("${p['develocity.access-key']}")
					}
					steps {
						script {
							docker.image(p['docker.java.main.image']).inside(p['docker.java.inside.basic']) {
								sh "PROFILE=jakarta-ee-10,spring-next-gen-snapshot,convergence ci/test.sh"
							}
						}
					}
				}

				stage("Test: jakarta-ee-9 (main)") {
					agent any
					options { timeout(time: 30, unit: 'MINUTES')}
					environment {
						ARTIFACTORY = credentials("${p['artifactory.credentials']}")
						DEVELOCITY_ACCESS_KEY = credentials("${p['develocity.access-key']}")
					}
					steps {
						script {
							docker.image(p['docker.java.main.image']).inside(p['docker.java.inside.basic']) {
								// Active Jakarta EE 9 profile and disable Jakarta EE 10 (default) profile
								sh "PROFILE=jakarta-ee-9,-jakarta-ee-10,convergence ci/test.sh"
							}
						}
					}
				}
			}
		}

		stage('Deploy') {
			agent any
			options { timeout(time: 20, unit: 'MINUTES') }
			environment {
				ARTIFACTORY = credentials("${p['artifactory.credentials']}")
				SONATYPE = credentials('oss-login')
				KEYRING = credentials('spring-signing-secring.gpg')
				PASSPHRASE = credentials('spring-gpg-passphrase')
				STAGING_PROFILE_ID = credentials('spring-data-release-deployment-maven-central-staging-profile-id')
				DEVELOCITY_ACCESS_KEY = credentials("${p['develocity.access-key']}")
			}

			steps {
				script {
				    docker.withRegistry('', "${p['dockerhub.credentials']}") {
					    docker.image("${p['docker.java.build.image-proxy']}").inside(p['docker.java.inside.basic']) {
                            PROJECT_VERSION = sh(
                                    script: "ci/version.sh",
                                    returnStdout: true
                            ).trim()

                            echo "Releasing Spring WS ${PROJECT_VERSION}..."

                            if (PROJECT_VERSION.matches(/.*-RC[0-9]+$/) || PROJECT_VERSION.matches(/.*-M[0-9]+$/)) {
                                RELEASE_TYPE = "milestone"
                            } else if (PROJECT_VERSION.endsWith('SNAPSHOT')) {
                                RELEASE_TYPE = 'snapshot'
                            } else if (PROJECT_VERSION.matches(/.*\.[0-9]+$/)) {
                                RELEASE_TYPE = 'release'
                            } else {
                                RELEASE_TYPE = 'snapshot'
                            }

                            echo "Release type: ${RELEASE_TYPE}"

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
    // 							sh "ci/smoke-test-against-maven-central.sh ${PROJECT_VERSION} ${STAGING_REPOSITORY_ID}"

                                writeFile(file: 'staging_repository_id.txt', text: "${STAGING_REPOSITORY_ID}")
                                stash 'staging_repository_id.txt'

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
			}
		}

		stage('Deploy (part 2)') {
			agent any
			options { timeout(time: 20, unit: 'MINUTES') }
			environment {
				ARTIFACTORY = credentials("${p['artifactory.credentials']}")
				SONATYPE = credentials('oss-login')
				KEYRING = credentials('spring-signing-secring.gpg')
				PASSPHRASE = credentials('spring-gpg-passphrase')
				STAGING_PROFILE_ID = credentials('spring-data-release-deployment-maven-central-staging-profile-id')
				DEVELOCITY_ACCESS_KEY = credentials("${p['develocity.access-key']}")
			}

			steps {
				script {
                    docker.withRegistry('', "${p['dockerhub.credentials']}") {
                        docker.image("${p['docker.java.legacy.image']}").inside(p['docker.java.inside.basic']) {
                            PROJECT_VERSION = sh(
                                    script: "ci/version.sh",
                                    returnStdout: true
                            ).trim()

                            echo "Releasing Spring WS ${PROJECT_VERSION}..."

                            if (PROJECT_VERSION.matches(/.*-RC[0-9]+$/) || PROJECT_VERSION.matches(/.*-M[0-9]+$/)) {
                                RELEASE_TYPE = "milestone"
                            } else if (PROJECT_VERSION.endsWith('SNAPSHOT')) {
                                RELEASE_TYPE = 'snapshot'
                            } else if (PROJECT_VERSION.matches(/.*\.[0-9]+$/)) {
                                RELEASE_TYPE = 'release'
                            } else {
                                RELEASE_TYPE = 'snapshot'
                            }

                            echo "Release type: ${RELEASE_TYPE}"

                            if (RELEASE_TYPE == 'release') {

                                unstash 'staging_repository_id.txt'

                                def STAGING_REPOSITORY_ID = readFile(file: 'staging_repository_id.txt')

                                sh "ci/rc-release.sh ${STAGING_REPOSITORY_ID}"

                                slackSend(
                                        color: (currentBuild.currentResult == 'SUCCESS') ? 'good' : 'danger',
                                        channel: '#spring-ws',
                                        message: "Spring WS ${PROJECT_VERSION} is released to Maven Central!")
                            } else {

                                echo "Since this is an Artifactory release, there is no 'part 2'."

                            }
                        }
                    }
				}
			}
		}

		stage('Release documentation') {
			when {
				beforeAgent(true)
				branch(pattern: "main|release", comparator: "REGEXP")
			}
			agent any
			options { timeout(time: 20, unit: 'MINUTES') }

			environment {
				ARTIFACTORY = credentials("${p['artifactory.credentials']}")
				DEVELOCITY_ACCESS_KEY = credentials("${p['develocity.access-key']}")
			}

			steps {
				script {
				    docker.withRegistry('', "${p['dockerhub.credentials']}") {
					    docker.image(p['docker.java.main.image']).inside(p['docker.java.inside.basic']) {
                            sh 'MAVEN_OPTS="-Duser.name=jenkins -Duser.home=/tmp/jenkins-home" ' +
                                    './mvnw -s settings.xml -Pjakarta-ee-10,distribute,docs,default ' +
                                    '-Dartifactory.server=https://repo.spring.io ' +
                                    "-Dartifactory.username=${ARTIFACTORY_USR} " +
                                    "-Dartifactory.password=${ARTIFACTORY_PSW} " +
                                    "-Dartifactory.distribution-repository=temp-private-local " +
                                    '-Duser.name=spring-builds+jenkins -Dmaven.test.skip=true -Dmaven.deploy.skip=true deploy -B'
                        }
                    }
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
