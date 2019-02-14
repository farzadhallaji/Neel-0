package com.neelnetwork.state.diffs.smart.scenarios

import com.neelnetwork.lang.v1.compiler.CompilerV1
import com.neelnetwork.lang.v1.parser.Parser
import com.neelnetwork.state._
import com.neelnetwork.state.diffs._
import com.neelnetwork.state.diffs.smart._
import com.neelnetwork.utils.compilerContext
import com.neelnetwork.{NoShrink, TransactionGen}
import org.scalatest.prop.PropertyChecks
import org.scalatest.{Matchers, PropSpec}
import com.neelnetwork.lagonaki.mocks.TestBlock
import com.neelnetwork.lang.ScriptVersion.Versions.V1

class OnlyTransferIsAllowedTest extends PropSpec with PropertyChecks with Matchers with TransactionGen with NoShrink {

  property("transfer is allowed but lease is not due to predicate") {

    val scriptText =
      s"""
         |
         | match tx {
         |  case ttx: TransferTransaction | MassTransferTransaction =>
         |     sigVerify(ttx.bodyBytes,ttx.proofs[0],ttx.senderPublicKey)
         |  case other =>
         |     false
         | }
      """.stripMargin
    val untyped         = Parser(scriptText).get.value
    val transferAllowed = CompilerV1(compilerContext(V1, isAssetScript = false), untyped).explicitGet()._1

    forAll(preconditionsTransferAndLease(transferAllowed)) {
      case (genesis, script, lease, transfer) =>
        assertDiffAndState(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(transfer)), smartEnabledFS) { case _ => () }
        assertDiffEi(Seq(TestBlock.create(Seq(genesis, script))), TestBlock.create(Seq(lease)), smartEnabledFS)(totalDiffEi =>
          totalDiffEi should produce("TransactionNotAllowedByScript"))
    }
  }

}
