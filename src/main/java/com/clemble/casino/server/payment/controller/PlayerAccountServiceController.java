package com.clemble.casino.server.payment.controller;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.List;

import com.clemble.casino.payment.service.PlayerAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.clemble.casino.payment.PlayerAccount;
import com.clemble.casino.money.Currency;
import com.clemble.casino.money.Money;
import com.clemble.casino.server.ExternalController;
import com.clemble.casino.server.payment.account.ServerPlayerAccountService;
import com.clemble.casino.server.payment.repository.ServerAccountService;
import com.clemble.casino.WebMapping;
import static com.clemble.casino.payment.PaymentWebMapping.*;

@RestController
public class PlayerAccountServiceController implements PlayerAccountService, ExternalController {

    final private ServerPlayerAccountService playerAccountService;
    final private ServerAccountService accountTemplate;

    public PlayerAccountServiceController(ServerPlayerAccountService playerAccountService,
                                          ServerAccountService playerAccountRepository) {
        this.playerAccountService = checkNotNull(playerAccountService);
        this.accountTemplate = checkNotNull(playerAccountRepository);
    }

    @Override
    public PlayerAccount myAccount() {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(method = RequestMethod.GET, value = MY_ACCOUNT, produces = WebMapping.PRODUCES)
    @ResponseStatus(value = HttpStatus.OK)
    public PlayerAccount myAccount(@CookieValue("player") String playerId) {
        // Step 1. Returning account from repository
        return accountTemplate.findOne(playerId);
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, value = ACCOUNT, produces = WebMapping.PRODUCES)
    @ResponseStatus(value = HttpStatus.OK)
    public PlayerAccount getAccount(@PathVariable("player") String playerId) {
        // Step 1. Returning account from repository
        return accountTemplate.findOne(playerId);
    }

    @Override
    @RequestMapping(method = RequestMethod.GET, value = ACCOUNTS, produces = WebMapping.PRODUCES)
    @ResponseStatus(value = HttpStatus.OK)
    public List<String> canAfford(@RequestParam("player") Collection<String> players, @RequestParam("currency") Currency currency, @RequestParam("amount") Long amount) {
        return playerAccountService.canAfford(players, Money.create(currency, amount));
    }

}
