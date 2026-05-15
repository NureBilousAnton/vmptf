FROM postgres:18-alpine

RUN apk add --no-cache git make \
 && git clone https://github.com/michelp/pgjwt.git /tmp/pgjwt \
 && make -C /tmp/pgjwt install \
 && apk del git make \
 && rm -rf /tmp/pgjwt
