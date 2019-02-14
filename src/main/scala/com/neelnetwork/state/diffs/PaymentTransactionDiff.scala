package com.neelnetwork.state.diffs

import cats.implicits._
import com.neelnetwork.settings.FunctionalitySettings
import com.neelnetwork.state.{Blockchain, Diff, LeaseBalance, Portfolio}
import com.neelnetwork.account.Address
import com.neelnetwork.transaction.ValidationError.GenericError
import com.neelnetwork.transaction.{PaymentTransaction, ValidationError}

import scala.util.{Left, Right}

object PaymentTransactionDiff {

  def apply(blockchain: Blockchain, height: Int, settings: FunctionalitySettings, blockTime: Long)(
      tx: PaymentTransaction): Either[ValidationError, Diff] = {

    if (height > settings.blockVersion3AfterHeight) {
      Left(GenericError(s"Payment transaction is deprecated after h=${settings.blockVersion3AfterHeight}"))
    } else {
      Right(
        Diff(
          height = height,
          tx = tx,
          portfolios = Map(tx.recipient -> Portfolio(balance = tx.amount, LeaseBalance.empty, assets = Map.empty)) combine Map(
            Address.fromPublicKey(tx.sender.publicKey) -> Portfolio(
              balance = -tx.amount - tx.fee,
              LeaseBalance.empty,
              assets = Map.empty
            )),
        ))
    }
  }
}
