FROM java:8

RUN git clone https://github.com/fvillalobos14/LP_Kotlin

CMD cd /LP_Kotlin && java -jar kot.jar

EXPOSE 8080