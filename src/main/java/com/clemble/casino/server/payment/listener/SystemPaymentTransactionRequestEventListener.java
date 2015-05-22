package com.clemble.casino.server.payment.listener;

import com.clemble.casino.error.ClembleErrorCode;
import com.clemble.casino.error.ClembleException;
import com.clemble.casino.error.ClembleValidationService;
import com.clemble.casino.payment.PaymentTransaction;
import com.clemble.casino.server.event.payment.SystemPaymentTransactionRequestEvent;
import com.clemble.casino.server.player.notification.SystemEventListener;
import com.clemble.casino.server.payment.repository.ServerAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by mavarazy on 7/5/14.
 */
public class SystemPaymentTransactionRequestEventListener implements SystemEventListener<SystemPaymentTransactionRequestEvent>{

    final private Logger LOG = LoggerFactory.getLogger(SystemPaymentTransactionRequestEventListener.class);

    final private ServerAccountService accountTemplate;
    final private ClembleValidationService validationService;

    public SystemPaymentTransactionRequestEventListener(
        ServerAccountService accountTemplate,
        ClembleValidationService validationService) {
        this.accountTemplate = checkNotNull(accountTemplate);
        this.validationService = validationService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEvent(SystemPaymentTransactionRequestEvent event) {
        PaymentTransaction paymentTransaction = event.getTransaction();
        LOG.debug("{} start", paymentTransaction.getTransactionKey());
        // Step 1. Sanity check
        if (paymentTransaction == null)
            throw ClembleException.withServerError(ClembleErrorCode.PaymentTransactionEmpty);
        validationService.validate(paymentTransaction);
        // Step 2. Processing payment transactions
        accountTemplate.process(paymentTransaction);
        LOG.debug("{} finish", paymentTransaction.getTransactionKey());
    }

    @Override
    public String getChannel() {
        return SystemPaymentTransactionRequestEvent.CHANNEL;
    }

    @Override
    public String getQueueName() {
        return SystemPaymentTransactionRequestEvent.CHANNEL + " > payment";
    }
}
