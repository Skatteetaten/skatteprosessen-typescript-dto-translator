package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator;

import java.util.Objects;
import java.util.function.Function;

public class SimpleNameWithNamespaceNamingStrategy implements Function<Class<?>, TypeName> {

    private final String defaultNamespace;

    public SimpleNameWithNamespaceNamingStrategy() {
        defaultNamespace = "";
    }

    public SimpleNameWithNamespaceNamingStrategy(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }

    @Override
    public TypeName apply(Class<?> type) {
        if (Objects.equals(defaultNamespace, type.getPackageName())) {
            return new TypeName(type.getSimpleName());
        } else {
            return new TypeName(type.getPackageName(), type.getSimpleName());
        }
    }
}
