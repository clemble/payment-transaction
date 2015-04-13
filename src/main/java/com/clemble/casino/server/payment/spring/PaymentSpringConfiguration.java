package com.clemble.casino.server.payment.spring;

import com.clemble.casino.error.ClembleCasinoValidationService;
import com.clemble.casino.payment.service.PlayerAccountService;
import com.clemble.casino.server.payment.account.BasicServerPlayerAccountService;
import com.clemble.casino.server.payment.listener.SystemPaymentFreezeRequestEventListener;
import com.clemble.casino.server.payment.listener.SystemPaymentTransactionRequestEventListener;
import com.clemble.casino.server.payment.listener.SystemPlayerAccountCreationEventListener;
import com.clemble.casino.server.payment.repository.*;
import com.clemble.casino.server.player.notification.ServerNotificationService;
import com.clemble.casino.server.player.notification.SystemNotificationService;
import com.clemble.casino.server.player.notification.SystemNotificationServiceListener;
import com.clemble.casino.server.spring.common.CommonSpringConfiguration;
import com.clemble.casino.server.spring.common.MongoSpringConfiguration;
import com.clemble.casino.server.spring.common.PaymentClientSpringConfiguration;
import static com.clemble.casino.server.spring.common.PaymentClientSpringConfiguration.Default.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.clemble.casino.server.payment.account.ServerPlayerAccountService;
import com.clemble.casino.server.spring.common.SpringConfiguration;
import com.clemble.casino.server.payment.controller.PaymentTransactionServiceController;
import com.clemble.casino.server.payment.controller.PlayerAccountServiceController;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

import java.util.HashMap;

@Configuration
@Import({ CommonSpringConfiguration.class })
public class PaymentSpringConfiguration implements SpringConfiguration {

    @Bean
    public PaymentTransactionServiceController paymentTransactionController(PaymentTransactionRepository paymentTransactionRepository) {
        return new PaymentTransactionServiceController(paymentTransactionRepository);
    }

    @Bean
    public PlayerAccountServiceController playerAccountController(
        ServerAccountService accountTemplate,
        ServerPlayerAccountService playerAccountService) {
        return new PlayerAccountServiceController(playerAccountService, accountTemplate);
    }

    @Bean
    public PlayerAccountRepository playerAccountRepository(MongoRepositoryFactory repositoryFactory){
        return repositoryFactory.getRepository(PlayerAccountRepository.class);
    }

    @Bean
    public PendingTransactionRepository pendingTransactionRepository(MongoRepositoryFactory repositoryFactory) {
        return repositoryFactory.getRepository(PendingTransactionRepository.class);
    }

    @Bean
    public ServerAccountService playerAccountTemplate(
        PlayerAccountRepository accountRepository,
        PaymentTransactionRepository transactionRepository,
        PendingTransactionRepository pendingTransactionRepository,
        @Qualifier("playerNotificationService") ServerNotificationService notificationService) {
        return new MongoServerAccountService(accountRepository, transactionRepository, pendingTransactionRepository, notificationService);
    }

    @Bean
    public ServerPlayerAccountService realPlayerAccountService(ServerAccountService playerAccountRepository) {
        return new BasicServerPlayerAccountService(playerAccountRepository);
    }

    @Bean
    public SystemPaymentTransactionRequestEventListener paymentTransactionRequestEventListener(
            ServerAccountService accountTemplate,
            SystemNotificationServiceListener notificationServiceListener,
            ClembleCasinoValidationService validationService) {
        SystemPaymentTransactionRequestEventListener eventListener = new SystemPaymentTransactionRequestEventListener(accountTemplate, validationService);
        notificationServiceListener.subscribe(eventListener);
        return eventListener;
    }

    @Bean
    public SystemPaymentFreezeRequestEventListener systemPaymentFreezeRequestEventListener(
        ServerAccountService accountTemplate,
        SystemNotificationServiceListener notificationServiceListener,
        ClembleCasinoValidationService validationService) {
        SystemPaymentFreezeRequestEventListener eventListener = new SystemPaymentFreezeRequestEventListener(accountTemplate, validationService);
        notificationServiceListener.subscribe(eventListener);
        return eventListener;
    }

    @Bean
    public SystemPlayerAccountCreationEventListener systemPlayerAccountCreationEventListener(
            PlayerAccountRepository accountRepository,
            SystemNotificationServiceListener notificationServiceListener) {
        SystemPlayerAccountCreationEventListener eventListener = new SystemPlayerAccountCreationEventListener(accountRepository);
        notificationServiceListener.subscribe(eventListener);
        return eventListener;
    }

    @Configuration
    @Import(MongoSpringConfiguration.class)
    public static class PaymentMongoSpringConfiguration implements SpringConfiguration {

        @Bean
        public PaymentTransactionRepository paymentTransactionRepository(MongoRepositoryFactory mongoRepositoryFactory) {
            return mongoRepositoryFactory.getRepository(PaymentTransactionRepository.class);
        }

    }

    @Bean
    public SimpleMessageListenerContainer accountServiceListener(
        PlayerAccountServiceController playerAccountController,
        @Value("${clemble.service.notification.system.user}") String user,
        @Value("${clemble.service.notification.system.password}") String password,
        @Value("${SYSTEM_NOTIFICATION_SERVICE_HOST}") String host) throws Exception {

        com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setUsername(user);
        connectionFactory.setPassword(password);
        connectionFactory.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("CL account client %d").build());

        CachingConnectionFactory springConnectionFactory = new CachingConnectionFactory(connectionFactory);

        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setExchange(PAYMENT_EXCHANGE);
        rabbitTemplate.setRoutingKey(PAYMENT_ROUTING_KEY);
        rabbitTemplate.setConnectionFactory(springConnectionFactory);

        RabbitAdmin rabbitAdmin = new RabbitAdmin(springConnectionFactory);
        rabbitAdmin.declareExchange(new DirectExchange(PAYMENT_EXCHANGE, true, false));
        rabbitAdmin.declareQueue(new Queue(PAYMENT_QUEUE, true));
        rabbitAdmin.declareBinding(new Binding(PAYMENT_QUEUE, Binding.DestinationType.QUEUE, PAYMENT_EXCHANGE, PaymentClientSpringConfiguration.Default.PAYMENT_ROUTING_KEY, new HashMap<String, Object>()));

        AmqpInvokerServiceExporter serviceExporter = new AmqpInvokerServiceExporter();
        serviceExporter.setAmqpTemplate(rabbitTemplate);
        serviceExporter.setServiceInterface(PlayerAccountService.class);
        serviceExporter.setService(playerAccountController);

        SimpleMessageListenerContainer accountServiceListener = new SimpleMessageListenerContainer();
        accountServiceListener.setMessageListener(serviceExporter);
        accountServiceListener.setQueueNames(PAYMENT_QUEUE);
        accountServiceListener.setConnectionFactory(springConnectionFactory);
        return accountServiceListener;
    }

}
