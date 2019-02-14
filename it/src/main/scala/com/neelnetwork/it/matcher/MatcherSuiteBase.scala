package com.neelnetwork.it.matcher

import com.typesafe.config.Config
import com.neelnetwork.it._
import com.neelnetwork.it.transactions.NodesFromDocker
import org.scalatest._
import com.neelnetwork.it.util._
import scala.concurrent.ExecutionContext

abstract class MatcherSuiteBase
    extends FreeSpec
    with Matchers
    with CancelAfterFailure
    with ReportingTestName
    with NodesFromDocker
    with BeforeAndAfterAll
    with MatcherNode {

  protected implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  val defaultAssetQuantity = 999999999999L

  val smartFee         = 0.004.neel
  val minFee           = 0.001.neel + smartFee
  val issueFee         = 1.neel + smartFee
  val leasingFee       = 0.002.neel + smartFee
  val tradeFee         = 0.003.neel
  val smartTradeFee    = tradeFee + smartFee
  val twoSmartTradeFee = tradeFee + 2 * smartFee

  protected def nodeConfigs: Seq[Config] =
    NodeConfigs.newBuilder
      .withDefault(4)
      .buildNonConflicting()

}
