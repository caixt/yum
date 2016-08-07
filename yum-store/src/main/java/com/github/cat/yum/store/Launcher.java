package com.github.cat.yum.store;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Launcher {
	
	public static Option HELP_OPTION = new Option("help", false, "print help message");
	
	public static String CALLCOMMAND = System.getProperty("CALL_COMMAND", "");
	
	
	public static void main(String[] args) {
		Options options = new Options();
		options.addOption(null, "cleanCache", false, "clean all cache");
		options.addOption(null, "search", false, "search rpm and download");
		options.addOption(null, "createRepo", false, "create repo for yum store");
        
		if(args.length == 0){
			printHelp(options);
			return ;
		}
		String command = args[0];
		Option option = options.getOption("--" + command);
		if (option == null) {
			printHelp(options);
			return ;
		}		
		
		String className = Launcher.class.getPackage().getName() + ".Yum" + 
				command.substring(0, 1).toUpperCase() + command.substring(1);
		
		try {
			Class.forName(className).getMethod("main", String[].class).invoke(null, (Object)args);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}  

	}
	
	
	private static void printHelp(Options options){
        HelpFormatter formatter = new HelpFormatter();
		formatter.setSyntaxPrefix(CALLCOMMAND + " command");
	    formatter.setLongOptPrefix("");
	    formatter.printHelp(" ", options);
	}
}
