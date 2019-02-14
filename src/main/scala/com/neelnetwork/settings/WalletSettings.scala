package com.neelnetwork.settings

import java.io.File

import com.neelnetwork.state.ByteStr

case class WalletSettings(file: Option[File], password: Option[String], seed: Option[ByteStr])
