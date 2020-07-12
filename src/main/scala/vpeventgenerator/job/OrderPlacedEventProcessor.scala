package vpeventgenerator.job

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.rabbitmq.client.{Channel, ConnectionFactory, DeliverCallback, Delivery}
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.lang3.SerializationUtils
import org.quartz.{Job, JobExecutionContext}
import vpeventgenerator.cli.Config
import vpeventgenerator.constant.EventType
import vpeventgenerator.model.{Data, Order}

class OrderPlacedEventProcessor extends Job with LazyLogging with DeliverCallback {

  var outputDirectory: String = _
  var channel: Channel = _
  var count = 1
  override def execute(jobExecutionContext: JobExecutionContext): Unit = {

    outputDirectory = jobExecutionContext.getJobDetail.getJobDataMap.getString(Config.OUTPUT_DIRECTORY)

    val factory = new ConnectionFactory
    factory.setHost("localhost")
    val connection = factory.newConnection
    channel = connection.createChannel


    channel.queueDeclare(EventType.ORDER_PLACED, false, false, false, null)
    logger.info(" [*] Waiting for messages.")
    channel.basicConsume(EventType.ORDER_PLACED, true, this, _ => {})
  }

  override def handle(consumerTag: String, message: Delivery): Unit = {
    val order = SerializationUtils.deserialize[Order](message.getBody)
    logger.info(s"================== Received created order : $order ==================")
    writeJson(order, outputDirectory)
    Thread.sleep(1000)
    val orderResultData = Data(order.Data.OrderId, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
    if (count == 5) {
      count = 1
      val orderResult = Order(EventType.ORDER_CANCELED, orderResultData)
      writeJson(orderResult, outputDirectory)
    }
    else {
      count += 1
      val orderResult = Order(EventType.ORDER_DELIVERED, orderResultData)
      writeJson(orderResult, outputDirectory)
    }
  }
}
