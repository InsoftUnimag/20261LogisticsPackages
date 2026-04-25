package com.logistics.packages.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * T312 [US3] - Configuración de RabbitMQ para solicitud y respuesta de rutas
 * MOD1-IP-003 - Phase 3
 * 
 * Configura los exchanges, queues y bindings necesarios para la comunicación
 * asíncrona con el Módulo de Gestión de Rutas.
 * 
 * T313: Incluye configuración de reintentos mediante Dead Letter Exchange (DLX)
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.messaging.ruta.request.exchange:solicitudes_ruta_exchange}")
    private String requestExchange;

    @Value("${app.messaging.ruta.request.queue:solicitudes_ruta_queue}")
    private String requestQueue;

    @Value("${app.messaging.ruta.request.routing-key:solicitud.nueva}")
    private String requestRoutingKey;

    @Value("${app.messaging.ruta.response.queue:respuestas_ruta_queue}")
    private String responseQueue;

    @Value("${app.messaging.ruta.dlx.exchange:solicitudes_ruta_dlx}")
    private String dlxExchange;

    @Value("${app.messaging.ruta.dlx.queue:solicitudes_ruta_dlq}")
    private String dlxQueue;

    /**
     * ObjectMapper configurado para JSON
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * Conversor de mensajes a JSON
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * RabbitTemplate configurado con conversor JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    /**
     * Exchange para solicitudes de ruta (tipo topic para flexibilidad)
     */
    @Bean
    public TopicExchange requestExchange() {
        return new TopicExchange(requestExchange, true, false);
    }

    /**
     * Cola para solicitudes de ruta con Dead Letter Exchange configurado
     * FR-005: Permite reintentos automáticos en caso de timeout
     */
    @Bean
    public Queue requestQueue() {
        return QueueBuilder.durable(requestQueue)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", "solicitud.failed")
                .build();
    }

    /**
     * Binding entre el exchange de solicitudes y la cola
     */
    @Bean
    public Binding requestBinding(Queue requestQueue, TopicExchange requestExchange) {
        return BindingBuilder.bind(requestQueue)
                .to(requestExchange)
                .with(requestRoutingKey);
    }

    /**
     * Cola para recibir respuestas del Módulo de Gestión de Rutas
     */
    @Bean
    public Queue responseQueue() {
        return new Queue(responseQueue, true);
    }

    /**
     * Dead Letter Exchange para mensajes fallidos
     * T313: Permite manejar reintentos y mensajes con errores persistentes
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(dlxExchange, true, false);
    }

    /**
     * Dead Letter Queue para análisis de mensajes fallidos
     */
    @Bean
    public Queue dlxQueue() {
        return new Queue(dlxQueue, true);
    }

    /**
     * Binding entre DLX y DLQ
     */
    @Bean
    public Binding dlxBinding(Queue dlxQueue, DirectExchange dlxExchange) {
        return BindingBuilder.bind(dlxQueue)
                .to(dlxExchange)
                .with("solicitud.failed");
    }

    /**
     * Configuración del listener container con reintentos
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false); // No reencolar inmediatamente, usar DLX
        factory.setPrefetchCount(10); // Número de mensajes a prefetch
        return factory;
    }
}
