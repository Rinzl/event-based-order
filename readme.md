# Technical Task

## Dependencies
- Maven 3.6.3
- Java 1.8
- RabbitMQ
- Docker and docker compose
- Window Terminal or powershell if you using window

## How to run
1. cd to project directory.
2. Run docker-compose up -d to deploy RabbitMQ.
3. Run mvn package.
4. Run java -jar target/vp-event-generator-1.0-SNAPSHOT-jar-with-dependencies.jar --number-of-orders 100 --batch-size 23 --interval 1000 --output-directory "order-json/".
   
   - Adjust parameters base on your configuration.   
   - App arguments:   
   ![alt text](https://github.com/Rinzl/event-based-order/raw/master/app-arguments.png "app's arguments")

## 

* I think the use case of this program is the demonstration of how components of the app communicate and how data will be collected through each event.
* I spent about 2 days on this project.
* If i have more time, I will optimize source code and convert it to microservices
* In my project, i decided to go with Scala because it is very dynamic, concise and easy to write code when compare to Java. Scala can use java libraries and even writes java code in scala project.

