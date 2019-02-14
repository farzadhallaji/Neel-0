package com.neelnetwork.lang

trait Versioned {
  type Ver <: ScriptVersion
  val version: Ver
}
