package com.clemble.casino.server.payment.account;

import java.util.Collection;
import java.util.List;

import com.clemble.casino.money.Money;
import com.clemble.casino.server.ServerService;

public interface ServerPlayerAccountService extends ServerService {

    boolean canAfford(String player, Money amount);

    List<String> canAfford(Collection<String> players, Money amount);

}
