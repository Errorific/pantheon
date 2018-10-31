#!/usr/bin/env groovy

def buildFolders = [
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
];

void stashBuildFolders() {
  buildFolders.each {
    stash(
      name: ${it},
      allowEmpty: true,
      includes: "${it}/**"
    )
  }
}

void unstashBuildFolders() {
  buildFolders.each {
    unstash(
      name: ${it}
    )
  }
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

stage('shared build') {
  node {
    checkout scm
    docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
      docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
        try {
          stage('Compile') {
            sh './gradlew --no-daemon --parallel clean compileJava'
          }
          stage('compile tests') {
            sh './gradlew --no-daemon --parallel compileTestJava'
          }
          stage('assemble') {
            sh './gradlew --no-daemon --parallel assemble'
          }
          stage('stash build') {
            stashBuildFolders()
          }
        } finally {
          archiveArtifacts '**/build/reports/**'
          archiveArtifacts '**/build/test-results/**'
          archiveArtifacts 'build/reports/**'
          archiveArtifacts 'build/distributions/**'

          junit '**/build/test-results/**/*.xml'
        }
      }
    }
  }
}
stage('parallel tests') {
  parallel javaBuildTests: {
    stage('build tests') {
      node {
        checkout scm
        docker.image('docker:18.06.0-ce-dind').withRun('--privileged') { d ->
          docker.image('pegasyseng/pantheon-build:0.0.1').inside("--link ${d.id}:docker") {
            try {
              stage('unstash build') {
                unstashBuildFolders()
              }
              stage('Build') {
                sh './gradlew --no-daemon --parallel build'
              }
              stage('Integration Tests') {
                sh './gradlew --no-daemon --parallel integrationTest'
              }
              stage('Acceptance Tests') {
                sh './gradlew --no-daemon --parallel acceptanceTest --tests Web3Sha3AcceptanceTest --tests PantheonClusterAcceptanceTest --tests MiningAcceptanceTest'
              }
              stage('Check Licenses') {
                sh './gradlew --no-daemon --parallel checkLicenses'
              }
              stage('Check javadoc') {
                sh './gradlew --no-daemon --parallel javadoc'
              }
              stage('Jacoco root report') {
                sh './gradlew --no-daemon jacocoRootReport'
              }
            } finally {
              archiveArtifacts '**/build/reports/**'
              archiveArtifacts '**/build/test-results/**'
              archiveArtifacts 'build/reports/**'
              archiveArtifacts 'build/distributions/**'

              junit '**/build/test-results/**/*.xml'
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
                sh './gradlew --no-daemon --parallel referenceTest'
              }
            } finally {
              archiveArtifacts '**/build/reports/**'
              archiveArtifacts '**/build/test-results/**'
              archiveArtifacts 'build/reports/**'
              archiveArtifacts 'build/distributions/**'

              junit '**/build/test-results/**/*.xml'
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
                sh 'DOCKER_HOST=tcp://docker:2375 ./gradlew --no-daemon --parallel clean dockerQuickstartTest'
              }
            } finally {
              archiveArtifacts '**/build/test-results/**'
              archiveArtifacts '**/build/reports/**'

              junit '**/build/test-results/**/*.xml'
            }
          }
        }
      }
    }
  }
}
