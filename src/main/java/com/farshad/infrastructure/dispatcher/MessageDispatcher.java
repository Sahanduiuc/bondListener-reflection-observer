package com.farshad.infrastructure.dispatcher;

import com.google.protobuf.Message;

public interface MessageDispatcher {
    void dispatchEvent(String topic, Message message);
}

