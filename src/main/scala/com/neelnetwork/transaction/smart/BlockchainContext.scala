package com.neelnetwork.transaction.smart

import cats.kernel.Monoid
import com.neelnetwork.lang.{Global, ScriptVersion}
import com.neelnetwork.lang.v1.evaluator.ctx.EvaluationContext
import com.neelnetwork.lang.v1.evaluator.ctx.impl.neel.NeelContext
import com.neelnetwork.lang.v1.evaluator.ctx.impl.{CryptoContext, PureContext}
import com.neelnetwork.state._
import com.neelnetwork.transaction._
import com.neelnetwork.transaction.assets.exchange.Order
import monix.eval.Coeval
import shapeless._

object BlockchainContext {

  type In = Transaction :+: Order :+: CNil
  def build(version: ScriptVersion,
            nByte: Byte,
            in: Coeval[In],
            h: Coeval[Int],
            blockchain: Blockchain,
            isTokenContext: Boolean): EvaluationContext = {
    Monoid
      .combineAll(
        Seq(
          PureContext.build(version),
          CryptoContext.build(Global),
          NeelContext.build(version, new NeelEnvironment(nByte, in, h, blockchain), isTokenContext)
        ))
      .evaluationContext
  }
}
