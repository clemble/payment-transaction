package com.clemble.casino.server.payment.repository;

import com.clemble.casino.payment.PaymentTransaction;
import com.clemble.casino.payment.PendingTransaction;
import com.clemble.casino.payment.PlayerAccount;
import com.clemble.casino.server.ServerService;

public interface ServerAccountService extends ServerService {

    public PlayerAccount findOne(String player);

    public PaymentTransaction process(PaymentTransaction transaction);

    public PendingTransaction freeze(PendingTransaction transaction);

}
