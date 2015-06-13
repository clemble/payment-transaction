package com.clemble.casino.server.payment.repository;

import com.clemble.casino.payment.PaymentTransaction;
import com.clemble.casino.payment.PendingTransaction;
import com.clemble.casino.payment.PlayerAccount;
import com.clemble.casino.server.ServerService;

public interface ServerAccountService extends ServerService {

    PlayerAccount findOne(String player);

    PaymentTransaction process(PaymentTransaction transaction);

    PendingTransaction freeze(PendingTransaction transaction);

}
