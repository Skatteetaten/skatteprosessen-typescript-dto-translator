package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.emitter;

import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.TypeName;
import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.model.ModelDescription;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ExplodedInterfaceEmitter implements ModelDescription.OnBranch {

    private final Appendable appendable;
    private final Function<Class<?>, TypeName> naming;
    private final boolean export;
    private final String prefix;

    public ExplodedInterfaceEmitter(
        Appendable appendable,
        Function<Class<?>, TypeName> naming,
        boolean export,
        String prefix
    ) {
        this.appendable = appendable;
        this.naming = naming;
        this.export = export;
        this.prefix = prefix;
    }

    @Override
    public void accept(
        Class<?> type,
        ModelDescription superType,
        List<ModelDescription> subTypes,
        Map<String, ModelDescription.Property> properties
    ) throws IOException {
        if (export) {
            appendable.append("export ");
        }
        TypeName name = naming.apply(type);
        String indent;
        if (name.getModule().isPresent()) {
            indent = "  ";
            appendable.append("module ")
                .append(name.getModule().orElseThrow())
                .append(" {\n");
        } else {
            indent = "";
        }
        appendable.append(indent);
        if (name.getModule().isPresent()) {
            appendable.append("export ");
        }
        appendable.append("interface ").append(name.toUnqualifiedName(prefix));
        if (superType != null) {
            appendable.append(" extends ").append(naming.apply(superType.getType()).toQualifiedName(prefix, name));
        }
        appendable.append(" {\n");
        Iterator<Map.Entry<String, ModelDescription.Property>> it = properties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ModelDescription.Property> entry = it.next();
            appendable.append(indent).append("  ").append(entry.getKey());
            if (entry.getValue().isOptional()) {
                appendable.append("?");
            }
            appendable.append(": ");
            if (entry.getValue().isArray()) {
                appendable.append("Array<");
            }
            appendable.append(naming.apply(entry.getValue().getDescription().getType()).toQualifiedName(
                entry.getValue().getDescription().apply(() -> prefix, () -> null),
                name
            ));
            if (entry.getValue().isArray()) {
                appendable.append(">");
            }
            if (it.hasNext()) {
                appendable.append(",");
            }
            appendable.append("\n");
        }
        appendable.append(indent).append("}\n");
        if (name.getModule().isPresent()) {
            appendable.append("}\n");
        }
    }
}
