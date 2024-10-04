package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ModelRecursiveDescription implements ModelDescription {

    private final Supplier<ModelDescription> reference;

    ModelRecursiveDescription(Supplier<ModelDescription> reference) {
        this.reference = reference;
    }

    @Override
    public Class<?> getType() {
        return reference.get().getType();
    }

    @Override
    public void accept(
        BiConsumer<ModelDescription, Map<String, Property>> onBranch,
        Consumer<Map<String, String>> onEnumeration,
        Runnable onLeaf
    ) {
        reference.get().accept(onBranch, onEnumeration, onLeaf);
    }

    @Override
    public <T> T apply(Supplier<T> onBranch, Supplier<T> onLeaf) {
        return reference.get().apply(onBranch, onLeaf);
    }

    @Override
    public void traverse(
        OnBranch onBranch,
        OnEnumeration onEnumeration,
        Consumer<Class<?>> onLeaf
    ) { }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        } else if (object instanceof ModelDescription) {
            return reference.get().equals(object);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return reference.get().hashCode();
    }

    @Override
    public String toString() {
        return reference.get().toString();
    }
}
