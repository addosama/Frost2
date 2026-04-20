package pub.frost.client.property.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to insert other object's property to current object
 * if the object implements Boolean Supplier, it will provide the visibility of this group
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InsertProperty {
    /**
     * if not blank, it will insert properties into a new group
     * @return the key of the group or keep it blank
     */
    String value() default "";
}
