package com.neelnetwork.it.sync.matcher.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.{empty, parseString}
import com.neelnetwork.account.{AddressScheme, PrivateKeyAccount}
import com.neelnetwork.it.NodeConfigs.Default
import com.neelnetwork.it.sync.CustomFeeTransactionSuite.defaultAssetQuantity
import com.neelnetwork.it.sync.matcher.config.MatcherDefaultConfig._
import com.neelnetwork.it.util._
import com.neelnetwork.matcher.AssetPairBuilder
import com.neelnetwork.transaction.assets.IssueTransactionV2
import com.neelnetwork.transaction.assets.exchange.AssetPair
import scala.util.Random

// TODO: Make it trait
object MatcherPriceAssetConfig {

  private val _Configs: Seq[Config] = (Default.last +: Random.shuffle(Default.init).take(2))
    .zip(Seq(matcherConfig.withFallback(minerDisabled), minerDisabled, empty()))
    .map { case (n, o) => o.withFallback(n) }

  private val aliceSeed = _Configs(1).getString("account-seed")
  private val bobSeed   = _Configs(2).getString("account-seed")
  private val alicePk   = PrivateKeyAccount.fromSeed(aliceSeed).right.get
  private val bobPk     = PrivateKeyAccount.fromSeed(bobSeed).right.get

  val Decimals: Byte = 2

  val usdAssetName = "USD-X"
  val wctAssetName = "WCT-X"
  val ethAssetName = "ETH-X"
  val btcAssetName = "BTC-X"

  val IssueUsdTx: IssueTransactionV2 = IssueTransactionV2
    .selfSigned(
      2,
      AddressScheme.current.chainId,
      sender = alicePk,
      name = usdAssetName.getBytes(),
      description = "asset description".getBytes(),
      quantity = defaultAssetQuantity,
      decimals = Decimals,
      reissuable = false,
      script = None,
      fee = 1.neel,
      timestamp = System.currentTimeMillis()
    )
    .right
    .get

  val IssueWctTx: IssueTransactionV2 = IssueTransactionV2
    .selfSigned(
      2,
      AddressScheme.current.chainId,
      sender = bobPk,
      name = wctAssetName.getBytes(),
      description = "asset description".getBytes(),
      quantity = defaultAssetQuantity,
      decimals = Decimals,
      reissuable = false,
      script = None,
      fee = 1.neel,
      timestamp = System.currentTimeMillis()
    )
    .right
    .get

  val IssueEthTx: IssueTransactionV2 = IssueTransactionV2
    .selfSigned(
      2,
      AddressScheme.current.chainId,
      sender = alicePk,
      name = ethAssetName.getBytes(),
      description = "asset description".getBytes(),
      quantity = defaultAssetQuantity,
      decimals = 8,
      reissuable = false,
      script = None,
      fee = 1.neel,
      timestamp = System.currentTimeMillis()
    )
    .right
    .get

  val IssueBtcTx: IssueTransactionV2 = IssueTransactionV2
    .selfSigned(
      2,
      AddressScheme.current.chainId,
      sender = bobPk,
      name = btcAssetName.getBytes(),
      description = "asset description".getBytes(),
      quantity = defaultAssetQuantity,
      decimals = 8,
      reissuable = false,
      script = None,
      fee = 1.neel,
      timestamp = System.currentTimeMillis()
    )
    .right
    .get

  val BtcId = IssueBtcTx.id()
  val EthId = IssueEthTx.id()
  val UsdId = IssueUsdTx.id()
  val WctId = IssueWctTx.id()

  val wctUsdPair = AssetPair(
    amountAsset = Some(WctId),
    priceAsset = Some(UsdId)
  )

  val wctNeelPair = AssetPair(
    amountAsset = Some(WctId),
    priceAsset = None
  )

  val ethNeelPair = AssetPair(
    amountAsset = Some(EthId),
    priceAsset = None
  )

  val ethBtcPair = AssetPair(
    amountAsset = Some(EthId),
    priceAsset = Some(BtcId)
  )

  val neelUsdPair = AssetPair(
    amountAsset = None,
    priceAsset = Some(UsdId)
  )

  val ethUsdPair = AssetPair(
    amountAsset = Some(EthId),
    priceAsset = Some(UsdId)
  )

  val neelBtcPair = AssetPair(
    amountAsset = None,
    priceAsset = Some(BtcId)
  )

  val orderLimit = 10

  private val updatedMatcherConfig = parseString(s"""neel.matcher {
                                                    |  price-assets = [ "$UsdId", "$BtcId", "NEEL" ]
                                                    |  rest-order-limit = $orderLimit
                                                    |}""".stripMargin)

  val Configs: Seq[Config] = _Configs.map(updatedMatcherConfig.withFallback(_))

  def createAssetPair(asset1: String, asset2: String): AssetPair = {
    val (a1, a2) = (AssetPair.extractAssetId(asset1).get, AssetPair.extractAssetId(asset2).get)
    if (AssetPairBuilder.assetIdOrdering.compare(a1, a2) > 0)
      AssetPair(a1, a2)
    else
      AssetPair(a2, a1)
  }

}
