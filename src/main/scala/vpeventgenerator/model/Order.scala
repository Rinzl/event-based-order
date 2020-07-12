package vpeventgenerator.model

import scala.beans.BeanProperty

case class Order(@BeanProperty Type: String, @BeanProperty Data: Data) extends Serializable
