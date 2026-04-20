package pub.frost.client.property.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to begin a property group.
 * must be annotated on Boolean Supplier field that provide the visibility of this group;
 * if not, consider using @Property(beginGroup = "GroupName")
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyGroupHead {
    /**
     * @return group key
     */
    String value();
}
