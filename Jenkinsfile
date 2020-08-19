library("tdr-jenkinslib")

def repo = "tdr-local-aws"

pipeline {
  agent {
    label "master"
  }

  stages {
    stage("Report build start") {
      steps {
        script {
          tdr.reportStartOfBuildToGitHub(repo)
        }
      }
    }
    stage("Checkout") {
      steps {
        checkout scm
      }
    }
    stage("Run git secrets") {
      steps {
        script {
          tdr.runGitSecrets(repo)
        }
      }
    }
    stage('Test') {
      agent {
        ecs {
          inheritFrom 'transfer-frontend'
        }
      }
      steps {
        sh 'sbt -no-colors test'
      }
    }
  }
  post {
    failure {
      script {
        tdr.reportFailedBuildToGitHub(repo)
      }
    }
    success {
      script {
        tdr.reportSuccessfulBuildToGitHub(repo)
      }
    }
  }
}
