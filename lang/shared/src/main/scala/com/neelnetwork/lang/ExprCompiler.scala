package com.neelnetwork.lang

import com.neelnetwork.lang.directives.Directive

trait ExprCompiler extends Versioned {
  def compile(input: String, directives: List[Directive]): Either[String, version.ExprT]
}
