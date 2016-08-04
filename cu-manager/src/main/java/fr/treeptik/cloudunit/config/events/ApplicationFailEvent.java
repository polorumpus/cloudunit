package fr.treeptik.cloudunit.config.events;

import fr.treeptik.cloudunit.model.Application;
import org.springframework.context.ApplicationEvent;

/**
 * Created by nicolas on 03/08/2016.
 */
public class ApplicationFailEvent extends ApplicationEvent {

    public ApplicationFailEvent(Application source) {
        super(source);
    }

}
