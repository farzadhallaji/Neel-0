package com.neelnetwork.transaction

trait VersionedTransaction {
  def version: Byte
}
