package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@FunctionalInterface
public interface StructuralResolver {

    Optional<Branch<?>> toBranch(Class<?> type);

    default Map<String, String> toEnumerations(Class<? extends Enum<?>> type) {
        return Arrays.stream(type.getEnumConstants()).collect(Collectors.toMap(Enum::name, Enum::name, (left, right) -> {
            throw new IllegalStateException();
        }, LinkedHashMap::new));
    }

    interface Branch<PROPERTY> {

        Iterable<? extends PROPERTY> getProperties();

        String getName(PROPERTY property);

        Class<?> getType(PROPERTY property);

        default Type getGenericType(PROPERTY property) {
            throw new UnsupportedOperationException();
        }

        default boolean isRequired(PROPERTY property) {
            return false;
        }

        default Optional<Class<?>> getSuperClass() {
            return Optional.empty();
        }

        default List<Class<?>> getSubClasses() {
            return Collections.emptyList();
        }
    }
}
