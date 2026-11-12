// Pipeline de PNI-HCE
// feature/* y develop: build, tests, cobertura, SonarQube y Quality Gate
// main: además, despliegue a producción con aprobación manual

pipeline {
  agent any

  options {
    timestamps()
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timeout(time: 30, unit: 'MINUTES')
  }

  triggers {
    // Sin URL pública no hay webhook de GitHub: Jenkins revisa el repo cada 5 minutos
    pollSCM('H/5 * * * *')
  }

  environment {
    // Servicios Java que ya tienen pom.xml (se detectan solos)
    SERVICIOS = 'services/mpi services/fhir services/consent services/audit services/gateway'
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
        sh 'git log -1 --oneline'
      }
    }

    stage('Build y tests') {
      steps {
        script {
          def conPom = env.SERVICIOS.split(' ').findAll { fileExists("${it}/pom.xml") }
          if (conPom.isEmpty()) {
            echo 'Aún no hay servicios con pom.xml; se omite esta etapa.'
          }
          conPom.each { svc ->
            dir(svc) {
              // verify = compila + tests unitarios e integración + reporte JaCoCo
              sh 'mvn -B -ntp clean verify'
            }
          }
          env.SERVICIOS_CON_POM = conPom.join(' ')
        }
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml'
        }
      }
    }

    stage('Análisis SonarQube') {
      when { expression { env.SERVICIOS_CON_POM?.trim() } }
      steps {
        script {
          env.SERVICIOS_CON_POM.split(' ').each { svc ->
            def nombre = svc.tokenize('/').last()
            dir(svc) {
              withSonarQubeEnv('sonarqube') {
                sh """mvn -B -ntp sonar:sonar \
                  -Dsonar.projectKey=pni-hce-${nombre} \
                  -Dsonar.projectName='PNI-HCE ${nombre}' \
                  -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml"""
              }
            }
          }
        }
      }
    }

    stage('Quality Gate') {
      when { expression { env.SERVICIOS_CON_POM?.trim() } }
      steps {
        timeout(time: 5, unit: 'MINUTES') {
          // Detiene el pipeline si el Quality Gate falla (cobertura, bugs, vulnerabilidades)
          waitForQualityGate abortPipeline: true
        }
      }
    }

    stage('Despliegue a producción') {
      when { branch 'main' }
      steps {
        input message: '¿Desplegar a producción en AWS?', ok: 'Desplegar'
        echo 'Pendiente: script de despliegue a EC2 (infra/deploy/deploy-prod.sh)'
      }
    }
  }

  post {
    success { echo 'Pipeline OK' }
    failure { echo 'Pipeline falló: revisar etapa en rojo' }
  }
}
