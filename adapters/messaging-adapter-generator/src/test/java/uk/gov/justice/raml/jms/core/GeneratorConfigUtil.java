package uk.gov.justice.raml.jms.core;

import uk.gov.justice.raml.core.GeneratorConfig;

import org.junit.rules.TemporaryFolder;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GeneratorConfigUtil {

    public static GeneratorConfig configurationWithBasePackage(String basePackageName, TemporaryFolder outputFolder) {
        Path outputPath = Paths.get(outputFolder.getRoot().getAbsolutePath());
        return new GeneratorConfig(outputPath, outputPath, basePackageName);
    }

}
