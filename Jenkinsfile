#!/usr/bin/env groovy

if (env.BRANCH_NAME == "master") {
  properties([
    buildDiscarder(
      logRotator(
        daysToKeepStr: '90'
      )
    )
  ])
} else {
  properties([
    buildDiscarder(
      logRotator(
        numToKeepStr: '10'
      )
    )
  ])
}

stage('parallel tests') {
  parallel javaBuildTests: {
    stage('unit tests') {
      node {
        checkout scm
        sh 'git clean -fdxq -e .gradle/home -e **/build/'
        docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
          docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
            try {
              stage('Build') {
                sh 'GRADLE_USER_HOME=`pwd`/.gradle/home ./gradlew --no-daemon --parallel build'
              }
            } finally {
              archiveArtifacts(artifacts: '**/build/reports/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: '**/build/test-results/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: 'build/reports/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: 'build/distributions/**', allowEmptyArchive: true)

              junit(testResults: '**/build/test-results/**/*.xml', allowEmptyResults: true)
            }
          }
        }
      }
    }
  }, otherTests: {
    stage('unit tests') {
      node {
        checkout scm
        sh 'git clean -fdxq -e .gradle/home -e **/build/'
        docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
          docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
            try {
              stage('Integration Tests') {
                sh 'GRADLE_USER_HOME=`pwd`/.gradle/home ./gradlew --no-daemon --parallel integrationTest'
              }
              stage('Acceptance Tests') {
                sh 'GRADLE_USER_HOME=`pwd`/.gradle/home ./gradlew --no-daemon --parallel acceptanceTest --tests Web3Sha3AcceptanceTest --tests PantheonClusterAcceptanceTest --tests MiningAcceptanceTest'
              }
              stage('Check Licenses') {
                sh 'GRADLE_USER_HOME=`pwd`/.gradle/home ./gradlew --no-daemon --parallel checkLicenses'
              }
              stage('Check javadoc') {
                sh 'GRADLE_USER_HOME=`pwd`/.gradle/home ./gradlew --no-daemon --parallel javadoc'
              }
              stage('Jacoco root report') {
                sh 'GRADLE_USER_HOME=`pwd`/.gradle/home ./gradlew --no-daemon jacocoRootReport'
              }
            } finally {
              archiveArtifacts(artifacts: '**/build/reports/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: '**/build/test-results/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: 'build/reports/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: 'build/distributions/**', allowEmptyArchive: true)

              junit(testResults: '**/build/test-results/**/*.xml', allowEmptyResults: true)
            }
          }
        }
      }
    }
  }, javaReferenceTests: {
    stage('reference tests') {
      node {
        checkout scm
        sh 'git clean -fdxq -e .gradle/home -e **/build/'
        docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
          docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
            try {
              stage('Reference tests') {
                sh 'GRADLE_USER_HOME=`pwd`/.gradle/home ./gradlew --no-daemon --parallel referenceTest'
              }
            } finally {
              archiveArtifacts(artifacts: '**/build/reports/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: '**/build/test-results/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: 'build/reports/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: 'build/distributions/**', allowEmptyArchive: true)

              junit(testResults: '**/build/test-results/**/*.xml', allowEmptyResults: true)
            }
          }
        }
      }
    }
  }, quickstartTests: {
    stage('quickstart tests') {
      node {
        checkout scm
        sh 'git clean -fdxq -e .gradle/home -e **/build/'
        docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
          docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
            try {
              stage('Docker quickstart Tests') {
                sh 'DOCKER_HOST=tcp://docker:2375 GRADLE_USER_HOME=`pwd`/.gradle/home ./gradlew --no-daemon --parallel dockerQuickstartTest'
              }
            } finally {
              archiveArtifacts(artifacts: '**/build/test-results/**', allowEmptyArchive: true)
              archiveArtifacts(artifacts: '**/build/reports/**', allowEmptyArchive: true)

              junit(testResults: '**/build/test-results/**/*.xml', allowEmptyResults: true)
            }
          }
        }
      }
    }
  }
}
