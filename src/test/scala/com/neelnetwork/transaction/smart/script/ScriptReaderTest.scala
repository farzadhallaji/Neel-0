package com.neelnetwork.transaction.smart.script

import com.neelnetwork.crypto
import com.neelnetwork.lang.ScriptVersion.Versions.V1
import com.neelnetwork.lang.v1.Serde
import com.neelnetwork.lang.v1.compiler.Terms.TRUE
import com.neelnetwork.state.diffs.produce
import org.scalatest.{FreeSpec, Matchers}

class ScriptReaderTest extends FreeSpec with Matchers {
  val checksumLength = 4

  "should parse all bytes for V1" in {
    val body     = Array(V1.value.toByte) ++ Serde.serialize(TRUE) ++ "foo".getBytes
    val allBytes = body ++ crypto.secureHash(body).take(checksumLength)
    ScriptReader.fromBytes(allBytes) should produce("bytes left")
  }
}
