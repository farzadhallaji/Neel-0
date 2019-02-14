package com.neelnetwork.it.sync.smartcontract

import com.typesafe.config.{Config, ConfigFactory}
import com.neelnetwork.account.PrivateKeyAccount
import com.neelnetwork.it.api.SyncHttpApi._
import com.neelnetwork.it.transactions.NodesFromDocker
import com.neelnetwork.it.{ReportingTestName, WaitForHeight2}
import com.neelnetwork.it.util._
import com.neelnetwork.it.sync._
import com.neelnetwork.transaction.smart.SetScriptTransaction
import com.neelnetwork.transaction.smart.script.ScriptCompiler
import com.neelnetwork.transaction.transfer.TransferTransactionV2
import org.scalatest.{CancelAfterFailure, FreeSpec, Matchers}

class UTXAllowance extends FreeSpec with Matchers with WaitForHeight2 with CancelAfterFailure with ReportingTestName with NodesFromDocker {
  import UTXAllowance._

  override protected def nodeConfigs: Seq[Config] = Configs

  private def nodeA = nodes.head
  private def nodeB = nodes.last

  "create two nodes with scripted accounts and check UTX" in {
    val accounts = List(nodeA, nodeB).map(i => {

      val nodeAddress = i.createAddress()
      val acc         = PrivateKeyAccount.fromSeed(i.seed(nodeAddress)).right.get

      val tx = i.transfer(i.address, nodeAddress, 10.neel, 0.005.neel).id
      nodes.waitForHeightAriseAndTxPresent(tx)

      val scriptText = s"""true""".stripMargin

      val script = ScriptCompiler(scriptText, isAssetScript = false).explicitGet()._1
      val setScriptTransaction = SetScriptTransaction
        .selfSigned(SetScriptTransaction.supportedVersions.head, acc, Some(script), setScriptFee, System.currentTimeMillis())
        .right
        .get

      val setScriptId = i
        .signedBroadcast(setScriptTransaction.json())
        .id

      nodes.waitForHeightAriseAndTxPresent(setScriptId)
      acc
    })

    val txA =
      TransferTransactionV2
        .selfSigned(
          version = 2,
          assetId = None,
          sender = accounts(0),
          recipient = accounts(0),
          amount = 1.neel,
          timestamp = System.currentTimeMillis(),
          feeAssetId = None,
          feeAmount = minFee + 0.004.neel,
          attachment = Array.emptyByteArray
        )
        .right
        .get
    assertBadRequestAndMessage(
      nodeA.signedBroadcast(txA.json()),
      "transactions from scripted accounts are denied from UTX pool"
    )

    val txB =
      TransferTransactionV2
        .selfSigned(
          version = 2,
          assetId = None,
          sender = accounts(1),
          recipient = accounts(1),
          amount = 1.neel,
          timestamp = System.currentTimeMillis(),
          feeAssetId = None,
          feeAmount = minFee + 0.004.neel,
          attachment = Array.emptyByteArray
        )
        .right
        .get

    val txBId = nodeB.signedBroadcast(txB.json()).id
    nodes.waitForHeightArise()
    nodeA.findTransactionInfo(txBId) shouldBe None
  }

}

object UTXAllowance {
  import com.neelnetwork.it.NodeConfigs._
  private val FirstNode = ConfigFactory.parseString(s"""
                                                         |neel {
                                                         |  utx.allow-transactions-from-smart-accounts = false
                                                         |  miner {
                                                         |      quorum = 0
                                                         |      enable = yes
                                                         |  }
                                                         |}""".stripMargin)

  private val SecondNode = ConfigFactory.parseString(s"""
                                                          |neel {
                                                          |  utx.allow-transactions-from-smart-accounts = true
                                                          |  miner {
                                                          |      enable = no
                                                          |  }
                                                          |}""".stripMargin)

  val Configs: Seq[Config] = Seq(
    FirstNode.withFallback(Default.head),
    SecondNode.withFallback(Default(1))
  )

}
