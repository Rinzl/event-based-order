package vpeventgenerator.cli

case class Config(
                 numberOfOrders: Int = 0,
                 batchSize: Int = 0,
                 interval: Int = 0,
                 outputDirectory: String = "/")

object Config {
  val NUMBER_OR_ORDERS = "number_of_orders"
  val BATCH_SIZE = "batch_size"
  val INTERVAL = "interval"
  val OUTPUT_DIRECTORY = "output_directory"
}
