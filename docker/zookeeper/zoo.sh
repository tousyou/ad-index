docker stop zookeeper-server
docker rm zookeeper-server
docker run --name zookeeper-server --network canal-network -p 2181:2181 -e ALLOW_ANONYMOUS_LOGIN=yes -d bitnami/zookeeper
#docker run --name canal-zookeeper --network canal-network -p 2181:2181 -p 2888:2888 -p 3888:3888 -p 8080:8080 --restart always -d zookeeper

