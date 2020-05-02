cd ..
./mvnw package
 cp target/video-encoder-1.0-SNAPSHOT-runner.jar openshift/deployment
 cd openshift/deployment
 oc start-build --from-dir . video-encoder-app