FROM java:8-jre
MAINTAINER antono@clemble.com

EXPOSE 10009

ADD target/payment-transaction-*-SNAPSHOT.jar /data/payment-transaction.jar

CMD java -jar -Dspring.profiles.active=cloud -Dserver.port=10009 /data/payment-transaction.jar
