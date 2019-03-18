package com.farshad.infrastructure.dispatcher;


import com.farshad.infrastructure.listener.AbstractMessageListener;
import com.farshad.infrastructure.listener.MessageListenerFactory;
import com.google.protobuf.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageDispatcherImpl implements MessageDispatcher{


    private final MessageListenerFactory listenerFactory;

    private final ApplicationContext context;


    private  AbstractMessageListener abstractMessageListener;

    private List<AbstractMessageListener> observers = new ArrayList<AbstractMessageListener>();

    private Message message;

    @Autowired
    public MessageDispatcherImpl(MessageListenerFactory listenerFactory, ApplicationContext context) {
        this.listenerFactory = listenerFactory;
        this.context=context;
    }


    public void setMessage(Message message){
        this.message=message;
        notifyAllObservers();
    }


    public void attach(AbstractMessageListener abstractMessageListener){
        observers.add(abstractMessageListener);
    }

    public void notifyAllObservers(){
        for (AbstractMessageListener observer : observers) {
            observer.handle(message);
        }
    }


    private boolean callMessageListener(Message message) {
        abstractMessageListener = listenerFactory.getListeners(message.getClass());
        System.out.println("listener.getClass().getCanonicalName()="+abstractMessageListener.getClass().getCanonicalName());
        abstractMessageListener= context.getBean(abstractMessageListener.getClass());
        abstractMessageListener.setSubject(this);
        System.out.println("----------state change------------");
        this.setMessage(message);
        observers= new ArrayList<AbstractMessageListener>();
        return true;
    }


    @Override
    public void dispatchEvent(String topic, Message message) {
        callMessageListener(message);
    }


}
