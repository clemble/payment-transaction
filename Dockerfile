FROM java:8-jre
MAINTAINER antono@clemble.com

EXPOSE 8080

ADD target/payment-transaction-0.17.0-SNAPSHOT.jar /data/payment-transaction.jar

CMD java -jar -Dspring.profiles.active=cloud /data/payment-transaction.jar
