package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator;

import java.util.function.Function;

public class SimpleNameNamingStrategy implements Function<Class<?>, TypeName> {

    @Override
    public TypeName apply(Class<?> type) {
        return new TypeName(type.getSimpleName());
    }
}
