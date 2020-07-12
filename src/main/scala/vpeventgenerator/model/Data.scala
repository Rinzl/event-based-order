package vpeventgenerator.model

import scala.beans.BeanProperty

case class Data(@BeanProperty OrderId: String, @BeanProperty TimestampUtc: String) extends Serializable
