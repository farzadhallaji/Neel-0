package com.neelnetwork.state.diffs

import com.neelnetwork.features.BlockchainFeatures
import com.neelnetwork.features.FeatureProvider._
import com.neelnetwork.state.{Blockchain, Diff, LeaseBalance, Portfolio}
import com.neelnetwork.transaction.ValidationError
import com.neelnetwork.transaction.smart.SetScriptTransaction

import com.neelnetwork.transaction.ValidationError
import com.neelnetwork.transaction.ValidationError.GenericError
import com.neelnetwork.lang.v1.DenyDuplicateVarNames
import com.neelnetwork.utils.varNames

import scala.util.Right

object SetScriptTransactionDiff {
  def apply(blockchain: Blockchain, height: Int)(tx: SetScriptTransaction): Either[ValidationError, Diff] = {
    val scriptOpt = tx.script
    for {
      _ <- scriptOpt.fold(Right(()): Either[ValidationError, Unit]) { script =>
        if (blockchain.isFeatureActivated(BlockchainFeatures.SmartAccountTrading, height)) {
          Right(())
        } else {
          val version = script.version
          DenyDuplicateVarNames(version, varNames(version), script.expr).left.map(GenericError.apply)
        }
      }
    } yield {
      Diff(
        height = height,
        tx = tx,
        portfolios = Map(tx.sender.toAddress -> Portfolio(-tx.fee, LeaseBalance.empty, Map.empty)),
        scripts = Map(tx.sender.toAddress    -> scriptOpt)
      )
    }
  }
}
