package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StructuralTypeStructuralResolver implements StructuralResolver {

    private final Function<Class<?>, String> prefix;

    private final boolean enumerations;

    private final Class<? extends Annotation> compoundOf, subtypedBy, enumeratedAs;

    private final MethodHandle subtypeByValue, enumeratedAsValue;

    @SuppressWarnings("unchecked")
    private StructuralTypeStructuralResolver(ClassLoader classLoader) {
        prefix = type -> type == boolean.class ? "is" : "get";
        enumerations = true;
        try {
            compoundOf = (Class<? extends Annotation>) Class.forName("no.skatteetaten.fastsetting.formueinntekt.felles.structuraltype.api.CompoundOf", true, classLoader);
            subtypedBy = (Class<? extends Annotation>) Class.forName("no.skatteetaten.fastsetting.formueinntekt.felles.structuraltype.api.SubtypedBy", true, classLoader);
            subtypeByValue = MethodHandles.publicLookup().findVirtual(subtypedBy, "value", MethodType.methodType(Class[].class));
            enumeratedAs = (Class<? extends Annotation>) Class.forName("no.skatteetaten.fastsetting.formueinntekt.felles.structuraltype.api.EnumeratedAs", true, classLoader);
            enumeratedAsValue = MethodHandles.publicLookup().findVirtual(subtypedBy, "value", MethodType.methodType(Class[].class));
        } catch (Exception e) {
            throw new IllegalStateException("Could not resolve structural type API of " + classLoader, e);
        }
    }

    private StructuralTypeStructuralResolver(
        Function<Class<?>, String> prefix,
        boolean enumerations,
        Class<? extends Annotation> compoundOf,
        Class<? extends Annotation> subtypedBy,
        Class<? extends Annotation> enumeratedAs,
        MethodHandle subtypeByValue,
        MethodHandle enumeratedAsValue
    ) {
        this.prefix = prefix;
        this.enumerations = enumerations;
        this.compoundOf = compoundOf;
        this.subtypedBy = subtypedBy;
        this.enumeratedAs = enumeratedAs;
        this.subtypeByValue = subtypeByValue;
        this.enumeratedAsValue = enumeratedAsValue;
    }

    public static StructuralTypeStructuralResolver of() {
        return of(StructuralTypeStructuralResolver.class.getClassLoader());
    }

    public static StructuralTypeStructuralResolver of(ClassLoader classLoader) {
        return new StructuralTypeStructuralResolver(classLoader);
    }

    public StructuralTypeStructuralResolver withPrefix(Function<Class<?>, String> prefix) {
        return new StructuralTypeStructuralResolver(prefix, enumerations, compoundOf, subtypedBy, enumeratedAs, subtypeByValue, enumeratedAsValue);
    }

    public StructuralTypeStructuralResolver withEnumerations(boolean enumerations) {
        return new StructuralTypeStructuralResolver(prefix, enumerations, compoundOf, subtypedBy, enumeratedAs, subtypeByValue, enumeratedAsValue);
    }

    @Override
    public Optional<Branch<?>> toBranch(Class<?> type) {
        if (type.isAnnotationPresent(compoundOf)) {
            return Optional.of(new Branch<Method>() {
                @Override
                public Iterable<Method> getProperties() {
                    return () -> Stream.of(type.getDeclaredMethods())
                        .filter(method -> method.getName().startsWith(prefix.apply(method.getReturnType())))
                        .filter(method -> !method.isSynthetic())
                        .iterator();
                }

                @Override
                public String getName(Method property) {
                    String name = property.getName(), prefix = StructuralTypeStructuralResolver.this.prefix.apply(property.getReturnType());
                    if (prefix.equals(name)) {
                        return "$value";
                    } else {
                        return name.substring(prefix.length(), prefix.length() + 1).toLowerCase() + name.substring(prefix.length() + 1);
                    }
                }

                @Override
                public Class<?> getType(Method property) {
                    Class<?> type = property.getReturnType();
                    if (type == Optional.class) {
                        Type generic = property.getGenericReturnType();
                        if (generic instanceof ParameterizedType) {
                            Type argument = ((ParameterizedType) generic).getActualTypeArguments()[0];
                            if (argument instanceof Class<?>) {
                                return (Class<?>) argument;
                            } else {
                                throw new IllegalStateException("Expected optional type to be parameterized for " + property);
                            }
                        } else {
                            throw new IllegalStateException("Expected optional type to be parameterized for " + property);
                        }
                    } else {
                        return type;
                    }
                }

                @Override
                public Type getGenericType(Method property) {
                    return property.getGenericReturnType();
                }

                @Override
                public Optional<Class<?>> getSuperClass() {
                    return Stream.of(type.getInterfaces())
                        .filter(iface -> iface.isAnnotationPresent(compoundOf))
                        .findAny();
                }

                @Override
                public List<Class<?>> getSubClasses() {
                    try {
                        Annotation annotation = type.getAnnotation(subtypedBy);
                        return annotation == null ? List.of() : List.of((Class<?>[]) subtypeByValue.invoke(annotation));
                    } catch (Throwable t) {
                        throw new IllegalStateException(t);
                    }
                }
            });
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Map<String, String> toEnumerations(Class<? extends Enum<?>> type) {
        if (!enumerations) {
            return StructuralResolver.super.toEnumerations(type);
        }
        return Arrays.stream(type.getEnumConstants()).collect(Collectors.toMap(Enum::name, value -> {
            try {
                Annotation annotation = type.getField(value.name()).getAnnotation(enumeratedAs);
                if (annotation == null) {
                    return value.name();
                }
                return (String) enumeratedAsValue.invoke(annotation);
            } catch (Throwable t) {
                throw new IllegalStateException(t);
            }
        }, (left, right) -> {
            throw new IllegalStateException();
        }, LinkedHashMap::new));
    }
}
