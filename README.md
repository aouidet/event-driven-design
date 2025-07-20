
# Execute Project
To run docker compose file, and check if all containers are running using 
docker compose ps command.

```bash
  docker-compose docker compose -f common.yml -f kafka_cluster.yml -f services.yml up
  docker ps
```
## Documentation
[[confluent]Configure a multi-node environment](https://docs.confluent.io/platform/current/kafka/multi-node.html)

```bash
docker pull confluentinc/cp-kafkacat
# list topic in a broker
docker run -it \
           --network host \
           confluentinc/cp-kafkacat \
           kafkacat -L -b localhost:19091
 # sur macOS, l'option --network host ne fonctionne pas comme sur Linux. En effet, Docker Desktop pour Mac ne prend pas en charge le mode host networking, car il utilise une machine virtuelle Linux interne.
 docker run -it --rm confluentinc/cp-kafkacat \
  kafkacat -L -b host.docker.internal:19091

# --rm : pour supprimer le conteneur après utilisation (optionnel mais propre).
# host.docker.internal : remplace localhost sur macOS.

# Test from host : nc -vz localhost 19091
```

kcat (formerly kafkacat) is a command-line utility that you can use to test and debug Apache Kafka® deployments. You can use kcat to produce, consume, and list topic and partition information for Kafka. Described as “netcat for Kafka”, it is a swiss-army knife of tools for inspecting and creating data in Kafka.

[Documentation Kafkacat](https://docs.confluent.io/platform/current/tools/kafkacat-usage.html)

