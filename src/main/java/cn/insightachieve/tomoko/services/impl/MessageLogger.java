package cn.insightachieve.tomoko.services.impl;

import cn.insightachieve.tomoko.models.Message;
import cn.insightachieve.tomoko.services.IMessageHandler;
import com.google.gson.Gson;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class MessageLogger implements IMessageHandler {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MessageLogger.class);

    private Gson gson = new Gson();

    @Override
    public void handle(Message message) {
        LOGGER.info(gson.toJson(message));
    }

    @PostConstruct
    private void init() {
        // Do something.
    }

    @PreDestroy
    private void destroy() {
        // Do something.
    }
}
