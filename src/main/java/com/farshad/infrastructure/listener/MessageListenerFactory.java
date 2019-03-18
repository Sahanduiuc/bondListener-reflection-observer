package com.farshad.infrastructure.listener;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class MessageListenerFactory {

    private final ApplicationContext context;

    private final MessageListener messageListener;

    @Autowired
    public MessageListenerFactory(ApplicationContext context, MessageListener messageListener) {
        this.context = context;
        this.messageListener = messageListener;
    }

    public AbstractMessageListener getListeners(Class eventClass){
        Class<? extends AbstractMessageListener> listenerClass = messageListener.getMessageListener(eventClass);
        if(listenerClass != null) {
            return context.getBean(listenerClass);
        }
        return null;
    }

    public boolean isSubscribeToEvent(String eventClassName){
        if(messageListener.getMessageListener(eventClassName) != null){
            return true;
        }else {
            return false;
        }
    }
}

