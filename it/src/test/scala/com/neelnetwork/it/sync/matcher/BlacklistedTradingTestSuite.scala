package com.neelnetwork.it.sync.matcher

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.parseString
import com.neelnetwork.it.api.SyncHttpApi._
import com.neelnetwork.it.api.SyncMatcherHttpApi._
import com.neelnetwork.it.matcher.MatcherSuiteBase
import com.neelnetwork.it.sync._
import com.neelnetwork.it.sync.matcher.config.MatcherPriceAssetConfig._
import com.neelnetwork.transaction.assets.exchange.OrderType._
import org.scalatest._

class BlacklistedTradingTestSuite extends MatcherSuiteBase with GivenWhenThen {

  import BlacklistedTradingTestSuite._
  override protected def nodeConfigs: Seq[Config] = Configs.map(configWithBlacklisted().withFallback(_))

  private def matcher = dockerNodes().head
  private def alice   = dockerNodes()(1)
  private def bob     = dockerNodes()(2)

  Seq(IssueUsdTx, IssueWctTx, IssueEthTx, IssueBtcTx).map(createSignedIssueRequest).map(matcher.signedIssue).foreach { tx =>
    matcher.waitForTransaction(tx.id)
  }

  val (dec2, dec8) = (1000L, 1000000000L)

  "When blacklists are empty and some orders was placed" - {
    val usdOrder = matcher.placeOrder(alice.privateKey, neelUsdPair, BUY, dec8, dec2, matcherFee).message.id
    val wctOrder = matcher.placeOrder(alice.privateKey, wctNeelPair, BUY, dec2, dec8, matcherFee).message.id
    val ethOrder = matcher.placeOrder(alice.privateKey, ethNeelPair, SELL, dec8, dec8, matcherFee).message.id
    val btcOrder = matcher.placeOrder(bob.privateKey, neelBtcPair, SELL, dec8, dec8, matcherFee).message.id

    matcher.waitOrderStatus(wctNeelPair, btcOrder, "Accepted")

    "If some assets and addresses are blacklisted" in {
      docker.restartNode(
        matcher,
        configWithBlacklisted(
          assets = Array(WctId.toString),
          names = Array("ETH.*"),
          addresses = Array(bob.address)
        )
      )

      Then("orders for blacklisted assets are not available and new orders can't be placed")
      matcher.orderStatusExpectInvalidAssetId(wctOrder, wctNeelPair, WctId.toString)
      matcher.orderStatusExpectInvalidAssetId(ethOrder, ethNeelPair, EthId.toString)
      matcher.expectRejectedOrderPlacement(alice.privateKey, wctNeelPair, BUY, dec2, dec8)
      matcher.expectRejectedOrderPlacement(alice.privateKey, ethNeelPair, SELL, dec8, dec8)
      matcher.expectRejectedOrderPlacement(bob.privateKey, neelBtcPair, SELL, dec8, dec8)

      And("orders of blacklisted address are still available")
      matcher.orderStatus(btcOrder, neelBtcPair).status shouldBe "Accepted"

      And("orders for other assets are still available")
      matcher.orderStatus(usdOrder, neelUsdPair).status shouldBe "Accepted"

      And("OrderBook for blacklisted assets is not available")
      matcher.orderBookExpectInvalidAssetId(wctNeelPair, WctId.toString)
      matcher.orderBookExpectInvalidAssetId(ethNeelPair, EthId.toString)
      matcher.orderBook(neelBtcPair).asks.size shouldBe 1

      And("OrderHistory returns info about all orders")
      matcher.activeOrderHistory(alice.privateKey).size shouldBe 3
      matcher.activeOrderHistory(alice.privateKey).foreach(_.status shouldBe "Accepted")
      matcher.activeOrderHistory(bob.privateKey).size shouldBe 1
      matcher.activeOrderHistory(bob.privateKey).head.status shouldBe "Accepted"

      And("Trading markets have info about all asset pairs")
      matcher.tradingMarkets().markets.size shouldBe 4

      And("balances are still reserved")
      matcher.reservedBalance(alice.privateKey).size shouldBe 3
      matcher.reservedBalance(bob.privateKey).size shouldBe 1

      And("orders for other assets are still available")
      matcher.orderStatus(usdOrder, neelUsdPair).status shouldBe "Accepted"
    }

    "And now if all blacklists are cleared" in {
      docker.restartNode(matcher, configWithBlacklisted())

      Then("OrderBook for blacklisted assets is available again")
      matcher.orderBook(wctNeelPair).bids.size shouldBe 1
      matcher.orderBook(ethNeelPair).asks.size shouldBe 1

      And("order statuses are available again")
      matcher.orderStatus(wctOrder, wctNeelPair).status shouldBe "Accepted"
      matcher.orderStatus(ethOrder, ethNeelPair).status shouldBe "Accepted"

      And("new orders can be placed")
      val newWctOrder = matcher.placeOrder(alice.privateKey, wctNeelPair, BUY, dec2, dec8, matcherFee).message.id
      val newEthOrder = matcher.placeOrder(alice.privateKey, ethNeelPair, SELL, dec8, dec8, matcherFee).message.id
      val newBtcOrder = matcher.placeOrder(bob.privateKey, neelBtcPair, SELL, dec8, dec8, matcherFee).message.id
      matcher.waitOrderStatus(wctNeelPair, newBtcOrder, "Accepted")
      matcher.orderStatus(newWctOrder, wctNeelPair).status shouldBe "Accepted"
      matcher.orderStatus(newEthOrder, ethNeelPair).status shouldBe "Accepted"
    }

  }

}

object BlacklistedTradingTestSuite {

  def configWithBlacklisted(assets: Array[String] = Array(), names: Array[String] = Array(), addresses: Array[String] = Array()): Config = {
    def toStr(array: Array[String]): String = if (array.length == 0) "" else array.mkString("\"", "\", \"", "\"")
    parseString(s"""
                |neel.matcher {
                |  blacklisted-assets = [${toStr(assets)}]
                |  blacklisted-names = [${toStr(names)}]
                |  blacklisted-addresses = [${toStr(addresses)}]
                |}
    """.stripMargin)
  }

}
