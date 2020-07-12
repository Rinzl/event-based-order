package vpeventgenerator.cli

import scopt.OParser

object OParserBuilder {
  def build(): OParser[Unit, Config] = {
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
          .text("interval in millisecond"),
        opt[String]('o', "output-directory")
          .required()
          .action((x, c) => c.copy(outputDirectory = x))
          .text("output directory")
      )
    }
    parser
  }
}
