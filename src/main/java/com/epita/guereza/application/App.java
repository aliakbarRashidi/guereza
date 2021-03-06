package com.epita.guereza.application;

import com.epita.eventbus.EventMessage;
import com.epita.eventbus.client.EventBusClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    protected final String uid;
    protected final EventBusClient eventBus;

    protected App(final EventBusClient eventBus) {
        this.uid = UUID.randomUUID().toString();
        this.eventBus = eventBus;
    }

    /**
     * The main stuff
     */
    public abstract void run();

    /**
     * Send an object through the eventBus
     *
     * @param channel The channel to send message
     * @param obj     The object to send
     */
    protected void sendMessage(final String channel, final Object obj) {
        try {
            final EventMessage em = new EventMessage(channel, obj);
            eventBus.publish(em);
        } catch (final JsonProcessingException e) {
            LOGGER.error("Impossible to send message: {}", e.getMessage());
        }
    }

    protected void retryIn(final int seconds, final Runnable consumer) {
        LOGGER.info("Retry fetching url in {}seconds", seconds);
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(consumer, seconds, TimeUnit.SECONDS);
        executor.shutdownNow();
    }

    protected Object mappingObject(final EventBusClient.Message message) {
        try {
            final Class c = ClassLoader.getSystemClassLoader().loadClass(message.getMessageType());
            return new ObjectMapper().readValue(message.getContent(), c);
        } catch (final Exception e) {
            return null;
        }
    }
}
