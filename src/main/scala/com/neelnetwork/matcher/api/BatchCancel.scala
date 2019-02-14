package com.neelnetwork.matcher.api
import com.neelnetwork.account.Address
import com.neelnetwork.transaction.assets.exchange.AssetPair

case class BatchCancel(address: Address, assetPair: Option[AssetPair], timestamp: Long)
