package de.stro18.peass_ant.executor;

import de.dagere.peass.config.MeasurementStrategy;
import de.dagere.peass.dependency.analysis.testData.TestMethodCall;
import de.dagere.peass.execution.utils.EnvironmentVariables;
import de.dagere.peass.execution.utils.KoPeMeExecutor;
import de.dagere.peass.execution.utils.ProjectModules;
import de.dagere.peass.folders.PeassFolders;
import de.dagere.peass.execution.processutils.ProcessBuilderHelper;
import de.dagere.peass.execution.processutils.ProcessSuccessTester;
import de.dagere.peass.testtransformation.JUnitTestShortener;
import de.dagere.peass.testtransformation.JUnitTestTransformer;
import de.stro18.peass_ant.buildeditor.AntBuildEditor;
import de.stro18.peass_ant.buildeditor.helper.AntArgLineBuilder;
import de.stro18.peass_ant.buildeditor.tomcat.TomcatBuildEditor;
import de.stro18.peass_ant.utils.AntModuleUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AntTestExecutor extends KoPeMeExecutor {

    private static final Logger LOG = LogManager.getLogger(AntTestExecutor.class);
    
    AntBuildEditor buildEditor;
    AntCommandConstructor commandConstructor;

    public AntTestExecutor(final PeassFolders folders, final JUnitTestTransformer testTransformer, final EnvironmentVariables env) {
        super(folders, testTransformer, env);
        env.getEnvironmentVariables().put("ANT_OPTS", "-Duser.language=en -Duser.country=US -Duser.variant=US");

        buildEditor = new TomcatBuildEditor(testTransformer, getModules(), folders);
        commandConstructor = new TomcatCommandConstructor();
    }

    @Override
    protected void runTest(File moduleFolder, File logFile, TestMethodCall test, String testClass, long timeout) {
        final String[] command;
        if (testTransformer.getConfig().isUseKieker()) {
            AntArgLineBuilder argLineBuilder = new AntArgLineBuilder(testTransformer, moduleFolder);
            String[] arglineAnt = argLineBuilder.buildArglineAnt(buildEditor.getLastTmpFile());
            command = commandConstructor.constructTestExec(testClass, arglineAnt);
        } else {
            command = commandConstructor.constructTestExec(testClass, new String[0]);
        }
        
        ProcessBuilderHelper processBuilderHelper = new ProcessBuilderHelper(env, folders);
        processBuilderHelper.parseParams(test.getParams());

        final Process process;
        process = processBuilderHelper.buildFolderProcess(moduleFolder, logFile, command);
        execute(testClass, timeout, process);
    }

    @Override
    public void prepareKoPeMeExecution(File logFile) {

        /*
        File outputFolder = new File(folders.getProjectFolder(), "output");

        
        if (testTransformer.getConfig().getMeasurementStrategy().equals(MeasurementStrategy.PARALLEL) && outputFolder.exists()) {
            final List<File> modules = getModules().getModules();
            testTransformer.determineVersions(modules);
        } else {
        */
            try {
                clean(logFile);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            LOG.debug("Starting Test Transformation");
            prepareKiekerSource();
            transformTests();

            buildEditor.prepareBuild();

    }

    @Override
    public void executeTest(TestMethodCall test, File logFolder, long timeout) {
        final File moduleFolder = new File(folders.getProjectFolder(), test.getModule());
        runMethod(logFolder, test, moduleFolder, timeout);

    }

    @Override
    public boolean doesBuildfileExist() {
        File buildXml = new File(folders.getProjectFolder(), "build.xml");
        return buildXml.exists();
    }

    @Override
    public boolean isCommitRunning(String version) {
        try {
            clean(null);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } 
        
        final String[] command = commandConstructor.constructTestCompile();
        return new ProcessSuccessTester(folders, testTransformer.getConfig(), env)
                .testRunningSuccess(version, command);
    }

    @Override
    public ProjectModules getModules() {
        return AntModuleUtil.getModules(folders);
    }

    @Override
    protected void clean(File logFile) throws IOException, InterruptedException {
        if (!folders.getProjectFolder().exists()) {
            throw new RuntimeException("Can not execute clean - folder " + folders.getProjectFolder().getAbsolutePath() + " does not exist");
        } else {
            LOG.debug("Folder {} exists {} and is directory {} - cleaning should be possible",
                    folders.getProjectFolder().getAbsolutePath(),
                    folders.getProjectFolder().exists(),
                    folders.getProjectFolder().isDirectory());
        }
        
        final String[] command = commandConstructor.constructClean();
        final ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(folders.getProjectFolder());
        if (logFile != null) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
            processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(logFile));
        }

        boolean finished = false;
        int count = 0;
        while (!finished && count < 10) {
            final Process process = processBuilder.start();
            finished = process.waitFor(60, TimeUnit.MINUTES);
            if (!finished) {
                LOG.info("Clean process {} was not finished successfully; trying again to clean", process);
                process.destroyForcibly();
            }
            count++;
        }
    }

    @Override
    protected void runMethod(File logFolder, TestMethodCall test, File moduleFolder, long timeout) {
        try (final JUnitTestShortener shortener = new JUnitTestShortener(testTransformer, moduleFolder, test.toEntity(), test.getMethod())) {
            final File methodLogFile = getMethodLogFile(logFolder, test);
            runTest(moduleFolder, methodLogFile, test, test.getClazz(), timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
