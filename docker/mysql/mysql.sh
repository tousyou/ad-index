#!/bin/bash
MYSQL_DIR="/Users/lids/IdeaProjects/ad-index/wuliang/docker/mysql"
PASSWORD="lids"
NETWORK="canal-network"

docker network rm $NETWORK
docker stop mysql8.0
docker rm mysql8.0
sleep 20

rm -rf $MYSQL_DIR/data/*
docker network create $NETWORK
docker run --name mysql8.0 --network $NETWORK -p 3306:3306 -v $MYSQL_DIR/config:/etc/mysql/conf.d -v $MYSQL_DIR/data:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=$PASSWORD -d mysql

docker cp $MYSQL_DIR/sql/create_table.sql mysql8.0:/tmp/.
date
sleep 60
date
docker exec -it mysql8.0 mysql -h127.0.0.1 -uroot -plids -e "source /tmp/create_table.sql;"

docker exec -i mysql-server sh -c 'exec mysql -uroot -plids' < mysql/sql/create_table.sql 
