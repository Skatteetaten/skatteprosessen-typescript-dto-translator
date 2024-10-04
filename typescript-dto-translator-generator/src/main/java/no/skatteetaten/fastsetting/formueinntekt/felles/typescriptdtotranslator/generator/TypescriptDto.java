package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator;

import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.emitter.*;
import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.model.ModelDescription;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;

public class TypescriptDto {

    private final StructuralResolver structuralResolver;
    private final BiPredicate<Class<?>, String> condition;
    private final Function<Class<?>, TypeName> namingStrategy;
    private final Function<Class<?>, Optional<TypeName>> typeResolver;
    private final TypeName fallbackType;
    private final boolean retainFalse, retainZero, retainEmptyString;
    private final boolean export;
    private final Implosion implosion;
    private final String separator, implodedInterfacePrefix, explodedInterfacePrefix, implodedFunctionPrefix, explodedFunctionPrefix;

    public TypescriptDto() {
        structuralResolver = new SimpleStructuralResolver();
        condition = (type, name) -> true;
        namingStrategy = new SimpleNameNamingStrategy();
        typeResolver = new DefaultTypeResolver();
        fallbackType = TypeName.ANY;
        retainFalse = false;
        retainZero = false;
        retainEmptyString = false;
        export = true;
        implosion = Implosion.NONE;
        separator = "_";
        implodedInterfacePrefix = "Imploded_";
        explodedInterfacePrefix = null;
        implodedFunctionPrefix = "implode_";
        explodedFunctionPrefix = "explode_";
    }

    private TypescriptDto(
        StructuralResolver structuralResolver,
        BiPredicate<Class<?>, String> condition,
        Function<Class<?>, TypeName> namingStrategy,
        Function<Class<?>, Optional<TypeName>> typeResolver,
        TypeName fallbackType,
        boolean retainFalse,
        boolean retainZero,
        boolean retainEmptyString,
        boolean export,
        Implosion implosion,
        String separator,
        String implodedInterfacePrefix,
        String explodedInterfacePrefix,
        String implodedFunctionPrefix,
        String explodedFunctionPrefix
    ) {
        this.structuralResolver = structuralResolver;
        this.condition = condition;
        this.namingStrategy = namingStrategy;
        this.typeResolver = typeResolver;
        this.fallbackType = fallbackType;
        this.retainFalse = retainFalse;
        this.retainZero = retainZero;
        this.retainEmptyString = retainEmptyString;
        this.export = export;
        this.implosion = implosion;
        this.separator = separator;
        this.implodedInterfacePrefix = implodedInterfacePrefix;
        this.explodedInterfacePrefix = explodedInterfacePrefix;
        this.implodedFunctionPrefix = implodedFunctionPrefix;
        this.explodedFunctionPrefix = explodedFunctionPrefix;
    }

    public TypescriptDto withStructuralResolver(StructuralResolver structuralResolver) {
        return new TypescriptDto(
                structuralResolver,
                condition,
                namingStrategy,
                typeResolver,
                fallbackType,
                retainFalse,
                retainZero,
                retainEmptyString,
                export,
                implosion,
                separator,
                implodedInterfacePrefix,
                explodedInterfacePrefix,
                implodedFunctionPrefix,
                explodedFunctionPrefix
        );
    }

    public TypescriptDto withCondition(BiPredicate<Class<?>, String> condition) {
        return new TypescriptDto(
            structuralResolver,
            condition,
            namingStrategy,
            typeResolver,
            fallbackType,
            retainFalse,
            retainZero,
            retainEmptyString,
            export,
            implosion,
            separator,
            implodedInterfacePrefix,
            explodedInterfacePrefix,
            implodedFunctionPrefix,
            explodedFunctionPrefix
        );
    }

    public TypescriptDto withNamingStrategy(Function<Class<?>, TypeName> namingStrategy) {
        return new TypescriptDto(
            structuralResolver,
            condition,
            namingStrategy,
            typeResolver,
            fallbackType,
            retainFalse,
            retainZero,
            retainEmptyString,
            export,
            implosion,
            separator,
            implodedInterfacePrefix,
            explodedInterfacePrefix,
            implodedFunctionPrefix,
            explodedFunctionPrefix
        );
    }

    public TypescriptDto withTypeResolver(Function<Class<?>, Optional<TypeName>> typeResolver) {
        return new TypescriptDto(
            structuralResolver,
            condition,
            namingStrategy,
            typeResolver,
            fallbackType,
            retainFalse,
            retainZero,
            retainEmptyString,
            export,
            implosion,
            separator,
            implodedInterfacePrefix,
            explodedInterfacePrefix,
            implodedFunctionPrefix,
            explodedFunctionPrefix
        );
    }

    public TypescriptDto withFallbackType(TypeName fallbackType) {
        return new TypescriptDto(
            structuralResolver,
            condition,
            namingStrategy,
            typeResolver,
            fallbackType,
            retainFalse,
            retainZero,
            retainEmptyString,
            export,
            implosion,
            separator,
            implodedInterfacePrefix,
            explodedInterfacePrefix,
            implodedFunctionPrefix,
            explodedFunctionPrefix
        );
    }

