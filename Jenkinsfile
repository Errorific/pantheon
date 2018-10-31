#!/usr/bin/env groovy

buildFolders = [
  'buildSrc/build',
  'acceptance-tests/build',
  'consensus/clique/build',
  'consensus/common/build',
  'consensus/ibft/build',
  'consensus/ibftlegacy/build',
  'crypto/build',
  'errorprone-checks/build',
  'ethereum/blockcreation/build',
  'ethereum/core/build',
  'ethereum/eth/build',
  'ethereum/jsonrpc/build',
  'ethereum/mock-p2p/build',
  'ethereum/p2p/build',
  'ethereum/referencetests/build',
  'ethereum/rlp/build',
  'ethereum/trie/build',
  'pantheon/build',
  'quickstart/build',
  'testutil/build',
  'util/build'
]

void stashBuildFolders() {
  stash name: "gradleInProj", allowEmpty: true, includes: ".gradle/**"
  stash name: "gradleHome", allowEmpty: true, includes: "gradlehome/**"
  stash name: "builtstuff", allowEmpty: true, includes: "**/build/**"
//  buildFolders.each {location ->
//    stash(
//      name: location.replace('/', '_'),
//      allowEmpty: true,
//      includes: "${location}/"
//    )
//  }
}

void unstashBuildFolders() {
  //buildFolders.each {location ->
  //  unstash(location.replace('/', '_'))
  //  sh "ls ${location}"
  //}
  unstash "gradleInProj"
  unstash "gradleHome"
  unstash "builtstuff"
}

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

stage('build') {
  node {
    checkout scm
    docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
      docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
        try {
          stage('build/Compile') {
            sh 'GRADLE_USER_HOME=`pwd`/gradlehome ./gradlew --no-daemon --parallel clean compileJava compileTestJava compileIntegrationTestJava compileJmhJava compileTestSupportJava assemble'
          }
          stage('build/stash build') {
            stashBuildFolders()
          }
        } finally {
          archiveArtifacts(artifacts: '**/build/reports/**', allowEmptyArchive: true)
          archiveArtifacts(artifacts: 'build/reports/**', allowEmptyArchive: true)
          archiveArtifacts(artifacts: 'build/distributions/**', allowEmptyArchive: true)
        }
      }
    }
  }
}
stage('parallel tests') {
  parallel javaBuildTests: {
    stage('unit tests') {
      node {
        checkout scm
        docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
          docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
            try {
              stage('unstash build') {
                unstashBuildFolders()
              }
              stage('Build') {
                sh 'GRADLE_USER_HOME=`pwd`/gradlehome ./gradlew --no-daemon --parallel build'
              }
              stage('Integration Tests') {
                sh 'GRADLE_USER_HOME=`pwd`/gradlehome ./gradlew --no-daemon --parallel integrationTest'
              }
              stage('Acceptance Tests') {
                sh 'GRADLE_USER_HOME=`pwd`/gradlehome ./gradlew --no-daemon --parallel acceptanceTest --tests Web3Sha3AcceptanceTest --tests PantheonClusterAcceptanceTest --tests MiningAcceptanceTest'
              }
              stage('Check Licenses') {
                sh 'GRADLE_USER_HOME=`pwd`/gradlehome ./gradlew --no-daemon --parallel checkLicenses'
              }
              stage('Check javadoc') {
                sh 'GRADLE_USER_HOME=`pwd`/gradlehome ./gradlew --no-daemon --parallel javadoc'
              }
              stage('Jacoco root report') {
                sh 'GRADLE_USER_HOME=`pwd`/gradlehome ./gradlew --no-daemon jacocoRootReport'
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
        docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
          docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
            try {
              stage('unstash build') {
                unstashBuildFolders()
              }
              stage('Reference tests') {
                sh 'GRADLE_USER_HOME=`pwd`/gradlehome ./gradlew --no-daemon --parallel referenceTest'
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
        docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
          docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
            try {
              stage('unstash build') {
                unstashBuildFolders()
              }
              stage('Docker quickstart Tests') {
                sh 'DOCKER_HOST=tcp://docker:2375 GRADLE_USER_HOME=`pwd`/gradlehome ./gradlew --no-daemon --parallel dockerQuickstartTest'
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
