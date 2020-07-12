package vpeventgenerator

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.lang3.time.DateUtils
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{JobBuilder, JobDetail, JobKey, Trigger, TriggerBuilder}
import scopt.OParser
import vpeventgenerator.cli.Config
import vpeventgenerator.job.{EventGenerator, OrderCreatedEventProcessor}

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
      .newJob(classOf[OrderCreatedEventProcessor])
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

    val jobAndTrigger = createEventGeneratorJobAndTrigger(config)
    scheduler.scheduleJob(jobAndTrigger._1, jobAndTrigger._2)
  }

  def main(args : Array[String]): Unit = {
    val builder = OParser.builder[Config]
    val parser = {
      import builder._
      OParser.sequence(
        programName("vp-event-generator"),
        head("vp-event-generator", "1.0"),
        opt[Int]('n', "number-of-orders")
          .required()
          .action((x, c) => c.copy(numberOfOrders = x))
          .text("Number of orders"),
        opt[Int]('b', "batch-size")
          .required()
          .action((x, c) => c.copy(batchSize = x))
          .text("batch size"),
        opt[Int]('i', "interval")
          .required()
          .action((x, c) => c.copy(interval = x))
          .text("interval"),
        opt[String]('o', "output-directory")
          .required()
          .action((x, c) => c.copy(outputDirectory = x))
          .text("output directory")
      )
    }

    // OParser.parse returns Option[Config]
    OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        logger.info(s"Input config : $config")
        initializeApp(config)
      case _ =>
      // arguments are bad, error message will have been displayed
    }
  }

}
