package com.neelnetwork.it

import com.typesafe.config.ConfigFactory.{defaultApplication, defaultReference}
import com.neelnetwork.account.PublicKeyAccount
import com.neelnetwork.block.Block
import com.neelnetwork.consensus.PoSSelector
import com.neelnetwork.db.openDB
import com.neelnetwork.history.StorageFactory
import com.neelnetwork.settings._
import com.neelnetwork.state.{ByteStr, EitherExt2}
import com.neelnetwork.utils.NTP
import net.ceedubs.ficus.Ficus._

object BaseTargetChecker {
  def main(args: Array[String]): Unit = {
    val sharedConfig = Docker.genesisOverride
      .withFallback(Docker.configTemplate)
      .withFallback(defaultApplication())
      .withFallback(defaultReference())
      .resolve()
    val settings     = NeelSettings.fromConfig(sharedConfig)
    val genesisBlock = Block.genesis(settings.blockchainSettings.genesisSettings).explicitGet()
    val db           = openDB("/tmp/tmp-db")
    val time         = new NTP("ntp.pool.org")
    val bu           = StorageFactory(settings, db, time)
    val pos          = new PoSSelector(bu, settings.blockchainSettings)
    bu.processBlock(genesisBlock)

    try {
      NodeConfigs.Default.map(_.withFallback(sharedConfig)).collect {
        case cfg if cfg.as[Boolean]("neel.miner.enable") =>
          val account   = PublicKeyAccount(cfg.as[ByteStr]("public-key").arr)
          val address   = account.toAddress
          val balance   = bu.balance(address, None)
          val consensus = genesisBlock.consensusData
          val timeDelay = pos
            .getValidBlockDelay(bu.height, account.publicKey, consensus.baseTarget, balance)
            .explicitGet()

          f"$address: ${timeDelay * 1e-3}%10.3f s"
      }
    } finally time.close()
  }
}
