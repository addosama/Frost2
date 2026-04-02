package pub.frost.client.feature.module.annotations;

import pub.frost.client.feature.module.api.ModuleCategory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Module {
    String key();
    ModuleCategory category();
    boolean defaultState() default false;
}
