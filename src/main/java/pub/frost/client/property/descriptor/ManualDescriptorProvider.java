package pub.frost.client.property.descriptor;

import pub.frost.client.property.AbstractProperty;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ManualDescriptorProvider {
    List<PropertyDescriptor> provideDescriptors(
            String keyPrefix,
            Consumer<AbstractProperty<?, ?>> mainPropConsumer, Function<PropertyDescriptor, PropertyDescriptor> descriptorRegister
    );
}
