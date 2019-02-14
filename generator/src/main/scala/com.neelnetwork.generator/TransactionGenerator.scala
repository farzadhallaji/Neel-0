package com.neelnetwork.generator

import com.neelnetwork.transaction.Transaction

trait TransactionGenerator extends Iterator[Iterator[Transaction]] {
  override val hasNext = true
}
