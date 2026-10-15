package co.gentiac.authcore;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Verifica en CI el principio VII de la constitución y RNF-050:
 * las dependencias solo apuntan hacia adentro (adaptadores → aplicación → dominio).
 */
class ArquitecturaHexagonalTest {

    private static final String RAIZ = "co.gentiac.authcore";
    private static JavaClasses clases;

    @BeforeAll
    static void importar() {
        clases = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(RAIZ);
    }

    @Test
    void dominioNoDependeDeAplicacionNiInfraestructura() {
        noClasses().that().resideInAPackage(RAIZ + ".domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(RAIZ + ".application..", RAIZ + ".infrastructure..")
                .allowEmptyShould(true)
                .check(clases);
    }

    @Test
    void aplicacionNoDependeDeInfraestructura() {
        noClasses().that().resideInAPackage(RAIZ + ".application..")
                .should().dependOnClassesThat().resideInAPackage(RAIZ + ".infrastructure..")
                .allowEmptyShould(true)
                .check(clases);
    }

    @Test
    void dominioYAplicacionNoDependenDeFrameworks() {
        noClasses().that().resideInAnyPackage(RAIZ + ".domain..", RAIZ + ".application..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta..", "software.amazon..", "com.amazonaws..")
                .allowEmptyShould(true)
                .check(clases);
    }
}
