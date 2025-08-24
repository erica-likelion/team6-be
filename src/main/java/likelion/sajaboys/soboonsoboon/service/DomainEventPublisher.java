package likelion.sajaboys.soboonsoboon.service;

import likelion.sajaboys.soboonsoboon.service.ai.MessagePostedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DomainEventPublisher {
    private final ApplicationEventPublisher publisher;

    public DomainEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(MessagePostedEvent event) {
        publisher.publishEvent(event);
    }
}
