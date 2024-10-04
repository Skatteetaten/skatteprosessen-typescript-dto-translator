package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Year;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class DefaultTypeResolver implements Function<Class<?>, Optional<TypeName>> {

    private final boolean useBigInt;

    public DefaultTypeResolver() {
        useBigInt = false;
    }

    public DefaultTypeResolver(boolean useBigInt) {
        this.useBigInt = useBigInt;
    }

    @Override
    public Optional<TypeName> apply(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            return Optional.of(TypeName.BOOLEAN);
        } else if (CharSequence.class.isAssignableFrom(type)
                || type == UUID.class
                || type == char.class
                || type == Character.class) {
            return Optional.of(TypeName.STRING);
        } else if (useBigInt && (type == BigInteger.class || type == BigDecimal.class)) {
            return Optional.of(TypeName.BIGINT);
        } else if (type == byte.class
                || type == short.class
                || type == int.class
                || type == long.class
                || type == float.class
                || type == double.class
                || Number.class.isAssignableFrom(type)) {
            return Optional.of(TypeName.NUMBER);
        } else if (Temporal.class.isAssignableFrom(type)) {
            return Optional.of(type == Year.class
                ? TypeName.NUMBER
                : TypeName.STRING);
        } else {
            return Optional.empty();
        }
    }
}
