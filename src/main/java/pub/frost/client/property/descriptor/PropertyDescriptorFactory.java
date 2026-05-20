package pub.frost.client.property.descriptor;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import pub.frost.client.core.FrostCore;
import pub.frost.client.i18n.annotations.TranslationKey;
import pub.frost.client.property.AbstractProperty;
import pub.frost.client.property.annotations.InsertProperty;
import pub.frost.client.property.annotations.Property;
import pub.frost.client.property.annotations.PropertyGroupHead;
import pub.frost.client.property.annotations.PropertyGroupMain;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Accessors(chain = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyDescriptorFactory {

    private final Object target;

    public static PropertyDescriptorFactory create(Object target) {
        return new PropertyDescriptorFactory(target);
    }

    // ── output ────────────────────────────────────────────────────────────────
    private final Map<String, PropertyDescriptor> descriptorMap = new LinkedHashMap<>();
    private final Map<String, PropertyDescriptor> flattenedDescriptorMap = new HashMap<>();

    // ── config ────────────────────────────────────────────────────────────────
    @Setter private String keyPrefix = "";
    @Setter private boolean skipDeprecated = true;

    private boolean built = false;

    // ═════════════════════════════════════════════════════════════════════════
    // Build
    // ═════════════════════════════════════════════════════════════════════════

    public PropertyDescriptorFactory build() {
        List<PropertyDescriptor> topLevel = processObject(target, keyPrefix);
        for (PropertyDescriptor d : topLevel) {
            descriptorMap.put(d.getKey(), d);
        }
        built = true;
        return this;
    }

    // ── public accessors ──────────────────────────────────────────────────────

    public Map<String, PropertyDescriptor> getDescriptorMap() {
        if (FrostCore.DEBUG) assert built : "call build() first";
        return descriptorMap;
    }

    public Map<String, PropertyDescriptor> getFlattenedDescriptorMap() {
        if (FrostCore.DEBUG) assert built : "call build() first";
        return flattenedDescriptorMap;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Core recursive processor
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Top-level entry — no outer group exists, so @PropertyGroupMain found at
     * the top of the field list has nowhere to propagate and is silently ignored.
     */
    private List<PropertyDescriptor> processObject(Object object, String keyPrefix) {
        return processObject(object, keyPrefix, mp -> { /* top-level: no outer group to write into */ });
    }

    /**
     * Walks all declared fields of {@code object} and returns the ordered list
     * of descriptors produced.  Groups are represented as tree nodes; every
     * descriptor (leaf or group) is also registered in
     * {@link #flattenedDescriptorMap}.
     *
     * @param object              object whose fields are inspected
     * @param keyPrefix           prefix prepended to every key produced here
     * @param onMainPropertyFound callback invoked when a {@code @PropertyGroupMain}
     *                            field is encountered but no group is currently open
     *                            in this scope — allows the caller (an outer
     *                            {@code InsertProperty} group) to claim the property
     */
    private List<PropertyDescriptor> processObject(
            Object object,
            String keyPrefix,
            Consumer<AbstractProperty<?, ?>> onMainPropertyFound
    ) {
        if (object instanceof ManualDescriptorProvider) {
            return ((ManualDescriptorProvider) object).provideDescriptors(keyPrefix, onMainPropertyFound);
        }

        int[] unnamedIndex = {0};

        List<PropertyDescriptor> currentList = new ArrayList<>();
        Deque<GroupContext> groupStack = new ArrayDeque<>();

        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                if (skipDeprecated && field.isAnnotationPresent(Deprecated.class)) continue;

                boolean isPropertyField  = AbstractProperty.isPropertyField(field);
                boolean isGroupHeadField = AbstractProperty.isGroupHead(field);
                boolean isInsertField    = field.isAnnotationPresent(InsertProperty.class);

                if (!isPropertyField && !isGroupHeadField && !isInsertField) continue;

                field.setAccessible(true);

                // ── 1. Open a new group if this field requests it ─────────────
                GroupOpenSpec openSpec = resolveGroupOpen(field, object, isPropertyField, isGroupHeadField, isInsertField);
                if (openSpec != null) {
                    groupStack.push(new GroupContext(
                            openSpec.groupKey,
                            openSpec.translationKey,
                            openSpec.visibilitySupplier,
                            new ArrayList<>()
                    ));
                }

                // ── 2. Handle the payload of this field ───────────────────────
                List<PropertyDescriptor> targetList = groupStack.isEmpty()
                        ? currentList
                        : groupStack.peek().children;

                if (isPropertyField) {
                    AbstractProperty<?, ?> prop = (AbstractProperty<?, ?>) field.get(object);
                    Property anno = field.getAnnotation(Property.class);
                    if (anno.allowOverriding()) prop.enableOverriding();

                    String propKey = buildPropertyKey(anno.value(), unnamedIndex, groupStack, keyPrefix);
                    String translationKey = translationKeyOf(field);

                    PropertyDescriptor descriptor = new PropertyDescriptor(propKey, prop, translationKey);

                    if (field.isAnnotationPresent(PropertyGroupMain.class)) {
                        if (!groupStack.isEmpty()) {
                            // Within an open group in this scope — write directly.
                            groupStack.peek().mainProperty = prop;
                        } else {
                            // No open group here; propagate to the caller so the
                            // enclosing @InsertProperty group can claim it.
                            onMainPropertyFound.accept(prop);
                        }
                    }

                    targetList.add(descriptor);
                    flattenedDescriptorMap.put(propKey, descriptor);

                } else if (isInsertField) {
                    Object inserted = field.get(object);
                    if (inserted != null) {
                        // If this insert opened a group, its mainProperty lives in
                        // the top GroupContext.  If there is no surrounding group
                        // (plain insert without a key), propagate further upward.
                        Consumer<AbstractProperty<?, ?>> mainCallback = !groupStack.isEmpty()
                                ? mp -> groupStack.peek().mainProperty = mp
                                : onMainPropertyFound;

                        String nestedPrefix = buildNestedPrefix(groupStack, keyPrefix);
                        List<PropertyDescriptor> nested = processObject(inserted, nestedPrefix, mainCallback);
                        targetList.addAll(nested);
                    }
                }
                // GroupHead fields carry no payload beyond opening the group.

                // ── 3. Close the current group if this field requests it ──────
                boolean closesGroup = resolveGroupClose(field, isPropertyField, isInsertField);
                if (closesGroup && !groupStack.isEmpty()) {
                    GroupContext closed = groupStack.pop();
                    List<PropertyDescriptor> parentList = groupStack.isEmpty()
                            ? currentList
                            : groupStack.peek().children;

                    String groupKey = (keyPrefix + closed.groupKey).toLowerCase(Locale.ROOT);

                    PropertyDescriptor groupDescriptor = new PropertyDescriptor(
                            groupKey,
                            closed.mainProperty,
                            closed.children,
                            closed.visibilitySupplier,
                            closed.translationKey
                    );

                    parentList.add(groupDescriptor);
                    flattenedDescriptorMap.put(groupKey, groupDescriptor);
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access field: " + field.getName(), e);
            } catch (RuntimeException e) {
                System.err.printf("Exception processing field: %s%n", field.getName());
                throw e;
            }
        }

        return currentList;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Helpers
    // ═════════════════════════════════════════════════════════════════════════

    /** All information needed to open a group from a single field. */
    private static final class GroupOpenSpec {
        final String groupKey;
        final String translationKey;
        final Supplier<Boolean> visibilitySupplier;

        GroupOpenSpec(String groupKey, String translationKey, Supplier<Boolean> visibilitySupplier) {
            this.groupKey           = groupKey;
            this.translationKey     = translationKey;
            this.visibilitySupplier = visibilitySupplier;
        }
    }

    /**
     * Inspects {@code field} and returns a {@link GroupOpenSpec} when this
     * field should open a new group, or {@code null} otherwise.
     */
    private static GroupOpenSpec resolveGroupOpen(
            Field field, Object object,
            boolean isPropertyField, boolean isGroupHeadField, boolean isInsertField
    ) throws IllegalAccessException {

        if (isPropertyField) {
            String groupKey = field.getAnnotation(Property.class).startGroup();
            if (StringUtils.isBlank(groupKey)) return null;
            return new GroupOpenSpec(groupKey, translationKeyOf(field), () -> true);
        }

        if (isGroupHeadField) {
            String groupKey = field.getAnnotation(PropertyGroupHead.class).value();
            if (StringUtils.isBlank(groupKey)) return null;
            @SuppressWarnings("unchecked")
            Supplier<Boolean> visibility = (Supplier<Boolean>) field.get(object);
            return new GroupOpenSpec(groupKey, translationKeyOf(field), visibility);
        }

        if (isInsertField) {
            String groupKey = field.getAnnotation(InsertProperty.class).value();
            if (StringUtils.isBlank(groupKey)) return null;  // plain insert, no wrapping group
            Supplier<Boolean> visibility = resolveVisibilityFromInstance(field.get(object));
            return new GroupOpenSpec(groupKey, translationKeyOf(field), visibility);
        }

        return null;
    }

    /**
     * Returns {@code true} when the field should close the currently open group.
     * For {@code @InsertProperty} with a non-blank key this is always true
     * (the field both opens and closes the group in one step).
     */
    private static boolean resolveGroupClose(Field field, boolean isPropertyField, boolean isInsertField) {
        if (isPropertyField) {
            return field.getAnnotation(Property.class).endGroup();
        }
        if (isInsertField) {
            return !StringUtils.isBlank(field.getAnnotation(InsertProperty.class).value());
        }
        return false;
    }

    /**
     * If the inserted object implements {@code Supplier<Boolean>} it is used as
     * the group visibility supplier; otherwise visibility defaults to {@code true}.
     */
    private static Supplier<Boolean> resolveVisibilityFromInstance(Object instance) {
        for (Type type : instance.getClass().getGenericInterfaces()) {
            if (!(type instanceof ParameterizedType)) continue;
            ParameterizedType pt = (ParameterizedType) type;
            if (pt.getRawType() != Supplier.class) continue;
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] == Boolean.class) {
                @SuppressWarnings("unchecked")
                Supplier<Boolean> s = (Supplier<Boolean>) instance;
                return s;
            }
        }
        return () -> true;
    }

    /** Reads the {@link TranslationKey} annotation, falling back to {@code "~"}. */
    private static String translationKeyOf(Field field) {
        TranslationKey tk = field.getAnnotation(TranslationKey.class);
        return tk == null ? "~" : tk.value();
    }

    /**
     * Builds the fully-qualified key for a property field.
     *
     * <p>Format: {@code <keyPrefix><groupPrefix>.subprops.<propKey>}
     * or simply {@code <keyPrefix><propKey>} when there is no enclosing group.
     */
    private static String buildPropertyKey(
            String rawKey,
            int[] unnamedIndex,
            Deque<GroupContext> groupStack,
            String keyPrefix
    ) {
        if (StringUtils.isBlank(rawKey)) {
            rawKey = "unnamed-property-" + unnamedIndex[0]++;
        }

        if (!groupStack.isEmpty()) {
            String groupPrefix = groupStack.peek().groupKey.toLowerCase(Locale.ROOT);
            rawKey = groupPrefix + ".subprops." + rawKey;
        }

        return (keyPrefix + rawKey).toLowerCase(Locale.ROOT);
    }

    /**
     * Builds the key prefix passed to a nested {@link #processObject} call so
     * that inserted-object keys are scoped correctly under the current group.
     */
    private static String buildNestedPrefix(Deque<GroupContext> groupStack, String keyPrefix) {
        if (groupStack.isEmpty()) return keyPrefix;
        String groupPrefix = groupStack.peek().groupKey.toLowerCase(Locale.ROOT);
        return keyPrefix + groupPrefix + ".subprops.";
    }

    // ═════════════════════════════════════════════════════════════════════════
    // Internal state for an open group
    // ═════════════════════════════════════════════════════════════════════════

    private static final class GroupContext {
        final String groupKey;
        final String translationKey;
        final Supplier<Boolean> visibilitySupplier;
        final List<PropertyDescriptor> children;
        AbstractProperty<?, ?> mainProperty;  // set when @PropertyGroupMain is encountered

        GroupContext(
                String groupKey,
                String translationKey,
                Supplier<Boolean> visibilitySupplier,
                List<PropertyDescriptor> children
        ) {
            this.groupKey           = groupKey;
            this.translationKey     = translationKey;
            this.visibilitySupplier = visibilitySupplier;
            this.children           = children;
        }
    }
}