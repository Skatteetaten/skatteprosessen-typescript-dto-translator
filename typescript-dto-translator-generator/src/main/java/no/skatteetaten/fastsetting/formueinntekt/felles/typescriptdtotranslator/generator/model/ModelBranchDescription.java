package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

class ModelBranchDescription implements ModelDescription {

    private final Class<?> type;
    private final Map<String, Property> properties;
    private final ModelDescription superType;
    private final List<ModelDescription> subTypes;

    ModelBranchDescription(
        Class<?> type,
        Map<String, Property> properties,
        ModelDescription superDescription,
        List<ModelDescription> subDescriptions
    ) {
        this.type = type;
        this.properties = properties;
        this.superType = superDescription;
        this.subTypes = subDescriptions;
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
        onBranch.accept(superType, properties);
    }

    @Override
    public <T> T apply(Supplier<T> onBranch, Supplier<T> onLeaf) {
        return onBranch.get();
    }

    @Override
    public void traverse(
        OnBranch onBranch,
        OnEnumeration onEnumeration,
        Consumer<Class<?>> onLeaf
    ) throws IOException {
        onBranch.accept(type, superType, subTypes, properties);
        if (superType != null) {
            superType.traverse(onBranch, onEnumeration, onLeaf);
        }
        for (ModelDescription subType : subTypes) {
            subType.traverse(onBranch, onEnumeration, onLeaf);
        }
        for (Property property: properties.values()) {
            property.getDescription().traverse(onBranch, onEnumeration, onLeaf);
        }
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
        return "Singular{sort=branch,type=" + type.getTypeName() + "}";
    }
}
