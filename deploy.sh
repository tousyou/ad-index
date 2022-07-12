./mvnw clean -f pom.xml -P prod
./mvnw package -f pom.xml -DskipTests=true -P prod

rm -rf target/dependency
mkdir -p target/dependency
cd target/dependency
jar -xf ../*.jar

cd ../../
docker build -t lids/ad-index .

cd docker
docker-compose down
docker-compose up -d

