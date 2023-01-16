package com.testcom.chat.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "tut.fanout";

    @Bean
    public FanoutExchange exchangeFanout() {
        return new FanoutExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue queueChat() {
        return new AnonymousQueue(new Base64UrlNamingStrategy("chat-"));
    }

    @Bean
    public Binding bindingChat(@Qualifier("exchangeFanout") FanoutExchange exchangeFanout, @Qualifier("queueChat") Queue queueChat) {
        return BindingBuilder.bind(queueChat).to(exchangeFanout);
    }
}
