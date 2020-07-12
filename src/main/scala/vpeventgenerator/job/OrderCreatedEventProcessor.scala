package vpeventgenerator.job

import java.io.{BufferedWriter, FileWriter}

import com.rabbitmq.client.{ConnectionFactory, DeliverCallback, Delivery}
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.lang3.SerializationUtils
import org.quartz.{Job, JobExecutionContext}
import vpeventgenerator.cli.Config
import vpeventgenerator.constant.QueueName
import vpeventgenerator.model.Order

class OrderCreatedEventProcessor extends Job with LazyLogging with DeliverCallback {

  val FILE_NAME = "order"
  var outputDirectory: String = _

  override def execute(jobExecutionContext: JobExecutionContext): Unit = {

    outputDirectory = jobExecutionContext.getJobDetail.getJobDataMap.getString(Config.OUTPUT_DIRECTORY)

    val factory = new ConnectionFactory
    factory.setHost("localhost")
    val connection = factory.newConnection
    val channel = connection.createChannel


    channel.queueDeclare(QueueName.ORDER_CREATED, false, false, false, null)
    logger.info(" [*] Waiting for messages.")
    channel.basicConsume(QueueName.ORDER_CREATED, true, this, _ => {})
  }

  override def handle(consumerTag: String, message: Delivery): Unit = {
    val order = SerializationUtils.deserialize[Order](message.getBody)
    logger.info(s"================== Received created order : $order ==================")
    writeJson(order)
  }

  def writeJson(order: Order): Unit = {
    val writer = new BufferedWriter(new FileWriter(s"$outputDirectory/$FILE_NAME${System.currentTimeMillis()}.json"))
    val orderJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(order)
    writer.write(orderJson)
    writer.close()
  }
}
