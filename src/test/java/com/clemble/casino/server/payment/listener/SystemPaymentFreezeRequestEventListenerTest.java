package com.clemble.casino.server.payment.listener;

import com.clemble.casino.server.event.payment.SystemPaymentFreezeRequestEvent;
import com.clemble.casino.server.payment.spring.PaymentSpringConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by mavarazy on 5/11/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { PaymentSpringConfiguration.class })
public class SystemPaymentFreezeRequestEventListenerTest {

    @Autowired
    public SystemPaymentFreezeRequestEventListener listener;

    @Test(expected = Exception.class)
    public void testValidationWithNull() {
        // Step 1. Generating illegal request
        SystemPaymentFreezeRequestEvent illegalFreeze = new SystemPaymentFreezeRequestEvent(null, null);
        // Step 2. Checking validation works for illegal requests
        listener.onEvent(illegalFreeze);
    }

}
