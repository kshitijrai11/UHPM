pipeline {
    agent {
        // Run on the Jenkins master/agent node, assuming docker is available
        docker {
            // We use maven:3.9.6-eclipse-temurin-21 because our project requires Java 21
            image 'maven:3.9.6-eclipse-temurin-21'
            args '-v /var/run/docker.sock:/var/run/docker.sock -v /root/.m2:/root/.m2'
        }
    }

    environment {
        // MAVEN_OPTS can be configured if you want special garbage collection or memory limits for builds
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
        stage('Checkout') {
            steps {
                // Jenkins checks out the repository by default, but we print a message here
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                echo 'Compiling across all modules...'
                sh './mvnw clean compile'
            }
        }

        stage('Test & Analyze') {
            steps {
                echo 'Running Unit & Integration Tests (Testcontainers via Docker socket)...'
                // This runs surefire (unit tests) and failsafe (integration tests)
                sh './mvnw verify'
            }
            post {
                always {
                    // Record test results for Jenkins UI
                    junit '**/target/surefire-reports/TEST-*.xml'
                    junit '**/target/failsafe-reports/TEST-*.xml, null'
                }
            }
        }

        stage('Security Scan (Trivy)') {
            steps {
                echo 'Running security checks...'
                // Placeholder for Trivy or OWASP Dependency Check
                // sh 'trivy fs .'
                echo 'Security scan passed (simulated)'
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging executable JARs...'
                // Skip tests here since we already ran them in the previous stage
                sh './mvnw package -DskipTests'
            }
        }

        stage('Build Docker Images') {
            steps {
                echo 'Building Docker images (simulated)...'
                // Usually handled by spring-boot:build-image or kaniko/docker build
                // sh './mvnw spring-boot:build-image -DskipTests'
            }
        }
    }

    post {
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the logs.'
        }
    }
}
