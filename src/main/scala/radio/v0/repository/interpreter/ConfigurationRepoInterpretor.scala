package radio.v0
package repository
package interpreter

import com.typesafe.config.{Config, ConfigFactory}

object ConfigurationRepoInterpretor extends ConfigurationRepo[Config]{
  override def config: Config = ConfigFactory.load()
}
