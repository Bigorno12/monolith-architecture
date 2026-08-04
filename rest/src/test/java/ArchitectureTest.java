import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchitectureTest {

    private final JavaClasses allProjectClasses = new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_TESTS)
            .importPackages("mu.server");

    @Test
    void layer_checks_test() {
        Architectures.LayeredArchitecture layeredArchitecture = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .layer("Service").definedBy("..service..")
                .layer("Persistence").definedBy("..persistence..")
                .layer("Config").definedBy("..config..")
                .layer("Advice").definedBy("..advice..")
                .layer("Filter").definedBy("..filter..")
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Config", "Advice", "Filter")
                .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Config", "Controller");

        layeredArchitecture.check(allProjectClasses);
    }

    @Test
    void annotation_check_for_transactional_should_reside_in_service_layer_in_method() {
        methods().that().areDeclaredInClassesThat()
                .haveSimpleNameEndingWith("ServiceImpl")
                .and().arePublic()
                .and().haveNameNotMatching("^(fallback.*|authenticate)$")
                .should().beAnnotatedWith(Transactional.class)
                .check(allProjectClasses);
    }

    @Test
    void annotation_check_for_service_should_reside_in_service_module() {
        classes().that().areAnnotatedWith(Service.class)
                .or().haveNameMatching(".*Service")
                .should()
                .resideInAPackage("..service..")
                .check(allProjectClasses);
    }

    @Test
    void annotation_check_for_entity_repository_noRepositoryBean_should_reside_in_persistence_module() {
        classes().that().areAnnotatedWith(Entity.class)
                .or().haveNameMatching(".*Persistence")
                .should()
                .resideInAPackage("..persistence..")
                .orShould()
                .beAnnotatedWith(Repository.class)
                .orShould()
                .beAnnotatedWith(NoRepositoryBean.class)
                .check(allProjectClasses);
    }

    @Test
    void package_dependency_checks() {
        noClasses().that().resideInAnyPackage("..persistence..")
                .should().dependOnClassesThat().resideInAnyPackage("..service..")
                .andShould().dependOnClassesThat().resideInAnyPackage("..controller..")
                .check(allProjectClasses);
    }
}
