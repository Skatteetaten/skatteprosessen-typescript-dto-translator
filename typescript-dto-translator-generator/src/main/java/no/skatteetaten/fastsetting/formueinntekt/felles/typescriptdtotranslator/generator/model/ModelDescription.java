package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.model;

import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.StructuralResolver;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public interface ModelDescription {

    Class<?> getType();

    void accept(
        BiConsumer<ModelDescription, Map<String, Property>> onBranch,
        Consumer<Map<String, String>> onEnumeration,
        Runnable onLeaf
    );

    <T> T apply(
        Supplier<T> onBranch,
        Supplier<T> onLeaf
    );

    void traverse(
        OnBranch onBranch,
        OnEnumeration onEnumeration,
        Consumer<Class<?>> onLeaf
    ) throws IOException;

    static List<ModelDescription> of(
        StructuralResolver structuralResolver,
        BiPredicate<Class<?>, String> condition,
        List<Class<?>> types
    ) {
        Set<Class<?>> resolved = new HashSet<>();
        Map<Class<?>, ModelDescription> references = new HashMap<>();
        return types.stream().distinct()
            .map(type -> of(structuralResolver, condition, type, resolved, references))
            .collect(Collectors.toList());
    }

    private static <PROPERTY> ModelDescription of(
        StructuralResolver structuralResolver,
        BiPredicate<Class<?>, String> condition,
        Class<?> type,
        Set<Class<?>> resolved,
        Map<Class<?>, ModelDescription> references
    ) {
        if (!resolved.add(type)) {
            return new ModelRecursiveDescription(() -> references.get(type));
        }
        Optional<StructuralResolver.Branch<?>> candidate;
        ModelDescription description;
        if (type.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> enumeration = (Class<? extends Enum<?>>) type;
            description = new ModelEnumerationDescription(
                type,
                structuralResolver.toEnumerations(enumeration)
            );
        } else if (type.isPrimitive() || (candidate = structuralResolver.toBranch(type)).isEmpty()) {
            description = new ModelLeafDescription(type);
        } else {
            @SuppressWarnings("unchecked")
            StructuralResolver.Branch<PROPERTY> branch = (StructuralResolver.Branch<PROPERTY>) candidate.get();
            Map<String, Property> properties = new LinkedHashMap<>();
            for (PROPERTY property : branch.getProperties()) {
                String name = branch.getName(property);
                if (!condition.test(type, name)) {
                    continue;
                }
                Class<?> target = branch.getType(property);
                boolean array;
                if (List.class.isAssignableFrom(target)) {
                    Type generic = branch.getGenericType(property);
                    if (!(generic instanceof ParameterizedType)
                        || ((ParameterizedType) generic).getActualTypeArguments().length != 1
                        || !(((ParameterizedType) generic).getActualTypeArguments()[0] instanceof Class<?>)) {
                        throw new IllegalArgumentException("Unexpected generic type for " + property);
                    }
                    target = (Class<?>) ((ParameterizedType) generic).getActualTypeArguments()[0];
                    array = true;
                } else if (Collection.class.isAssignableFrom(target) || Map.class.isAssignableFrom(target)) {
                    throw new IllegalArgumentException("Only list collection types are supported: " + property);
                } else {
                    array = false;
                }
                properties.put(name, new Property(
                    of(structuralResolver, condition, target, resolved, references),
                    array,
                    !(target.isPrimitive() || branch.isRequired(property))
                ));
            }
            description = new ModelBranchDescription(
                type,
                properties,
                branch.getSuperClass()
                    .map(superType -> of(structuralResolver, condition, superType, resolved, references))
                    .orElse(null),
                branch.getSubClasses().stream()
                    .map(subType -> of(structuralResolver, condition, subType, resolved, references))
                    .collect(Collectors.toList())
            );
        }
        references.put(type, description);
        return description;
    }

    class Property {

        private final ModelDescription description;

        private final boolean array, optional;

        Property(ModelDescription description, boolean array, boolean optional) {
            this.description = description;
            this.array = array;
            this.optional = optional;
        }

        public ModelDescription getDescription() {
            return description;
        }

        public boolean isArray() {
            return array;
        }

        public boolean isOptional() {
            return optional;
        }
    }

    @FunctionalInterface
    interface OnBranch {

        void accept(
            Class<?> type,
            ModelDescription superType,
            List<ModelDescription> subTypes,
            Map<String, Property> properties
        ) throws IOException;
    }

    @FunctionalInterface
    interface OnEnumeration {

        void accept(
            Class<?> type,
            Map<String, String> enumerations
        ) throws IOException;
    }
}
