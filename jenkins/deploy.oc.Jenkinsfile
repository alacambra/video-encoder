pipeline{
    agent {
        label 'maven-j13'
    }

    environment {
        APPLICATION_NAME = "video-encoder-app"
        BUILD_CONFIG_SELECTOR = "bc"
        DEPLOY_CONFIG_SELECTOR = "dc"
    }

    stages{
        
        stage('package') {
            steps{
                sh script: "./mvnw package -Dmaven.test.skip=true"
                sh script: "mkdir ./deployment-${env.BUILD_ID}"
                sh script: "cp ./target/video-encoder-runner.jar ./deployment-${env.BUILD_ID}/"
            }
        }

        stage('s2i build'){
            steps{
                script{
                    openshift.withCluster(){
                        openshift.withProject(){
                            def build = openshift.selector(env.BUILD_CONFIG_SELECTOR, env.APPLICATION_NAME);
                            def startedBuild = build.startBuild("--from-file=\"./deployment-${env.BUILD_ID}\"");
                            startedBuild.logs('-f');
                            echo "${env.APPLICATION_NAME} build status: ${startedBuild.object().status}";
                        }
                    }
                }
            }
        }

        stage("wait for build"){
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            def latestBcVersion = openshift.selector(env.BUILD_CONFIG_SELECTOR, env.APPLICATION_NAME).object().status
                            timeout (time: 20, unit: 'SECONDS') {
                                while(true) {
                                    if(openshift.selector("pods", env.APPLICATION_NAME + "-" + latestBcVersion.lastVersion + "-build").object().status.containerStatuses[0].state.terminated.reason == "Completed"){
                                    return true;
                                    }
                                    print openshift.selector("pods", env.APPLICATION_NAME + "-" + latestBcVersion.lastVersion + "-build").object().status.containerStatuses[0].state.terminated.reason
                                }
                            }
                        }
                    }
                }
            }
        }

        stage("print info"){
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            def latestBcVersion = openshift.selector(env.BUILD_CONFIG_SELECTOR, env.APPLICATION_NAME).object().status
                            print "openshift.selector(env.BUILD_CONFIG_SELECTOR, env.APPLICATION_NAME).object(): " + openshift.selector(env.BUILD_CONFIG_SELECTOR, env.APPLICATION_NAME).object()
                            print "status: " + latestBcVersion
                            print "openshift.selector(env.BUILD_CONFIG_SELECTOR, env.APPLICATION_NAME).related('pods'): " + openshift.selector(env.BUILD_CONFIG_SELECTOR, env.APPLICATION_NAME).related('pods')
                            print "podName: " + env.APPLICATION_NAME + "-" + latestBcVersion.lastVersion + "-build"
                            print "pod: " + openshift.selector("pods", env.APPLICATION_NAME + "-" + latestBcVersion.lastVersion + "-build").object().status.containerStatuses[0].state
                        }
                    }
                }
            }
        }

        stage('deploy') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            openshift.selector(env.DEPLOY_CONFIG_SELECTOR, env.APPLICATION_NAME).rollout().latest()
                        }
                    }
                }
            }
        }

        stage('wait for deployment') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject() {
                            def latestDeploymentVersion = openshift.selector(env.DEPLOY_CONFIG_SELECTOR, env.APPLICATION_NAME).object().status.latestVersion
                            def rc = openshift.selector('rc', "${env.APPLICATION_NAME}-${latestDeploymentVersion}")
                            timeout (time: 10, unit: 'SECONDS') {
                                rc.untilEach(1){
                                    def rcMap = it.object()
                                    return (rcMap.status.replicas.equals(rcMap.status.readyReplicas))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}