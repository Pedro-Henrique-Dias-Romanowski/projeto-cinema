package com.romanowski.pedro.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class AMQPConfiguration {

    @Bean
    public RabbitAdmin criaRabbitAdmin(ConnectionFactory conn){
        return new RabbitAdmin(conn);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> incializaAdmin(RabbitAdmin rabbitAdmin){
        return event -> rabbitAdmin.initialize();
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter messageConverter){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue filaDetalhesPedido(){
        return QueueBuilder
                .nonDurable("pagamentos.detalhes")
                .deadLetterExchange("pagamentos.dlx")
                .deadLetterRoutingKey("pagamentos.detalhes.dlq")
                .build();
    }

    @Bean
    public Queue filaDetalhesPedidoDLQ(){
        return QueueBuilder
                .nonDurable("pagamentos.detalhes.dlq")
                .build();
    }

    @Bean
    public FanoutExchange fanoutExchange(){
        return ExchangeBuilder
                .fanoutExchange("pagamentos.ex")
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange(){
        return ExchangeBuilder
                .directExchange("pagamentos.dlx")
                .build();
    }

    @Bean
    public Binding bindPagamentoPedido(){
        return BindingBuilder.bind(filaDetalhesPedido())
                .to(fanoutExchange());
    }

    @Bean
    public Binding bindPagamentoPedidoDLQ(){
        return BindingBuilder.bind(filaDetalhesPedidoDLQ())
                .to(deadLetterExchange())
                .with("pagamentos.detalhes.dlq");
    }
}
