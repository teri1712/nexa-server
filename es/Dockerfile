FROM curlimages/curl:8.19.0

USER root

WORKDIR /es
COPY ./init.sh ./init.sh
VOLUME ./schema ./schema
RUN chmod +x ./init.sh

ENTRYPOINT ["./init.sh"]
