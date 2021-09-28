library("tdr-jenkinslib")

def repo = "tdr-local-aws"

pipeline {
  agent {
    label "built-in"
  }

  stages {
    stage("Report build start") {
      steps {
        script {
          tdr.reportStartOfBuildToGitHub(repo, env.GIT_COMMIT)
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
        tdr.reportFailedBuildToGitHub(repo, env.GIT_COMMIT)
      }
    }
    success {
      script {
        tdr.reportSuccessfulBuildToGitHub(repo, env.GIT_COMMIT)
      }
    }
  }
}