    public TypescriptDto withRetention(boolean retainFalse, boolean retainZero, boolean retainEmptyString) {
        return new TypescriptDto(
            structuralResolver,
            condition,
            namingStrategy,
            typeResolver,
            fallbackType,
            retainFalse,
            retainZero,
            retainEmptyString,
            export,
            implosion,
            separator,
            implodedInterfacePrefix,
            explodedInterfacePrefix,
            implodedFunctionPrefix,
            explodedFunctionPrefix
        );
    }

    public TypescriptDto withExport(boolean export) {
        return new TypescriptDto(
            structuralResolver,
            condition,
            namingStrategy,
            typeResolver,
            fallbackType,
            retainFalse,
            retainZero,
            retainEmptyString,
            export,
            implosion,
            separator,
            implodedInterfacePrefix,
            explodedInterfacePrefix,
            implodedFunctionPrefix,
            explodedFunctionPrefix
        );
    }

    public TypescriptDto withImplosion(Implosion implosion) {
        return new TypescriptDto(
            structuralResolver,
            condition,
            namingStrategy,
            typeResolver,
            fallbackType,
            retainFalse,
            retainZero,
            retainEmptyString,
            export,
            implosion,
            separator,
            implodedInterfacePrefix,
            explodedInterfacePrefix,
            implodedFunctionPrefix,
            explodedFunctionPrefix
        );
    }

    public TypescriptDto withImplosion(
        Implosion implosion,
        String separator,
        String implodedInterfacePrefix,
        String explodedInterfacePrefix,
        String implodedFunctionPrefix,
        String explodedFunctionPrefix
    ) {
        return new TypescriptDto(
            structuralResolver,
            condition,
            namingStrategy,
            typeResolver,
            fallbackType,
            retainFalse,
            retainZero,
            retainEmptyString,
            export,
            implosion,
            separator,
            implodedInterfacePrefix,
            explodedInterfacePrefix,
            implodedFunctionPrefix,
            explodedFunctionPrefix
        );
    }

    public String make(Class<?>... types) {
        return make(List.of(types));
    }

    public String make(List<Class<?>> types) {
        StringBuilder sb = new StringBuilder();
        try {
            make(sb, types);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return sb.toString();
    }

    public void make(Appendable appendable, Class<?>... types) throws IOException {
        make(appendable, List.of(types));
    }

    public void make(Appendable appendable, List<Class<?>> types) throws IOException {
        List<ModelDescription> descriptions = ModelDescription.of(
            structuralResolver,
            condition,
            types
        );
        Map<Class<?>, TypeName> names = new HashMap<>();
        for (ModelDescription description : descriptions) {
            Function<Class<?>, TypeName> namingStrategy = new DuplicationAvoidingNamingStrategy(this.namingStrategy);
            description.traverse(
                (type, superType, subTypes, properties) -> names.put(type, namingStrategy.apply(type)),
                (type, enumerations) -> names.put(type, namingStrategy.apply(type)),
                type -> names.put(type, typeResolver.apply(type).orElse(fallbackType))
            );
        }
        for (ModelDescription description : descriptions) {
            description.traverse(
                new ExplodedInterfaceEmitter(appendable, names::get, export, explodedInterfacePrefix),
                new EnumerationEmitter(appendable, names::get, export),
                type -> { }
            );
        }
        if (implosion.isImplode()) {
            for (ModelDescription description : descriptions) {
                description.traverse(new ImplodedInterfaceEmitter(
                    appendable,
                    names::get,
                    export,
                    separator,
                        implodedInterfacePrefix,
                    implosion.isAlwaysOptional()
                ), (type, enumerations) -> { }, type -> { });
            }
            for (ModelDescription description : descriptions) {
                description.traverse(new ImplosionEmitter(
                    appendable,
                    names::get,
                    export,
                    separator,
                    implodedInterfacePrefix,
                    explodedInterfacePrefix,
                    implodedFunctionPrefix
                ), (type, enumerations) -> { }, type -> { });
            }
            for (ModelDescription description : descriptions) {
                description.traverse(new ExplosionEmitter(
                    appendable,
                    names::get,
                    export,
                    separator,
                    implodedInterfacePrefix,
                    explodedInterfacePrefix,
                    explodedFunctionPrefix,
                    retainFalse,
                    retainZero,
                    retainEmptyString
                ), (type, enumerations) -> { }, type -> { });
            }
        }
    }

    static class DuplicationAvoidingNamingStrategy implements Function<Class<?>, TypeName> {

        private final Function<Class<?>, TypeName> delegate;

        private final Set<TypeName> names = new HashSet<>();

        DuplicationAvoidingNamingStrategy(Function<Class<?>, TypeName> delegate) {
            this.delegate = delegate;
        }

        @Override
        public TypeName apply(Class<?> type) {
            TypeName name = delegate.apply(type);
            if (!names.add(name)) {
                throw new IllegalArgumentException("Duplicate name: " + name);
            }
            return name;
        }
    }
}
