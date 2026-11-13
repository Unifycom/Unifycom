package io.unifycom.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * @author 01373207
 */
@java.lang.annotation.Target(TYPE)
@Retention(RUNTIME)
public @interface MessageToEvent {

    Class<?> value();
}
