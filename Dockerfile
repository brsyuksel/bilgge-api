FROM ghcr.io/graalvm/graalvm-ce:ol8-java8-21.0.0 AS builder

RUN cd /root && \
    curl -L https://github.com/sbt/sbt/releases/download/v1.4.7/sbt-1.4.7.tgz --output sbt.tgz && \
    tar xvf sbt.tgz && \
    mv -v sbt /opt/sbt

ENV PATH="/opt/sbt/bin:${PATH}"

RUN mkdir /source
WORKDIR /source

COPY . .
RUN sbt packArchive

FROM ghcr.io/graalvm/graalvm-ce:ol8-java8-21.0.0

RUN mkdir /app
WORKDIR /app

COPY --from=builder /source/target/bilgge-0.1.1.tar.gz .
RUN tar xvf bilgge-0.1.1.tar.gz
RUN rm -v bilgge-*.tar.gz && mv -v bilgge-* bilgge && cd bilgge/bin

WORKDIR /app/bilgge/bin
CMD ./bilgge
