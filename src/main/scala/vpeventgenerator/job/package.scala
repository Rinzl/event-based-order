package vpeventgenerator

import java.io.{BufferedWriter, File, FileWriter}

import com.fasterxml.jackson.databind.ObjectMapper
import vpeventgenerator.model.Order

package object job {
  val FILE_NAME = "order"
  val jsonMapper = new ObjectMapper()

  def writeJson(order: Order, dir: String): Unit = {
    val file = new File(s"$dir/")
    if (!file.exists()) {
      file.mkdir()
    }
    val writer = new BufferedWriter(new FileWriter(s"$dir/$FILE_NAME${System.currentTimeMillis()}.json"))
    val orderJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(order)
    writer.write(orderJson)
    writer.close()
  }
}
