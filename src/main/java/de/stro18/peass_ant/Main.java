package de.stro18.peass_ant;

import java.util.concurrent.Callable;

import de.dagere.peass.*;
import de.dagere.peass.measurement.cleaning.CleanStarter;
import de.dagere.peass.measurement.utils.CreateScriptStarter;
import de.dagere.peass.visualization.VisualizeRCAStarter;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "peass", mixinStandardHelpOptions = true, subcommands = {
        SelectStarter.class,
        MeasureStarter.class,
        GetChangesStarter.class,
        CleanStarter.class,
        IsChangeStarter.class,
        SearchCauseStarter.class,
        CreateScriptStarter.class,
        VisualizeRCAStarter.class,
        ContinuousExecutionStarter.class},
        synopsisSubcommandLabel = "COMMAND")
public class Main implements Callable<Void> {

    public static void main(final String[] args) {
        final CommandLine line = new CommandLine(new Main());
        if (args.length != 0) {
            System.exit(line.execute(args));
        } else {
            line.usage(System.out);
        }
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}

