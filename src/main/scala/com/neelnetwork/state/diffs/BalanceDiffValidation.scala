package com.neelnetwork.state.diffs

import cats.implicits._
import com.neelnetwork.account.Address
import com.neelnetwork.metrics.Instrumented
import com.neelnetwork.settings.FunctionalitySettings
import com.neelnetwork.state.{Blockchain, ByteStr, Diff, LeaseBalance, Portfolio}
import com.neelnetwork.transaction.ValidationError.AccountBalanceError
import com.neelnetwork.utils.ScorexLogging

import scala.util.{Left, Right}

object BalanceDiffValidation extends ScorexLogging with Instrumented {

  def apply(b: Blockchain, currentHeight: Int, fs: FunctionalitySettings)(d: Diff): Either[AccountBalanceError, Diff] = {
    val changedAccounts = d.portfolios.keySet

    val positiveBalanceErrors: Map[Address, String] = changedAccounts
      .flatMap(acc => {
        val portfolioDiff = d.portfolios(acc)
        val oldPortfolio  = b.portfolio(acc)

        val newPortfolio = oldPortfolio.combine(portfolioDiff)

        lazy val negativeBalance          = newPortfolio.balance < 0
        lazy val negativeAssetBalance     = newPortfolio.assets.values.exists(_ < 0)
        lazy val negativeEffectiveBalance = newPortfolio.effectiveBalance < 0
        lazy val leasedMoreThanOwn        = newPortfolio.balance < newPortfolio.lease.out && currentHeight > fs.allowLeasedBalanceTransferUntilHeight

        val err = if (negativeBalance) {
          Some(s"negative neel balance: $acc, old: ${oldPortfolio.balance}, new: ${newPortfolio.balance}")
        } else if (negativeAssetBalance) {
          Some(s"negative asset balance: $acc, new portfolio: ${negativeAssetsInfo(newPortfolio)}")
        } else if (negativeEffectiveBalance) {
          Some(s"negative effective balance: $acc, old: ${leaseNeelInfo(oldPortfolio)}, new: ${leaseNeelInfo(newPortfolio)}")
        } else if (leasedMoreThanOwn && oldPortfolio.lease.out == newPortfolio.lease.out) {
          Some(s"$acc trying to spend leased money")
        } else if (leasedMoreThanOwn) {
          Some(s"leased being more than own: $acc, old: ${leaseNeelInfo(oldPortfolio)}, new: ${leaseNeelInfo(newPortfolio)}")
        } else None
        err.map(acc -> _)
      })
      .toMap

    if (positiveBalanceErrors.isEmpty) {
      Right(d)
    } else {
      Left(AccountBalanceError(positiveBalanceErrors))
    }
  }

  private def leaseNeelInfo(p: Portfolio): (Long, LeaseBalance) = (p.balance, p.lease)

  private def negativeAssetsInfo(p: Portfolio): Map[ByteStr, Long] = p.assets.filter(_._2 < 0)
}
