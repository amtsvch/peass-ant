package de.stro18.peass_ant.buildeditor.helper;

import de.dagere.peass.execution.utils.RequiredDependency;
import de.dagere.peass.testtransformation.JUnitVersions;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TransitiveDependencyFinder {

    public static final boolean jenkins = false;

    public static List<RequiredDependency> getAllTransitives(final JUnitVersions versions) {
        if (jenkins) {
            return getConstantDependencies();
        } else {
            return getTransitiveDependencies(versions);
        }
    }

    private static List<RequiredDependency> getTransitiveDependencies(final JUnitVersions versions) {
        List<RequiredDependency> requiredNonTransitiveDeps = RequiredDependency.getAll(versions);

        List<String> requiredNonTransitiveDepsAsStrings = new ArrayList<>();
        for (RequiredDependency dependency : requiredNonTransitiveDeps) {
            String mavenCoordinates = getMavenCoordinates(dependency); 
            requiredNonTransitiveDepsAsStrings.add(mavenCoordinates);
        }

        MavenCoordinate[] requiredTransitiveDepsAsCoordinates = Maven.resolver().resolve(requiredNonTransitiveDepsAsStrings.toArray(new String[]{}))
                .withTransitivity().as(MavenCoordinate.class);

        List<RequiredDependency> requiredTransitiveDeps = new LinkedList<>();
        for (MavenCoordinate coordinate : requiredTransitiveDepsAsCoordinates) {
            RequiredDependency dependency = getRequiredDependency(coordinate);
            requiredTransitiveDeps.add(dependency);
        }

        return requiredTransitiveDeps;
    }

    // Necessary because ShrinkWrap does not work with classloader of Jenkins
    private static List<RequiredDependency> getConstantDependencies() {
        List<RequiredDependency> requiredDependencies = new LinkedList<>();
        for (String dependencyStr : constantDependencies()) {
            String[] dependencyParts = dependencyStr.split(":");

            RequiredDependency dependency;
            if (dependencyParts.length == 5) {
                dependency = new RequiredDependency(dependencyParts[0],
                        dependencyParts[1], dependencyParts[4], null, dependencyParts[3]);
            } else {
                // dependencyParts.length = 4
                dependency = new RequiredDependency(dependencyParts[0],
                        dependencyParts[1], dependencyParts[3], null, null);
            }

            requiredDependencies.add(dependency);
        }

        return requiredDependencies;
    }


    private static String[] constantDependencies() {

        String[] dependencies = new String[]{
                "de.dagere.kopeme:kopeme-junit:jar:0.15.3",
                "de.dagere.kopeme:kopeme-core:jar:0.15.3",
                "org.aspectj:aspectjweaver:jar:1.9.7",
                "org.aspectj:aspectjrt:jar:1.9.7",
                "org.hamcrest:hamcrest:jar:2.2",
                "org.junit.vintage:junit-vintage-engine:jar:5.8.2",
                "org.junit.platform:junit-platform-engine:jar:1.8.2",
                "junit:junit:jar:4.13.2",
                "org.hamcrest:hamcrest-core:jar:1.3",
                "org.apiguardian:apiguardian-api:jar:1.1.2",
                "org.junit.jupiter:junit-jupiter-engine:jar:5.8.2",
                "org.junit.jupiter:junit-jupiter-api:jar:5.8.2",
                "org.opentest4j:opentest4j:jar:1.2.0",
                "org.junit.platform:junit-platform-commons:jar:1.8.2",
                "org.apache.commons:commons-math3:jar:3.6.1",
                "org.apache.logging.log4j:log4j-api:jar:2.14.1",
                "org.apache.logging.log4j:log4j-core:jar:2.14.1",
                "org.apache.logging.log4j:log4j-slf4j-impl:jar:2.14.1",
                "org.apache.maven:maven-model:jar:3.8.4",
                "org.codehaus.plexus:plexus-utils:jar:3.3.0",
                "org.javassist:javassist:jar:3.27.0-GA",
                "com.fasterxml.jackson.core:jackson-databind:jar:2.13.0",
                "com.fasterxml.jackson.core:jackson-annotations:jar:2.13.0",
                "com.fasterxml.jackson.core:jackson-core:jar:2.13.0",
                "org.glassfish.jaxb:jaxb-xjc:jar:2.3.5",
                "org.glassfish.jaxb:xsom:jar:2.3.5",
                "com.sun.xml.bind.external:relaxng-datatype:jar:2.3.5",
                "org.glassfish.jaxb:codemodel:jar:2.3.5",
                "com.sun.xml.bind.external:rngom:jar:2.3.5",
                "com.sun.xml.dtd-parser:dtd-parser:jar:1.4.5",
                "com.sun.istack:istack-commons-tools:jar:3.0.12",
                "org.glassfish.jaxb:jaxb-runtime:jar:2.3.5",
                "jakarta.xml.bind:jakarta.xml.bind-api:jar:2.3.3",
                "org.glassfish.jaxb:txw2:jar:2.3.5",
                "com.sun.istack:istack-commons-runtime:jar:3.0.12",
                "com.sun.activation:jakarta.activation:jar:1.2.2",
                "javax.activation:activation:jar:1.1.1",
                "com.github.javaparser:javaparser-core:jar:3.23.1",
                "net.kieker-monitoring:kieker:jar:1.15",
                "org.slf4j:slf4j-api:jar:1.7.30",
                "org.jctools:jctools-core:jar:3.3.0",
                "net.kieker-monitoring:kieker:jar:aspectj:1.15"
        };

        return dependencies;
    }
    
    private static String getMavenCoordinates(RequiredDependency dependency) {
        String dependencyStr;
        if (dependency.getClassifier() == null) {
            dependencyStr = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" +
                    dependency.getVersion();
        } else {
            dependencyStr = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + "jar" +
                    ":" + dependency.getClassifier() + ":" + dependency.getVersion();
        }
        
        return dependencyStr;
    }
    
    private static RequiredDependency getRequiredDependency(MavenCoordinate coordinate) {
        return new RequiredDependency(coordinate.getGroupId(),
                coordinate.getArtifactId(), coordinate.getVersion(), null,
                coordinate.getClassifier().isEmpty() ? null : coordinate.getClassifier());
    }
}
