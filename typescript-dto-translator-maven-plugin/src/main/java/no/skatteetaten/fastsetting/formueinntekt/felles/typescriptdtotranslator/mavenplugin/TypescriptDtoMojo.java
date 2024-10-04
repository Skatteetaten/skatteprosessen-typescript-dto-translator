package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.mavenplugin;

import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.Implosion;
import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.TypeName;
import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.generator.TypescriptDto;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mojo(name = "typescript-dto",
    defaultPhase = LifecyclePhase.PROCESS_CLASSES,
    threadSafe = true,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class TypescriptDtoMojo extends AbstractMojo {

    private static final String NULL = "null";

    @Parameter(defaultValue = "${project}", readonly = true)
    public MavenProject project;

    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}", readonly = true)
    public String classes;

    @Parameter(defaultValue = "${project.artifactId}", required = true)
    public String name;

    @Parameter(defaultValue = "${project.version}", required = true)
    public String version;

    @Parameter(defaultValue = "^5.3.3", required = true)
    public String typescript;

    @Parameter(required = true, defaultValue = "${project.build.directory}/typescript-dto")
    public String target;

    @Parameter(required = true, defaultValue = "true")
    public boolean export;

    @Parameter(required = true, defaultValue = "OPTIONAL")
    public Implosion implosion;

    @Parameter(required = true, defaultValue = "_")
    public String separator;

    @Parameter(required = true, defaultValue = "Imploded_")
    public String implodedInterfacePrefix;

    @Parameter(required = true, defaultValue = NULL)
    public String explodedInterfacePrefix;

    @Parameter(required = true, defaultValue = "implode_")
    public String implodedFunctionPrefix;

    @Parameter(required = true, defaultValue = "explode_")
    public String explodedFunctionPrefix;

    @Parameter
    public Resolver resolver;

    @Parameter(required = true, defaultValue = "false")
    public boolean retainFalse;

    @Parameter(required = true, defaultValue = "false")
    public boolean retainZero;

    @Parameter(required = true, defaultValue = "false")
    public boolean retainEmptyString;

    @Parameter
    public List<String> imports;

    @Parameter
    public List<String> types;

    @Parameter
    public String defaultNamespace;

    @Parameter
    public Map<String, String> namespaces;

    @Parameter
    public String registry;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (types.isEmpty()) {
            getLog().warn("No type definitions found - skipping execution");
            return;
        }
        File classes = new File(this.classes), sources = new File(this.target);
        if (!classes.isDirectory() && !classes.mkdirs()) {
            throw new MojoFailureException("Not a directory: " + classes.getAbsolutePath());
        } else if (!sources.isDirectory() && !sources.mkdirs()) {
            throw new MojoFailureException("Not a directory: " + sources.getAbsolutePath());
        }
        List<String> elements;
        try {
            elements = new ArrayList<>(project.getCompileClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("Could not resolve compile class path", e);
        }
        getLog().debug("Resolving class path: " + elements);
        List<URL> classPath = new ArrayList<>();
        for (String element : elements) {
            File file = new File(element);
            if (!file.exists()) {
                throw new MojoExecutionException("Class path element does not exist: " + element);
            }
            try {
                classPath.add(file.toURI().toURL());
            } catch (Exception e) {
                throw new MojoExecutionException("Could not resolve class path element: " + element, e);
            }
        }
        try (URLClassLoader classLoader = new URLClassLoader(
            classPath.toArray(URL[]::new),
            ClassLoader.getPlatformClassLoader()
        )) {
            TypescriptDto typescriptDto = new TypescriptDto()
                .withExport(export)
                .withRetention(retainFalse, retainZero, retainEmptyString)
                .withImplosion(
                    implosion,
                    separator,
                    implodedInterfacePrefix.equals(NULL) ? null : implodedInterfacePrefix,
                    explodedInterfacePrefix.equals(NULL) ? null : explodedInterfacePrefix,
                    implodedFunctionPrefix.equals(NULL) ? null : implodedFunctionPrefix,
                    explodedFunctionPrefix.equals(NULL) ? null : explodedFunctionPrefix
                );
            if (resolver != null) {
                typescriptDto = typescriptDto.withStructuralResolver(resolver.resolver(classLoader));
            }
            try (Writer writer = Files.newBufferedWriter(sources.toPath().resolve("package.json"))) {
                writer.append("{\n")
                    .append("  \"name\": \"").append(name).append("\",\n")
                    .append("  \"version\": \"").append(version.endsWith("-SNAPSHOT")
                            ? ("0.0.0-" + version.replaceAll("[^a-zA-Z0-9]", "-"))
                            : version).append("\",\n")
                    .append("  \"types\": \"index.d.ts\",\n")
                    .append("  \"main\": \"index.js\",\n")
                    .append("  \"devDependencies\": {\n")
                    .append("    \"typescript\": \"").append(typescript).append("\"\n")
                    .append("  },\n")
                    .append("  \"scripts\": {\n")
                    .append("    \"prepack\": \"npx tsc --module umd --moduleResolution node --typeRoots --target es5 --lib es6 --declaration --sourceMap index.ts\"\n")
                    .append("  },\n")
                    .append("  \"files\": [\n")
                    .append("    \"package.json\",\n")
                    .append("    \"index.js\",\n")
                    .append("    \"index.d.ts\",\n")
                    .append("    \"index.js.map\"\n")
                    .append("  ]");
                if (registry != null) {
                    writer.append(",\n")
                        .append("  \"publishConfig\": {\n")
                        .append("    \"registry\": \"").append(registry).append("\"\n")
                        .append("  }");
                }
                writer.append("\n}\n");
            }
            try (Writer writer = Files.newBufferedWriter(sources.toPath().resolve("index.ts"))) {
                if (imports != null) {
                    for (String value : imports) {
                        writer.append("import * from \"").append(value).append("\";\n");
                    }
                }
                typescriptDto.withNamingStrategy(type -> {
                    if (defaultNamespace != null && defaultNamespace.equals(type.getPackageName())) {
                        return new TypeName(type.getSimpleName());
                    }
                    String namespace = namespaces == null ? null : namespaces.get(type.getPackageName());
                    return new TypeName(namespace == null ? type.getPackageName() : namespace, type.getSimpleName());
                }).make(writer, types.stream().map(type -> {
                    try {
                        return Class.forName(type, false, classLoader);
                    } catch (ClassNotFoundException e) {
                        throw new IllegalArgumentException("Cannot find type " + type, e);
                    }
                }).collect(Collectors.toList()));
            }
        } catch (IOException e) {
            throw new MojoFailureException("Failed to close class loader", e);
        }
    }
}
