package com.farshad;


import com.farshad.infrastructure.dispatcher.MessageDispatcherImpl;
import com.farshad.infrastructure.listener.MessageListenerFactory;
import com.google.protobuf.Message;
import com.farshad.infrastructure.protos.generated.domain.BondProtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class App {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    MessageListenerFactory messageListenerFactory;

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner demo() {
        return (args) -> {
            System.out.println("reflection once only!");
            BondProtos.Bond.Builder message = BondProtos.Bond.newBuilder().setName("farshadBond").setValue(22);
            Message builtMessage=message.build();
            MessageDispatcherImpl messageDispatcher=applicationContext.getBean(MessageDispatcherImpl.class);
            messageDispatcher.dispatchEvent("asset", builtMessage);
            messageDispatcher.dispatchEvent("asset", builtMessage);
            messageDispatcher.dispatchEvent("asset", builtMessage);
        };
    }

}
