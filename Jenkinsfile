pipeline {
    agent any

    environment {
        // MAVEN_OPTS can be configured if you want special garbage collection or memory limits for builds
        MAVEN_OPTS = '-Xmx1024m'
        // Testcontainers configuration for Jenkins DooD via TCP Proxy
        DOCKER_HOST = 'tcp://127.0.0.1:2375'
        TESTCONTAINERS_RYUK_DISABLED = 'true'
        TESTCONTAINERS_CHECKS_DISABLE = 'true'
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
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
                echo 'Running Unit & Integration Tests...'
                // Start a TCP proxy to the Docker socket to bypass Java's WSL2 Unix Domain Socket bugs
                sh '''
                if [ ! -f socat ]; then
                    curl -sLo socat https://github.com/andrew-d/static-binaries/raw/master/binaries/linux/x86_64/socat
                    chmod +x socat
                fi
                # Kill any existing socat
                pkill -f 'socat TCP-LISTEN:2375' || true
                ./socat TCP-LISTEN:2375,fork,bind=127.0.0.1 UNIX-CONNECT:/var/run/docker.sock &
                sleep 2
                ./mvnw verify
                '''
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
