FROM openjdk:17.0-jdk
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /ad-index/lib
COPY ${DEPENDENCY}/META-INF /ad-index/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /ad-index
RUN rm /ad-index/logback-test.xml
RUN mkdir -p /ad-index/data
RUN mkdir -p /ad-index/log
EXPOSE 8080
ENTRYPOINT ["java","-cp","ad-index:ad-index/lib/*","lids.ad.wuliang.WuliangApplication"]
