FROM tomcat:8.0.53-jre8
RUN apt update && apt install -y vim procps net-tools
WORKDIR /usr/local/tomcat/
COPY ./build/libs/async-service.war ./webapps
COPY ./conf/server.xml ./conf
ENV CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,address=8001,server=y,suspend=n"
CMD ["/usr/local/tomcat/bin/catalina.sh","run"]