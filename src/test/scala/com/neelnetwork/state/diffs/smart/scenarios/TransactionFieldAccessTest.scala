package com.neelnetwork.state.diffs.smart.scenarios

import com.neelnetwork.lang.v1.compiler.CompilerV1
import com.neelnetwork.lang.v1.parser.Parser
import com.neelnetwork.state.diffs.smart._
import com.neelnetwork.state._
import com.neelnetwork.state.diffs.{assertDiffAndState, assertDiffEi, produce}
import com.neelnetwork.utils.compilerContext
import com.neelnetwork.{NoShrink, TransactionGen}
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}
import com.neelnetwork.lagonaki.mocks.TestBlock
import com.neelnetwork.lang.ScriptVersion.Versions.V1
import com.neelnetwork.transaction.GenesisTransaction
import com.neelnetwork.transaction.lease.LeaseTransaction
import com.neelnetwork.transaction.smart.SetScriptTransaction
import com.neelnetwork.transaction.transfer._

class TransactionFieldAccessTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {

  private def preconditionsTransferAndLease(
      code: String): Gen[(GenesisTransaction, SetScriptTransaction, LeaseTransaction, TransferTransactionV2)] = {
    val untyped = Parser(code).get.value
    val typed   = CompilerV1(compilerContext(V1, isAssetScript = false), untyped).explicitGet()._1
    preconditionsTransferAndLease(typed)
  }

  private val script =
    """
      |
      | match tx {
      | case ttx: TransferTransaction =>
      |       isDefined(ttx.assetId)==false
      |   case other =>
      |       false
      | }
      """.stripMargin

  property("accessing field of transaction without checking its type first results on exception") {
    forAll(preconditionsTransferAndLease(script)) {
      case ((genesis, script, lease, transfer)) =>
        assertDiffAndState(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(transfer)), smartEnabledFS) { case _ => () }
        assertDiffEi(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(lease)), smartEnabledFS)(totalDiffEi =>
          totalDiffEi should produce("TransactionNotAllowedByScript"))
    }
  }
}
