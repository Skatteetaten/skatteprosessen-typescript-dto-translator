package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.emitter;

import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.TypeName;
import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.model.ModelDescription;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExplosionEmitter implements ModelDescription.OnBranch {

    private final Appendable appendable;
    private final Function<Class<?>, TypeName> naming;
    private final boolean export;
    private final String separator, implodedInterfacePrefix, explodedInterfacePrefix, functionPrefix;
    private final boolean retainFalse, retainZero, retainEmptyString;

    public ExplosionEmitter(
        Appendable appendable,
        Function<Class<?>, TypeName> naming,
        boolean export,
        String separator,
        String implodedInterfacePrefix,
        String explodedInterfacePrefix,
        String functionPrefix,
        boolean retainFalse,
        boolean retainZero,
        boolean retainEmptyString
    ) {
        this.appendable = appendable;
        this.naming = naming;
        this.export = export;
        this.separator = separator;
        this.implodedInterfacePrefix = implodedInterfacePrefix;
        this.explodedInterfacePrefix = explodedInterfacePrefix;
        this.functionPrefix = functionPrefix;
        this.retainFalse = retainFalse;
        this.retainZero = retainZero;
        this.retainEmptyString = retainEmptyString;
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
        appendable.append("function ")
            .append(name.toUnqualifiedName(functionPrefix))
            .append("(input: ")
            .append(name.toUnqualifiedName(implodedInterfacePrefix))
            .append(", output: any = { }): ")
            .append(name.toUnqualifiedName(explodedInterfacePrefix))
            .append(" {\n");
        if (superType != null) {
            appendable.append(indent)
                .append("  ")
                .append(naming.apply(superType.getType()).toQualifiedName(functionPrefix, name))
                .append("(input, output);");
        }
        List<DelayedAppendable> appendables = new ArrayList<>();
        new ChainCollector(naming, appendables::add).onBranch(superType, properties);
        for (DelayedAppendable delayed : appendables) {
            delayed.accept(appendable, separator, functionPrefix, indent, name, retainFalse, retainZero, retainEmptyString);
            appendable.append("\n");
        }
        appendable.append(indent)
            .append("  return output as ")
            .append(name.toUnqualifiedName(explodedInterfacePrefix))
            .append(";\n")
            .append(indent)
            .append("}\n");
        if (name.getModule().isPresent()) {
            appendable.append("}\n");
        }
    }

    interface DelayedAppendable {

        void accept(
            Appendable appendable, String separator, String functionPrefix, String indent, TypeName host,
            boolean retainFalse, boolean retainZero, boolean retainEmptyString
        ) throws IOException;
    }

    static class ChainCollector {

        private final Function<Class<?>, TypeName> naming;
        private final Consumer<DelayedAppendable> callback;
        private final List<String> names;
        private final Set<ModelDescription> chain;

        ChainCollector(
            Function<Class<?>, TypeName> naming,
            Consumer<DelayedAppendable> callback
        ) {
            this.naming = naming;
            this.callback = callback;
            names = List.of();
            chain = Set.of();
        }

        private ChainCollector(
            Function<Class<?>, TypeName> naming,
            Consumer<DelayedAppendable> callback,
            List<String> names,
            Set<ModelDescription> chain
        ) {
            this.naming = naming;
            this.callback = callback;
            this.names = names;
            this.chain = chain;
        }

        void onBranch(ModelDescription superType, Map<String, ModelDescription.Property> properties) {
            for (Map.Entry<String, ModelDescription.Property> entry : properties.entrySet()) {
                if (entry.getValue().isArray()) {
                    callback.accept((appendable, separator, functionPrefix, indent, host, retainFalse, retainZero, retainEmptyString) -> {
                        appendable.append(indent).append("  if (input");
                        CharSequence current = ".";
                        for (String name : names) {
                            appendable.append(current).append(name);
                            current = separator;
                        }
                        appendable.append(current).append(entry.getKey());
                        appendable.append(") {\n");
                        for (int i = 0; i < names.size(); i++) {
                            appendable.append(indent).append("    if (").append("output");
                            for (int j = 0; j <= i; j++) {
                                appendable.append(".").append(names.get(j));
                            }
                            appendable.append(" == null) { output");
                            for (int j = 0; j <= i; j++) {
                                appendable.append(".").append(names.get(j));
                            }
                            appendable.append(" = { }; }\n");
                        }
                        appendable.append(indent).append("    ").append("output");
                        for (String name : names) {
                            appendable.append(".").append(name);
                        }
                        appendable.append(".").append(entry.getKey()).append(" = input");
                        current = ".";
                        for (String name : names) {
                            appendable.append(current).append(name);
                            current = separator;
                        }
                        appendable.append(current).append(entry.getKey());
                        entry.getValue().getDescription().accept((ignored, ignored2) -> {
                            try {
                                appendable.append(".map((it) => ")
                                    .append(naming.apply(entry.getValue().getDescription().getType()).toQualifiedName(functionPrefix, host))
                                    .append("(it))");
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }, enumerations -> { }, () -> { });
                        appendable.append(";\n").append(indent).append("  }");
                    });
                } else {
                    Set<ModelDescription> chain = new HashSet<>(this.chain);
                    if (!chain.add(entry.getValue().getDescription())) {
                        throw new IllegalStateException("Cannot flatten recursive property"
                                + " for " + entry.getKey()
                                + " following " + names);
                    }
                    ChainCollector collector = new ChainCollector(naming, callback, Stream.concat(
                        names.stream(), Stream.of(entry.getKey())
                    ).collect(Collectors.toList()), chain);
                    entry.getValue().getDescription().accept(
                        collector::onBranch,
                        enumerations -> collector.onNonBranch(entry.getValue().getDescription().getType()),
                        () -> collector.onNonBranch(entry.getValue().getDescription().getType())
                    );
                }
            }
        }

        private void onNonBranch(Class<?> type) {
            callback.accept((appendable, separator, functionPrefix, indent, host, retainFalse, retainZero, retainEmptyString) -> {
                appendable.append(indent).append("  if (input");
                if (retainFalse && naming.apply(type).equals(TypeName.BOOLEAN)) {
                    CharSequence current = ".";
                    for (String name : names) {
                        appendable.append(current).append(name);
                        current = separator;
                    }
                    appendable.append(" === false || input");
                } else if (retainZero && naming.apply(type).equals(TypeName.NUMBER)) {
                    CharSequence current = ".";
                    for (String name : names) {
                        appendable.append(current).append(name);
                        current = separator;
                    }
                    appendable.append(" === 0 || input");
                } else if (retainEmptyString && naming.apply(type).equals(TypeName.STRING)) {
                    CharSequence current = ".";
                    for (String name : names) {
                        appendable.append(current).append(name);
                        current = separator;
                    }
                    appendable.append(" === '' || input");
                }
                CharSequence current = ".";
                for (String name : names) {
                    appendable.append(current).append(name);
                    current = separator;
                }
                appendable.append(") {\n");
                for (int i = 1; i < names.size(); i++) {
                    appendable.append(indent).append("    if (").append("output");
                    for (int j = 0; j < i; j++) {
                        appendable.append(".").append(names.get(j));
                    }
                    appendable.append(" == null) { output");
                    for (int j = 0; j < i; j++) {
                        appendable.append(".").append(names.get(j));
                    }
                    appendable.append(" = { }; }\n");
                }
                appendable.append(indent).append("    ").append("output");
                for (String name : names) {
                    appendable.append(".").append(name);
                }
                appendable.append(" = input");
                current = ".";
                for (String name : names) {
                    appendable.append(current).append(name);
                    current = separator;
                }
                appendable.append(";\n").append(indent).append("  }");
            });
        }
    }
}
