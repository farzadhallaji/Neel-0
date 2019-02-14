package com.neelnetwork.generator

import cats.Show
import com.neelnetwork.account.PrivateKeyAccount
import com.neelnetwork.generator.OracleTransactionGenerator.Settings
import com.neelnetwork.generator.utils.Gen
import com.neelnetwork.it.util._
import com.neelnetwork.state._
import com.neelnetwork.transaction.smart.SetScriptTransaction
import com.neelnetwork.transaction.transfer.{TransferTransactionV2}
import com.neelnetwork.transaction.{DataTransaction, Transaction}

class OracleTransactionGenerator(settings: Settings, val accounts: Seq[PrivateKeyAccount]) extends TransactionGenerator {
  override def next(): Iterator[Transaction] = generate(settings).toIterator

  def generate(settings: Settings): Seq[Transaction] = {
    val oracle = accounts.last

    val scriptedAccount = accounts.head

    val script = Gen.oracleScript(oracle, settings.requiredData)

    val enoughFee = 0.005.neel

    val setScript: Transaction =
      SetScriptTransaction
        .selfSigned(1, scriptedAccount, Some(script), enoughFee, System.currentTimeMillis())
        .explicitGet()

    val setDataTx: Transaction = DataTransaction
      .selfSigned(1, oracle, settings.requiredData.toList, enoughFee, System.currentTimeMillis())
      .explicitGet()

    val transactions: List[Transaction] =
      List
        .fill(settings.transactions) {
          TransferTransactionV2
            .selfSigned(2, None, scriptedAccount, oracle, 1.neel, System.currentTimeMillis(), None, enoughFee, Array.emptyByteArray)
            .explicitGet()
        }

    setScript +: setDataTx +: transactions
  }
}

object OracleTransactionGenerator {
  final case class Settings(transactions: Int, requiredData: Set[DataEntry[_]])

  object Settings {
    implicit val toPrintable: Show[Settings] = { x =>
      s"Transactions: ${x.transactions}\n" +
        s"DataEntries: ${x.requiredData}\n"
    }
  }
}
