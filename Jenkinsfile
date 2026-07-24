pipeline {
    agent any

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
                sh 'chmod +x mvnw'
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
                echo 'Building Docker images...'
                script {
                    def services = [
                        'api-gateway', 'config-server', 'eureka', 'notification-service', 
                        'order-service', 'payment-service', 'product-service', 
                        'recommendation-service', 'user-service'
                    ]
                    for (service in services) {
                        echo "Building ${service}..."
                        sh "docker build -t ultrahpm/${service}:latest -f ${service}/Dockerfile ."
                    }
                }
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
