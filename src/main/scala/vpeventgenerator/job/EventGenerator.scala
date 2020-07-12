package vpeventgenerator.job

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

import com.rabbitmq.client.ConnectionFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.lang3.SerializationUtils
import org.quartz.{Job, JobExecutionContext}
import vpeventgenerator.cli.Config
import vpeventgenerator.constant.EventType
import vpeventgenerator.model.{Data, Order}

class EventGenerator extends Job with LazyLogging {

  override def execute(jobExecutionContext: JobExecutionContext): Unit = {

    val data = jobExecutionContext.getJobDetail.getJobDataMap
    val factory = new ConnectionFactory()
    var currentNumberOfOrders = data.getInt(Config.NUMBER_OR_ORDERS)
    factory.setHost("localhost")
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    channel.queueDeclare(EventType.ORDER_PLACED, false, false, false, null)

    var count = 0
    while (currentNumberOfOrders > 0) {
      count += 1
      currentNumberOfOrders -= 1

      // create order
      val id = UUID.randomUUID().toString
      val dateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
      val orderData = Data(id, dateTime)
      val order = Order(EventType.ORDER_PLACED, orderData)
      channel.basicPublish("", EventType.ORDER_PLACED, null, SerializationUtils.serialize(order))
      logger.info(s"New order is created : $order")
      if (count == data.getInt(Config.BATCH_SIZE)) {
        logger.info("=============================================================================")
        Thread.sleep(data.getInt(Config.INTERVAL))
        count = 0
      }
    }
  }
}
