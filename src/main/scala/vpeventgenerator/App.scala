package vpeventgenerator

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.lang3.time.DateUtils
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{JobBuilder, JobDetail, JobKey, Trigger, TriggerBuilder}
import scopt.OParser
import vpeventgenerator.cli.{Config, OParserBuilder}
import vpeventgenerator.job.{EventGenerator, OrderPlacedEventProcessor}

/**
 * @author dangth
 */
object App extends LazyLogging{

  def createEventGeneratorJobAndTrigger(config: Config): (JobDetail, Trigger) = {
    val EventGeneratorJobKey = new JobKey("EventGenerator", "group1")
    val eventGeneratorJob = JobBuilder
      .newJob(classOf[EventGenerator])
      .withIdentity(EventGeneratorJobKey)
      .build()

    val jobDataGenerator = eventGeneratorJob.getJobDataMap
    jobDataGenerator.put(Config.NUMBER_OR_ORDERS, config.numberOfOrders)
    jobDataGenerator.put(Config.BATCH_SIZE, config.batchSize)
    jobDataGenerator.put(Config.INTERVAL, config.interval)

    val triggerWithDelay = TriggerBuilder
      .newTrigger
      .withIdentity("triggerWithDelay", "group1")
      .startAt(DateUtils.addSeconds(new Date(), 15))
      .build()

    (eventGeneratorJob, triggerWithDelay)
  }

  def initializeApp(config: Config): Unit = {

    val orderCreatedProcessorJobKey = new JobKey("orderCreatedProcessor", "group1")

    val orderCreatedJob = JobBuilder
      .newJob(classOf[OrderPlacedEventProcessor])
      .withIdentity(orderCreatedProcessorJobKey)
      .build()

    val jobDataOrderCreated = orderCreatedJob.getJobDataMap
    jobDataOrderCreated.put(Config.OUTPUT_DIRECTORY, config.outputDirectory)

    val runOnceTrigger = TriggerBuilder
      .newTrigger
      .withIdentity("RunOneTrigger", "group1")
      .build()

    val scheduler = new StdSchedulerFactory().getScheduler

    scheduler.start()

    scheduler.scheduleJob(orderCreatedJob, runOnceTrigger)

    val eventGeneratorJobAndTrigger = createEventGeneratorJobAndTrigger(config)
    scheduler.scheduleJob(eventGeneratorJobAndTrigger._1, eventGeneratorJobAndTrigger._2)
  }

  def main(args : Array[String]): Unit = {

    // OParser.parse returns Option[Config]
    OParser.parse(OParserBuilder.build(), args, Config()) match {
      case Some(config) =>
        logger.info(s"Input config : $config")
        initializeApp(config)
      case _ =>
      // arguments are bad, error message will have been displayed
    }
  }

}
