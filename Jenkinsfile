pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'your-registry.io/tradevision'
        KUBECONFIG = credentials('kubeconfig')
        DOCKER_CREDENTIALS = credentials('docker-registry-credentials')
        IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(8)}"
        SERVICES = 'user-service stock-service trade-service portfolio-service alert-service'
    }

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    echo "Building branch: ${env.BRANCH_NAME}"
                    echo "Commit: ${env.GIT_COMMIT}"
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests --no-transfer-progress'
            }
            post {
                success { echo 'Maven build successful' }
                failure { echo 'Maven build failed' }
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test --no-transfer-progress'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Quality') {
            when {
                branch 'main'
            }
            steps {
                sh 'mvn verify -Psonar --no-transfer-progress'
            }
        }

        stage('Docker Build & Push') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    sh "echo ${DOCKER_CREDENTIALS_PSW} | docker login ${DOCKER_REGISTRY} -u ${DOCKER_CREDENTIALS_USR} --password-stdin"

                    def services = SERVICES.split(' ')
                    services.each { service ->
                        sh """
                            docker build -t ${DOCKER_REGISTRY}/${service}:${IMAGE_TAG} \
                                         -t ${DOCKER_REGISTRY}/${service}:latest \
                                         ./${service}
                            docker push ${DOCKER_REGISTRY}/${service}:${IMAGE_TAG}
                            docker push ${DOCKER_REGISTRY}/${service}:latest
                        """
                    }

                    // Build frontend
                    sh """
                        docker build -t ${DOCKER_REGISTRY}/frontend:${IMAGE_TAG} \
                                     -t ${DOCKER_REGISTRY}/frontend:latest \
                                     ./frontend
                        docker push ${DOCKER_REGISTRY}/frontend:${IMAGE_TAG}
                        docker push ${DOCKER_REGISTRY}/frontend:latest
                    """
                }
            }
        }

        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    sh """
                        export KUBECONFIG=${KUBECONFIG}
                        kubectl apply -f k8s/namespace.yml
                        kubectl apply -f k8s/configmap.yml
                        kubectl apply -f k8s/
                        kubectl rollout status deployment/user-service -n tradevision --timeout=120s
                        kubectl rollout status deployment/stock-service -n tradevision --timeout=120s
                        kubectl rollout status deployment/trade-service -n tradevision --timeout=120s
                        kubectl rollout status deployment/portfolio-service -n tradevision --timeout=120s
                        kubectl rollout status deployment/alert-service -n tradevision --timeout=120s
                        kubectl rollout status deployment/frontend -n tradevision --timeout=120s
                    """
                }
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                input message: 'Deploy to production?', ok: 'Deploy'
                script {
                    sh """
                        export KUBECONFIG=${KUBECONFIG}
                        kubectl apply -f k8s/namespace.yml
                        kubectl apply -f k8s/configmap.yml
                        kubectl apply -f k8s/
                        kubectl rollout status deployment/user-service -n tradevision --timeout=180s
                        kubectl rollout status deployment/stock-service -n tradevision --timeout=180s
                        kubectl rollout status deployment/trade-service -n tradevision --timeout=180s
                        kubectl rollout status deployment/portfolio-service -n tradevision --timeout=180s
                        kubectl rollout status deployment/alert-service -n tradevision --timeout=180s
                        kubectl rollout status deployment/frontend -n tradevision --timeout=180s
                    """
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout ${DOCKER_REGISTRY} || true'
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully for ${env.BRANCH_NAME} - Build #${env.BUILD_NUMBER}"
        }
        failure {
            echo "Pipeline FAILED for ${env.BRANCH_NAME} - Build #${env.BUILD_NUMBER}"
        }
    }
}
