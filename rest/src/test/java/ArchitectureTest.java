import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

@AnalyzeClasses(packages = "mu.server", importOptions = {ImportOption.DoNotIncludeTests.class})
public class ArchitectureTest {

    private static final String ENUMERATION_PACKAGE = "..persistence.enumeration..";

    @ArchTest
    static final ArchRule layer_checks_test = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Persistence").definedBy(resideInAPackage("..persistence..").and(not(resideInAPackage(ENUMERATION_PACKAGE))))
            .layer("Enumeration").definedBy(ENUMERATION_PACKAGE)
            .layer("Config").definedBy("..config..")
            .layer("Advice").definedBy("..advice..")
            .layer("Filter").definedBy("..filter..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Config", "Advice", "Filter")
            .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Config", "Controller")
            .whereLayer("Enumeration").mayOnlyBeAccessedByLayers("Persistence", "Service", "Controller", "Config", "Advice", "Filter");


    @ArchTest
    static final ArchRule annotation_check_for_transactional_should_reside_in_service_layer_in_method =
            methods().that().areDeclaredInClassesThat()
                    .haveSimpleNameEndingWith("ServiceImpl")
                    .and().arePublic()
                    .and().haveNameNotMatching("^(fallback.*|authenticate)$")
                    .should().beAnnotatedWith(Transactional.class);

    @ArchTest
    static final ArchRule annotation_check_for_service_should_reside_in_service_module =
            classes().that().areAnnotatedWith(Service.class)
                    .or().haveNameMatching(".*Service")
                    .should()
                    .resideInAPackage("..service..");

    @ArchTest
    static final ArchRule annotation_check_for_entity_repository_noRepositoryBean_should_reside_in_persistence_module =
            classes().that().areAnnotatedWith(Entity.class)
                    .or().haveNameMatching(".*Persistence")
                    .should()
                    .resideInAPackage("..persistence..")
                    .orShould()
                    .beAnnotatedWith(Repository.class)
                    .orShould()
                    .beAnnotatedWith(NoRepositoryBean.class);

    @ArchTest
    static final ArchRule package_dependency_checks =
            noClasses().that().resideInAnyPackage("..persistence..")
                    .should().dependOnClassesThat().resideInAnyPackage("..service..")
                    .andShould().dependOnClassesThat().resideInAnyPackage("..controller..");

    @ArchTest
    static final ArchRule persistence_enums_should_all_live_in_the_shared_enumeration_package =
            classes().that().resideInAPackage("..persistence..")
                    .and().areEnums()
                    .should().resideInAPackage(ENUMERATION_PACKAGE)
                    .because("""
                            the enumeration package is the only slice of persistence the service and rest modules may import,
                            so an enum outside it is unreachable from the upper layers
                            """
                    );

    @ArchTest
    static final ArchRule controllers_should_be_in_controller_package =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Controller")
                    .should()
                    .resideInAPackage("..controller..")
                    .orShould()
                    .beAnnotatedWith(RestController.class);

    @ArchTest
    static final ArchRule shared_enumeration_package_should_not_drag_in_the_rest_of_persistence =
            noClasses().that().resideInAPackage(ENUMERATION_PACKAGE)
                    .should().dependOnClassesThat(
                            resideInAPackage("..persistence..").and(not(resideInAPackage(ENUMERATION_PACKAGE))))
                    .because("""
                            the shared enums must stay a leaf: depending on entities or repositories would leak them
                            into the service and rest modules through the back door
                            """
                    );

    @ArchTest
    static final ArchRule field_injection_should_not_be_used = NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
}
