package com.petlew.gacek.visualisation

import java.net.InetSocketAddress

import com.typesafe.config.Config

object VisualizationConfig {

  def fromConfig(config: Config) = VisualizationConfig(
    address = new InetSocketAddress(
      config.getString("com.petlew.gacek.visualization.server.address.host"),
      config.getInt("com.petlew.gacek.visualization.server.address.port")
    )
  )
}

case class VisualizationConfig(address: InetSocketAddress)

