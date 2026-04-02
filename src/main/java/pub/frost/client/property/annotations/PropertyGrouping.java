package pub.frost.client.property.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class PropertyGrouping {
    /**
     * Push property group before registering current field
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Push {
        String value();
    }

    /**
     * Pop property group after registered current field and add current descriptor to property list
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Pop {}
}
