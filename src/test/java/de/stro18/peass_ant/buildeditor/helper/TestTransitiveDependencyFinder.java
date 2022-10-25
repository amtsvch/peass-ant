package de.stro18.peass_ant.buildeditor.helper;

import de.dagere.peass.execution.utils.RequiredDependency;
import de.dagere.peass.testtransformation.JUnitVersions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TestTransitiveDependencyFinder {
    
    @Test
    public void testNonTransitiveDependency() {
        Assertions.assertTrue(dependencyFound("kieker"));
    }
    
    @Test
    public void testTransitiveDependency() {
        Assertions.assertTrue(dependencyFound("maven-model"));
    }

    //TODO: check previous implementation of getAllTransitives with boolean parameters (here was false)
    private boolean dependencyFound(String artifactId) {
        JUnitVersions juv = new JUnitVersions();
        juv.setJunit4(true);
        juv.setJunit5(true);
        List<RequiredDependency> requiredDependencies = TransitiveDependencyFinder.getAllTransitives(juv);

        for (RequiredDependency dependency : requiredDependencies) {
            if (dependency.getArtifactId().equals(artifactId)) {
                return true;
            }
        }
        
        return false;
    }
}
