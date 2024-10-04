package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.model;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ModelLeafDescription implements ModelDescription {

    private final Class<?> type;

    ModelLeafDescription(Class<?> type) {
        this.type = type;
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
        onLeaf.run();
    }

    @Override
    public void traverse(OnBranch onBranch, OnEnumeration onEnumeration, Consumer<Class<?>> onLeaf) {
        onLeaf.accept(type);
    }

    @Override
    public <T> T apply(Supplier<T> onBranch, Supplier<T> onLeaf) {
        return onLeaf.get();
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
        return 31 + type.hashCode();
    }

    @Override
    public String toString() {
        return "Singular{sort=leaf,type=" + type.getTypeName() + "}";
    }
}
