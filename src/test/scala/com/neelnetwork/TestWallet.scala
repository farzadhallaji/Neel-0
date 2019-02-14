package com.neelnetwork

import com.neelnetwork.settings.WalletSettings
import com.neelnetwork.wallet.Wallet

trait TestWallet {
  protected val testWallet: Wallet = {
    val wallet = Wallet(WalletSettings(None, Some("123"), None))
    wallet.generateNewAccounts(10)
    wallet
  }
}
