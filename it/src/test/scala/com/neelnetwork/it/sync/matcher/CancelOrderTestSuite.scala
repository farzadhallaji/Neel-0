package com.neelnetwork.it.sync.matcher

import com.typesafe.config.Config
import com.neelnetwork.it.api.SyncHttpApi._
import com.neelnetwork.it.api.SyncMatcherHttpApi._
import com.neelnetwork.it.matcher.MatcherSuiteBase
import com.neelnetwork.it.sync._
import com.neelnetwork.it.sync.matcher.config.MatcherPriceAssetConfig._
import com.neelnetwork.it.util._
import com.neelnetwork.transaction.assets.exchange.{AssetPair, OrderType}

import scala.concurrent.duration._

class CancelOrderTestSuite extends MatcherSuiteBase {

  override protected def nodeConfigs: Seq[Config] = Configs

  private val neelBtcPair = AssetPair(None, Some(BtcId))

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    Seq(IssueUsdTx, IssueBtcTx).map(createSignedIssueRequest).map(matcherNode.signedIssue).foreach { tx =>
      matcherNode.waitForTransaction(tx.id)
    }

    Seq(
      aliceNode.transfer(IssueUsdTx.sender.toAddress.stringRepr, aliceAcc.address, defaultAssetQuantity, 100000, Some(UsdId.toString), None, 2),
      bobNode.transfer(IssueBtcTx.sender.toAddress.stringRepr, bobAcc.address, defaultAssetQuantity, 100000, Some(BtcId.toString), None, 2)
    ).foreach { tx =>
      matcherNode.waitForTransaction(tx.id)
    }
  }

  "Order can be canceled" - {
    "by sender" in {
      val orderId = matcherNode.placeOrder(bobAcc, neelUsdPair, OrderType.SELL, 100.neel, 800, matcherFee).message.id
      matcherNode.waitOrderStatus(neelUsdPair, orderId, "Accepted", 1.minute)

      matcherNode.cancelOrder(bobAcc, neelUsdPair, orderId)
      matcherNode.waitOrderStatus(neelUsdPair, orderId, "Cancelled", 1.minute)

      matcherNode.orderHistoryByPair(bobAcc, neelUsdPair).collectFirst {
        case o if o.id == orderId => o.status shouldEqual "Cancelled"
      }
    }
    "with API key" in {
      val orderId = matcherNode.placeOrder(bobAcc, neelUsdPair, OrderType.SELL, 100.neel, 800, matcherFee).message.id
      matcherNode.waitOrderStatus(neelUsdPair, orderId, "Accepted", 1.minute)

      matcherNode.cancelOrderWithApiKey(orderId)
      matcherNode.waitOrderStatus(neelUsdPair, orderId, "Cancelled", 1.minute)

      matcherNode.fullOrderHistory(bobAcc).filter(_.id == orderId).head.status shouldBe "Cancelled"
      matcherNode.orderHistoryByPair(bobAcc, neelUsdPair).filter(_.id == orderId).head.status shouldBe "Cancelled"

      val orderBook = matcherNode.orderBook(neelUsdPair)
      orderBook.bids shouldBe empty
      orderBook.asks shouldBe empty
    }
  }

  "Cancel is rejected" - {
    "when request sender is not the sender of and order" in {
      val orderId = matcherNode.placeOrder(bobAcc, neelUsdPair, OrderType.SELL, 100.neel, 800, matcherFee).message.id
      matcherNode.waitOrderStatus(neelUsdPair, orderId, "Accepted", 1.minute)

      matcherNode.expectCancelRejected(matcherNode.privateKey, neelUsdPair, orderId)

      // Cleanup
      matcherNode.cancelOrder(bobAcc, neelUsdPair, orderId)
      matcherNode.waitOrderStatus(neelUsdPair, orderId, "Cancelled")
    }
  }

  "Batch cancel" - {
    "works for" - {
      "all orders placed by an address" in {
        matcherNode.fullOrderHistory(bobAcc)

        val usdOrderIds = 1 to 5 map { i =>
          matcherNode.placeOrder(bobAcc, neelUsdPair, OrderType.SELL, 100.neel + i, 400, matcherFee).message.id
        }

        matcherNode.assetBalance(bobAcc.toAddress.stringRepr, BtcId.base58)

        val btcOrderIds = 1 to 5 map { i =>
          matcherNode.placeOrder(bobAcc, neelBtcPair, OrderType.BUY, 100.neel + i, 400, matcherFee).message.id
        }

        (usdOrderIds ++ btcOrderIds).foreach(id => matcherNode.waitOrderStatus(neelUsdPair, id, "Accepted"))

        matcherNode.cancelAllOrders(bobAcc)

        (usdOrderIds ++ btcOrderIds).foreach(id => matcherNode.waitOrderStatus(neelUsdPair, id, "Cancelled"))
      }

      "a pair" in {
        val usdOrderIds = 1 to 5 map { i =>
          matcherNode.placeOrder(bobAcc, neelUsdPair, OrderType.SELL, 100.neel + i, 400, matcherFee).message.id
        }

        val btcOrderIds = 1 to 5 map { i =>
          matcherNode.placeOrder(bobAcc, neelBtcPair, OrderType.BUY, 100.neel + i, 400, matcherFee).message.id
        }

        (usdOrderIds ++ btcOrderIds).foreach(id => matcherNode.waitOrderStatus(neelUsdPair, id, "Accepted"))

        matcherNode.cancelOrdersForPair(bobAcc, neelBtcPair)

        btcOrderIds.foreach(id => matcherNode.waitOrderStatus(neelUsdPair, id, "Cancelled"))
        usdOrderIds.foreach(id => matcherNode.waitOrderStatus(neelUsdPair, id, "Accepted"))
      }
    }
  }
}
