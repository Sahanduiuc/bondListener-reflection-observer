# Combine Java Reflection With Observer pattern

## Historical Motivation
We were working in a financial securities company and we noticed that 
we were looking up the hashmap each time a financial event is listened by our servers.

## Open for extension and Closed for modification
The domain experts just need to extend an abstract class and put the annotation
so that the infrastructure could detect the new listeners.

```java
@DomainEventListener(ExtMessageClasses = {BondProtos.Bond.class})
public class BondListener extends AbstractMessageListener<BondProtos.Bond> {

    public void setSubject(MessageDispatcherImpl messageDispatcher){
        this.messageDispatcherImpl=messageDispatcher;
        this.messageDispatcherImpl.attach(this);
    }

    @Override
    public void handle(BondProtos.Bond message)  {
        System.out.println("handled bond successfully!");
        System.out.println("message.getName()="+message.getName());
        System.out.println("message.getValue()="+message.getValue());
    }
}
```


## Solution
We just collect the listeners. At run time we register all listeners in the hashmap once!

```java_holder_method_tree
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
```
Now The observer pattern is used at compile time:
```java
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
```

## compile
 protoc bond.proto  --java_out=./


## Simulate and test
To simulate our framework, we need to dispatch some events.Lets say
Three Bonds(a financial security) events are received for example:
```java_holder_method_tree
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
```

## Results
```java_holder_method_tree
 bean
 found listener:com.farshad.domain.observers.BondListener
 2019-03-18 09:04:11.526  INFO 1948 --- [           main] o.s.b.web.embedded.netty.NettyWebServer  : Netty started on port(s): 8081
 2019-03-18 09:04:11.534  INFO 1948 --- [           main] com.farshad.App                          : Started App in 5.613 seconds (JVM running for 8.034)
 reflection once only!
 listener.getClass().getCanonicalName()=com.farshad.domain.observers.BondListener
 ----------state change------------
 handled bond successfully!
 message.getName()=farshadBond
 message.getValue()=22
 listener.getClass().getCanonicalName()=com.farshad.domain.observers.BondListener
 ----------state change------------
 handled bond successfully!
 message.getName()=farshadBond
 message.getValue()=22
 listener.getClass().getCanonicalName()=com.farshad.domain.observers.BondListener
 ----------state change------------
 handled bond successfully!
 message.getName()=farshadBond
 message.getValue()=22
```
