package cn.insightachieve.tomoko.services;

import cn.insightachieve.tomoko.models.Message;

public interface IMessageHandler {
    void handle(Message message);
}
