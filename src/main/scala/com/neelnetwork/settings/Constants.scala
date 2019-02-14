package com.neelnetwork.settings

import com.neelnetwork.Version
import com.neelnetwork.utils.ScorexLogging

/**
  * System constants here.
  */
object Constants extends ScorexLogging {
  val ApplicationName = "neel"
  val AgentName       = s"Neel v${Version.VersionString}"

  val UnitsInWave = 100000000L
  val TotalNeel  = 100000000L
}
