package com.petlew.gacek.visualisation

import java.net.InetSocketAddress

import com.typesafe.config.Config

object VisualizationServerConfig {

  def fromConfig(config: Config) = VisualizationServerConfig(
    interface = config.getString("com.petlew.gacek.visualization.server.backend.address.host"),
    port = config.getInt("com.petlew.gacek.visualization.server.backend.address.port")
  )

}

case class VisualizationServerConfig(interface: String, port: Int)
