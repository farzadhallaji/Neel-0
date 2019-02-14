package com.neelnetwork

import com.neelnetwork.utils.base58Length
import com.neelnetwork.block.{Block, MicroBlock}

package object transaction {

  type AssetId = com.neelnetwork.state.ByteStr
  val AssetIdLength: Int       = com.neelnetwork.crypto.DigestSize
  val AssetIdStringLength: Int = base58Length(AssetIdLength)
  type DiscardedTransactions = Seq[Transaction]
  type DiscardedBlocks       = Seq[Block]
  type DiscardedMicroBlocks  = Seq[MicroBlock]
  type AuthorizedTransaction = Authorized with Transaction
}
