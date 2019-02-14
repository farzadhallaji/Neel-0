package com.neelnetwork.transaction

import com.neelnetwork.account.Address

case class AssetAcc(account: Address, assetId: Option[AssetId])
