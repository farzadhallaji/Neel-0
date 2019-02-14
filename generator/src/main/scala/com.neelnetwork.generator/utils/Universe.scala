package com.neelnetwork.generator.utils

import com.neelnetwork.account.PrivateKeyAccount
import com.neelnetwork.state.ByteStr

object Universe {
  var AccountsWithBalances: List[(PrivateKeyAccount, Long)] = Nil
  var IssuedAssets: List[ByteStr]                           = Nil
  var Leases: List[ByteStr]                                 = Nil
}
