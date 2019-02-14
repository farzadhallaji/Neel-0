package com.neelnetwork.matcher.smart

import cats.implicits._
import com.neelnetwork.account.AddressScheme
import com.neelnetwork.lang.ExprEvaluator.Log
import com.neelnetwork.lang.v1.compiler.Terms.EVALUATED
import com.neelnetwork.lang.v1.evaluator.EvaluatorV1
import com.neelnetwork.transaction.assets.exchange.Order
import com.neelnetwork.transaction.smart.script.Script
import monix.eval.Coeval

object MatcherScriptRunner {

  def apply[A <: EVALUATED](script: Script, order: Order, isTokenScript: Boolean): (Log, Either[String, A]) = script match {
    case Script.Expr(expr) =>
      val ctx = MatcherContext.build(script.version, AddressScheme.current.chainId, Coeval.evalOnce(order), !isTokenScript)
      EvaluatorV1.applywithLogging[A](ctx, expr)
    case _ => (List.empty, "Unsupported script version".asLeft[A])
  }
}
