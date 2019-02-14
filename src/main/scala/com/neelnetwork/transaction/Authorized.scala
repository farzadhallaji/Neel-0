package com.neelnetwork.transaction

import com.neelnetwork.account.PublicKeyAccount

trait Authorized {
  val sender: PublicKeyAccount
}
