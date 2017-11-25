#!/bin/bash
# This file carries some examples to copy/paste while trying out the plugin

# If using docker swarm, use this command to create the internal network on which other docker containers
# and the Database can interact
docker network create --driver overlay cluster

# If using docker swarm, use this command to spin up a postgres database
docker service create --name clusterdb --network cluster -p 5432:5432 -e POSTGRES_PASSWORD=cluster -e POSTGRES_USER=cluster -e POSTGRES_DB=cluster postgres:latest

# Some example command to run peez/hivemq together with the previously started postgres database
docker service create --name hivemq --network cluster -p 1883:1883 -e HIVEMQ_CLUSTER_JDBC_URL=jdbc:postgresql://clusterdb:5432/cluster -e HIVEMQ_CLUSTER_JDBC_USER=cluster -e HIVEMQ_CLUSTER_JDBC_PASSWORD=cluster peez/hivemq:latest