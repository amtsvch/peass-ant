package de.stro18.peass_ant.executor;

import org.apache.commons.lang3.StringUtils;

public class TomcatCommandConstructor implements AntCommandConstructor {
    
    private static final String ANT = "ant";
    
    public String[] constructTestCompile() {
        return new String[] {ANT, "test-compile" };
    }
    
    public String[] constructClean() {
        return new String[] {ANT, "clean" };
    }
    
    public String[] constructTestExec(String testClass, String[] argLine) {
        String[] command = new String[] {ANT, "test", "-Dexecute.test.nio2=false", "-Dexecute.test.apr=false", "-Dtest.entry=" + testClass};
        
        StringBuilder stringBuilder = new StringBuilder(StringUtils.join(command, " "));
        for (String arg : argLine) {
            String[] keyAndValue = arg.split("=");
            stringBuilder.append(" " + keyAndValue[0] + "=" + keyAndValue[1]);
        }
        
        return stringBuilder.toString().split(" ");
    }
}