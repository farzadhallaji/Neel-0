package com.neelnetwork.lang

import com.neelnetwork.lang.v1.BaseGlobal

package object hacks {
  private[lang] val Global: BaseGlobal = com.neelnetwork.lang.Global // Hack for IDEA
}
