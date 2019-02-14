package com.neelnetwork.it

import com.neelnetwork.account.AddressScheme

trait IntegrationTestsScheme {
  AddressScheme.current = new AddressScheme {
    override val chainId: Byte = 'I'
  }
}
