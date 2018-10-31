#!/usr/bin/env groovy

buildFolders = [
  'buildSrc',
  'acceptance-tests',
  'consensus/clique',
  'consensus/common',
  'consensus/ibft',
  'consensus/ibftlegacy',
  'crypto',
  'errorprone-checks',
  'ethereum/blockcreation',
  'ethereum/core',
  'ethereum/eth',
  'ethereum/jsonrpc',
  'ethereum/mock-p2p',
  'ethereum/p2p',
  'ethereum/referencetests',
  'ethereum/rlp',
  'ethereum/trie',
  'pantheon',
  'quickstart',
  'testutil',
  'util'
]

void stashBuildFolders() {
  buildFolders.each {location ->
    stash(
      name: location.replace('/', '_'),
      allowEmpty: true,
      includes: "${location}/build/"
    )
  }
}

void unstashBuildFolders() {
  buildFolders.each {location ->
    dir("${location}") {
      unstash(location.replace('/', '_'))
    }
    sh "ls ${location}/build"
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
                sh './gradlew --no-daemon --parallel referenceTest'
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
                sh 'DOCKER_HOST=tcp://docker:2375 ./gradlew --no-daemon --parallel dockerQuickstartTest'
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
