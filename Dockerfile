FROM        ubuntu:14.04
RUN         apt-get update && apt-get install -y redis-server
EXPOSE      6379
ENTRYPOINT  ["/usr/bin/redis-server"]

# docker build -t tmt-redis .
# sudo docker run --name tmt-redis-container -d -p 6379:6379 tmt-redis
# sudo docker run -it --name tmt-redis-cli --link tmt-redis-container:redis --rm redis redis-cli -h redis -p 6379