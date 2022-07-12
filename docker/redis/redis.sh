docker stop redis7.0
docker rm redis7.0
docker run --name redis7.0 --network canal-network  -p 6379:6379 -d  redis
docker run --rm -it --network canal-network redis redis-cli -h redis7.0
