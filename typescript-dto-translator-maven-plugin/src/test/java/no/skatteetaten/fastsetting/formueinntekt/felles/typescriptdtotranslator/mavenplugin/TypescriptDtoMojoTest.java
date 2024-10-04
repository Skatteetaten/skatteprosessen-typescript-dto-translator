package no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.mavenplugin;

import no.skatteetaten.fastsetting.formueinntekt.felles.typescriptdtotranslator.mavenplugin.sample.SampleType;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class TypescriptDtoMojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public MojoRule mojoRule = new MojoRule();

    @Test
    public void can_apply_mojo() throws Exception {
        Path root = temporaryFolder.newFolder().toPath();
        Files.copy(TypescriptDtoMojoTest.class.getResourceAsStream("/pom.xml"), root.resolve("pom.xml"));
        includeClassFile(root, SampleType.class);
        TypescriptDtoMojo mojo = (TypescriptDtoMojo) mojoRule.lookupConfiguredMojo(root.toFile(), "typescript-dto");
        mojo.execute();
        assertThat(root.resolve("target/typescript-dto")).isDirectoryContaining(path -> path.getFileName().toString().equals("index.ts"));
        assertThat(root.resolve("target/typescript-dto")).isDirectoryContaining(path -> path.getFileName().toString().equals("package.json"));
    }

    private static void includeClassFile(Path root, Class<?> type) throws IOException {
        Path file = root.resolve("target/classes").resolve(type.getName().replace('.', '/') + ".class");
        Files.createDirectories(file.getParent());
        Files.copy(type.getResourceAsStream(type.getSimpleName() + ".class"), file);
    }
}
