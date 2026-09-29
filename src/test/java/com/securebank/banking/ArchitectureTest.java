package com.securebank.banking;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {
    private final com.tngtech.archunit.core.domain.JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.securebank.banking");

    @Test
    void excludesRemovedCloudInfrastructure() {
        noClasses().should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework.cloud..", "com.netflix.discovery..", "feign..")
                .check(classes);
    }

    @Test
    void mongodbModulesDoNotUseJpa() {
        noClasses().that().resideInAnyPackage("..payment..", "..notification..")
                .should().dependOnClassesThat().resideInAnyPackage("jakarta.persistence..")
                .check(classes);
    }
}
