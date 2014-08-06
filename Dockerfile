FROM dockerfile/java

RUN apt-get install redis-server supervisor -y
RUN mkdir /var/log/supervisord
ADD supervisord.conf /etc/supervisor/conf.d/proxy.conf

EXPOSE 9090

ADD target/fastchat-2.0.0-standalone.jar /root/fastchat.jar

CMD [ "supervisord" ]

