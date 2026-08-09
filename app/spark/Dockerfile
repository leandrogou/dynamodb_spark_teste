FROM eclipse-temurin:17-jdk

# Instala dependências
RUN apt-get update && apt-get install -y curl wget tar && rm -rf /var/lib/apt/lists/*
#RUN yum install -y curl wget tar && yum clean all

# Configura Java para Spark 3.5.1 com Java 17
ENV JAVA_TOOL_OPTIONS="-XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED"

# Baixa Spark 3.5.1 pré-compilado com Hadoop
ENV SPARK_VERSION=3.5.1
ENV HADOOP_VERSION=3
ENV SPARK_HOME=/opt/spark
ENV PATH=$SPARK_HOME/bin:$PATH


RUN wget https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
    && tar -xvzf spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz -C /opt \
    && mv /opt/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} /opt/spark \
    && rm spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

WORKDIR /opt/spark

#CMD ["bin/spark-shell"]
#CMD ["bin/spark-class", "org.apache.spark.deploy.worker.Worker", "spark://master:7077"]
