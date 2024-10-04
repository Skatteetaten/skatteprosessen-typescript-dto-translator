package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.mavenplugin;

import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.JaxbStructuralResolver;
import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.SimpleStructuralResolver;
import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.StructuralResolver;
import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.StructuralTypeStructuralResolver;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;

public enum Resolver {
    SIMPLE {
        @Override
        StructuralResolver resolver(ClassLoader classLoader) {
            return new SimpleStructuralResolver();
        }
    },
    STRUCTURAL {
        @Override
        StructuralResolver resolver(ClassLoader classLoader) {
            return StructuralTypeStructuralResolver.of(classLoader);
        }
    },
    JAVAX {
        @Override
        StructuralResolver resolver(ClassLoader classLoader) {
            return JaxbStructuralResolver.ofJavax(classLoader);
        }
    },
    JAKARTA {
        @Override
        StructuralResolver resolver(ClassLoader classLoader) {
            return JaxbStructuralResolver.ofJakarta(classLoader);
        }
    },

    INDUCE {
        @Override
        StructuralResolver resolver(ClassLoader classLoader) {
            StructuralResolver resolver = new SimpleStructuralResolver();
            Class<? extends Annotation> structural = resolve("no.skatteetaten.fastsetting.formueinntekt.felles.structuraltype.api.CompoundOf", classLoader);
            Class<? extends Annotation> structuralEnum = resolve("no.skatteetaten.fastsetting.formueinntekt.felles.structuraltype.api.EnumerationOf", classLoader);
            StructuralResolver resolverStructural = structural == null || structuralEnum == null ? null : StructuralTypeStructuralResolver.of(classLoader);
            Class<? extends Annotation> javax = resolve("javax.xml.bind.annotation.XmlType", classLoader);
            Class<? extends Annotation> javaxEnum = resolve("javax.xml.bind.annotation.XmlEnum", classLoader);
            StructuralResolver resolverJavax = javax == null || javaxEnum == null ? null : JaxbStructuralResolver.ofJavax(classLoader);
            Class<? extends Annotation> jakarta = resolve("jakarta.xml.bind.annotation.XmlType", classLoader);
            Class<? extends Annotation> jakartaEnum = resolve("jakarta.xml.bind.annotation.XmlEnum", classLoader);
            StructuralResolver resolverJakarta = jakarta == null || jakartaEnum == null ? null : JaxbStructuralResolver.ofJakarta(classLoader);
            return new StructuralResolver() {
                @Override
                public Optional<Branch<?>> toBranch(Class<?> type) {
                    if (resolverStructural != null && type.isAnnotationPresent(structural)) {
                        return resolverStructural.toBranch(type);
                    } else if (resolverJavax != null && type.isAnnotationPresent(javax)) {
                        return resolverJavax.toBranch(type);
                    } else if (resolverJakarta != null && type.isAnnotationPresent(jakarta)) {
                        return resolverJakarta.toBranch(type);
                    } else {
                        return resolver.toBranch(type);
                    }
                }

                @Override
                public Map<String, String> toEnumerations(Class<? extends Enum<?>> type) {
                    if (resolverStructural != null && type.isAnnotationPresent(structuralEnum)) {
                        return resolverStructural.toEnumerations(type);
                    } else if (resolverJavax != null && type.isAnnotationPresent(javaxEnum)) {
                        return resolverJavax.toEnumerations(type);
                    } else if (resolverJakarta != null && type.isAnnotationPresent(jakartaEnum)) {
                        return resolverJakarta.toEnumerations(type);
                    } else {
                        return resolver.toEnumerations(type);
                    }
                }
            };
        }
    };

    @SuppressWarnings("unchecked")
    static Class<? extends Annotation> resolve(String type, ClassLoader classLoader) {
        try {
            return (Class<? extends Annotation>) Class.forName(type, false, classLoader);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    abstract StructuralResolver resolver(ClassLoader classLoader);
}
