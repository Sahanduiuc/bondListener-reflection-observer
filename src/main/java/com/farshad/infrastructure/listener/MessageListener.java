package com.farshad.infrastructure.listener;



import com.farshad.infrastructure.annotations.DomainEventListener;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Set;

@Scope(value = "singleton")
@Component
public class MessageListener {


    private HashMap<String, Class<? extends AbstractMessageListener>> messageListeners;

    private String targetPackage;

    public MessageListener() {
        this.messageListeners = new HashMap<>();
    }

    private HashMap<String, Class<? extends AbstractMessageListener>> registerIntegrationEventListeners() throws ClassNotFoundException {
        HashMap<String, Class<? extends AbstractMessageListener>> messageListenerList = new HashMap<>();
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(DomainEventListener.class));
        Set<BeanDefinition> beans = provider.findCandidateComponents(targetPackage);
        for (BeanDefinition bd : beans) {
            System.out.println("bean");
            Class messageListenerClass = Class.forName(bd.getBeanClassName());
            Class[] messageClasses = ((DomainEventListener) messageListenerClass.getAnnotation(DomainEventListener.class)).ExtMessageClasses();
            for (Class messageClass : messageClasses) {
                System.out.println("found listener:"+messageListenerClass.getCanonicalName());
                messageListenerList.put(messageClass.getCanonicalName(), messageListenerClass);
            }
        }
        return messageListenerList;
    }


    @PostConstruct
    public void init() throws ClassNotFoundException {
        this.targetPackage = "com.farshad";
        this.messageListeners = registerIntegrationEventListeners();
    }

    public Class<? extends AbstractMessageListener> getMessageListener(String eventClassName) {
        return messageListeners.getOrDefault(eventClassName, null);
    }

    public Class<? extends AbstractMessageListener> getMessageListener(Class eventClass) {
        return getMessageListener(eventClass.getCanonicalName());
    }
}

