FROM alacambra/java-s2i:13.0.1_9
USER root
RUN yum install epel-release -y
RUN yum update -y
RUN rpm --import http://li.nux.ro/download/nux/RPM-GPG-KEY-nux.ro
RUN rpm -Uvh http://li.nux.ro/download/nux/dextop/el7/x86_64/nux-dextop-release-0-5.el7.nux.noarch.rpm
RUN yum install ffmpeg ffmpeg-devel -y
ENV APP_HOME /opt/app/
ENV APP_WORKING_DIR /opt/jobs/
ADD lib ${APP_HOME}lib
RUN mkdir -p /opt/jobs/ \
    && mkdir -p ${APP_HOME} \
    && chown -R 1001:0 ${APP_WORKING_DIR} \
    && chown -R 1001:0 /opt \
    && chown -R 1001:0 ${APP_HOME}
LABEL io.k8s.description="Video Encoder S2I Image" \
      io.k8s.display-name="Video Encoder  S2I Builder" \
      io.openshift.expose-services="8080:http" \
      io.openshift.tags="builder,lacambra.tech,ffmpeg"
EXPOSE 8080
COPY ./s2i/bin/ /usr/libexec/s2i
USER 1001