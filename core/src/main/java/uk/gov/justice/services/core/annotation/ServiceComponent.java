package uk.gov.justice.services.core.annotation;


import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.context.ApplicationScoped;

/**
 * Identifies service components. <p> Usage: @ServiceComponent({@link Component#COMMAND_API})
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(TYPE)
@ApplicationScoped
public @interface ServiceComponent {

    Component value();

}
