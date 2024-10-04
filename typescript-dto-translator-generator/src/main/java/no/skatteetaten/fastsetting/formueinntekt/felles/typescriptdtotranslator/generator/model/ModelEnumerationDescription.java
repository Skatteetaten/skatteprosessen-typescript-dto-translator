package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ModelEnumerationDescription implements ModelDescription {

    private final Class<?> type;
    private final Map<String, String> enumerations;

    ModelEnumerationDescription(
        Class<?> type,
        Map<String, String> enumerations
    ) {
        this.type = type;
        this.enumerations = enumerations;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public void accept(
        BiConsumer<ModelDescription, Map<String, Property>> onBranch,
        Consumer<Map<String, String>> onEnumeration,
        Runnable onLeaf
    ) {
        onEnumeration.accept(enumerations);
    }

    @Override
    public <T> T apply(Supplier<T> onBranch, Supplier<T> onLeaf) {
        return onLeaf.get();
    }

    @Override
    public void traverse(OnBranch onBranch, OnEnumeration onEnumeration, Consumer<Class<?>> onLeaf) throws IOException {
        onEnumeration.accept(type, enumerations);
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof ModelDescription) {
            ModelDescription description = (ModelDescription) object;
            return description.getType() == type;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 17 + type.hashCode();
    }

    @Override
    public String toString() {
        return "Singular{sort=enumeration,type=" + type.getTypeName() + "}";
    }
}
