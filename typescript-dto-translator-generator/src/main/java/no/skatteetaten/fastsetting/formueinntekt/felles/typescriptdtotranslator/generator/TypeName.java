package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator;

import java.util.Objects;
import java.util.Optional;

public class TypeName {

    public static final TypeName
            ANY = new TypeName("any"),
            STRING = new TypeName("string"),
            BOOLEAN = new TypeName("boolean"),
            BIGINT = new TypeName("bigint"),
            NUMBER = new TypeName("number");

    private final String module, name;

    public TypeName(String name) {
        module = null;
        this.name = name;
    }

    public TypeName(String module, String name) {
        this.module = module;
        this.name = name;
    }

    public Optional<String> getModule() {
        return Optional.ofNullable(module);
    }

    public String getName() {
        return name;
    }

    public String toUnqualifiedName(String prefix) {
        return prefix == null ? this.name : (prefix + this.name);
    }

    public String toQualifiedName(String prefix, TypeName host) {
        String name = prefix == null ? this.name : (prefix + this.name);
        return module == null || module.equals(host.getModule().orElse(""))
            ? name
            : (module + "." + name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeName typeName = (TypeName) o;
        return Objects.equals(module, typeName.module) && Objects.equals(name, typeName.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, name);
    }

    @Override
    public String toString() {
        return "TypeName{" +
                "namespace='" + module + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
