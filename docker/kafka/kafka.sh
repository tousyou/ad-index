docker stop kafka-server
docker rm kafka-server
docker run -d --name kafka-server --network canal-network -p 9092:9092 -e ALLOW_PLAINTEXT_LISTENER=yes -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper-server:2181 bitnami/kafka
#docker run -d --name kafka-server --network canal-network -p 9092:9092 -e KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true -eKAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://127.0.0.1:9092 -e ALLOW_PLAINTEXT_LISTENER=yes -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper-server:2181 bitnami/kafka

