FROM docker.elastic.co/logstash/logstash:7.17.16
RUN bin/logstash-plugin install logstash-output-mongodb
COPY logstash.conf /usr/share/logstash/pipeline/logstash.conf
