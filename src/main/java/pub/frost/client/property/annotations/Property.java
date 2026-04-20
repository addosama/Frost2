package pub.frost.client.property.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to register a property.
 * must be annotated on AbstractProperty field
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    /**
     * the key of current property
     * @return key
     */
    String value();

    /**
     * if not blank, create a group before register current property
     * @return group key
     */
    String startGroup() default "";

    /**
     * if true, end current group after register current property
     * @return end current group or not
     */
    boolean endGroup() default false;

    /**
     * if true, enable overriding ability for current property
     * @return enable overriding ability or not
     */
    boolean allowOverriding() default false;
}
