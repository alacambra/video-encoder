cd s2i-image
docker build -t alacambra/video-encoder .
docker push alacambra/video-encoder:latest
oc import-image  video-encoder --from=alacambra/video-encoder:latest