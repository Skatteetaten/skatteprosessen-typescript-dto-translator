package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.emitter;

import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.TypeName;
import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.model.ModelDescription;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImplodedInterfaceEmitter implements ModelDescription.OnBranch {
    
    private final Appendable appendable;
    private final Function<Class<?>, TypeName> naming;
    private final boolean export;
    private final String separator, prefix;
    private final boolean alwaysOptional;

    public ImplodedInterfaceEmitter(
        Appendable appendable,
        Function<Class<?>, TypeName> naming,
        boolean export,
        String separator,
        String prefix,
        boolean alwaysOptional
    ) {
        this.appendable = appendable;
        this.naming = naming;
        this.export = export;
        this.separator = separator;
        this.prefix = prefix;
        this.alwaysOptional = alwaysOptional;
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
        List<DelayedAppendable> appendables = new ArrayList<>();
        new ChainCollector(naming, appendables::add, alwaysOptional).onBranch(superType, properties);
        Iterator<DelayedAppendable> it = appendables.iterator();
        while (it.hasNext()) {
            DelayedAppendable delayed = it.next();
            appendable.append(indent).append("  ");
            delayed.accept(appendable, separator, prefix, name);
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

    interface DelayedAppendable {

        void accept(Appendable appendable, String separator, String prefix, TypeName host) throws IOException;
    }

    static class ChainCollector {

        private final Function<Class<?>, TypeName> naming;
        private final Consumer<DelayedAppendable> callback;
        private final boolean optional;
        private final List<String> names;
        private final Set<ModelDescription> chain;

        ChainCollector(Function<Class<?>, TypeName> naming, Consumer<DelayedAppendable> callback, boolean optional) {
            this.naming = naming;
            this.callback = callback;
            this.optional = optional;
            names = List.of();
            chain = Set.of();
        }

        private ChainCollector(
            Function<Class<?>, TypeName> naming,
            Consumer<DelayedAppendable> callback,
            boolean optional,
            List<String> names,
            Set<ModelDescription> chain
        ) {
            this.naming = naming;
            this.callback = callback;
            this.optional = optional;
            this.names = names;
            this.chain = chain;
        }

        void onBranch(ModelDescription superType, Map<String, ModelDescription.Property> properties) {
            for (Map.Entry<String, ModelDescription.Property> entry : properties.entrySet()) {
                if (entry.getValue().isArray()) {
                    callback.accept((appendable, separator, prefix, host) -> {
                        for (String name : names) {
                            appendable.append(name).append(separator);
                        }
                        appendable.append(entry.getKey());
                        if (entry.getValue().isOptional()) {
                            appendable.append("?");
                        }
                        appendable.append(": Array<").append(naming.apply(entry.getValue().getDescription().getType()).toQualifiedName(
                            entry.getValue().getDescription().apply(() -> prefix, () -> null),
                            host
                        )).append(">");
                    });
                } else {
                    Set<ModelDescription> chain = new HashSet<>(this.chain);
                    if (!chain.add(entry.getValue().getDescription())) {
                        throw new IllegalStateException("Cannot flatten recursive property"
                            + " for " + entry.getKey()
                            + " following " + names);
                    }
                    ChainCollector collector = new ChainCollector(
                        naming, callback, optional || entry.getValue().isOptional(),
                        Stream.concat(names.stream(), Stream.of(entry.getKey())).collect(Collectors.toList()),
                        chain
                    );
                    entry.getValue().getDescription().accept(
                        collector::onBranch,
                        enumerations -> collector.onNonBranch(entry.getValue().getDescription().getType()),
                        () -> collector.onNonBranch(entry.getValue().getDescription().getType())
                    );
                }
            }
        }

        void onNonBranch(Class<?> type) {
            callback.accept((appendable, separator, interfacePrefix, host) -> {
                Iterator<String> it = names.iterator();
                while (it.hasNext()) {
                    appendable.append(it.next());
                    if (it.hasNext()) {
                        appendable.append(separator);
                    }
                }
                if (optional) {
                    appendable.append("?");
                }
                appendable.append(": ").append(naming.apply(type).toQualifiedName(null, host));
            });
        }
    }
}
