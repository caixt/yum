package com.github.cat.yum.store;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.cat.yum.store.base.YumException;
import com.github.cat.yum.store.base.YumStore;

public class YumCleanCache {
	private static Logger log = LoggerFactory.getLogger(YumCleanCache.class);
	
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(Launcher.HELP_OPTION);
		
		options.addOption(Option.builder("c").longOpt("config").argName("file")
                .hasArg()
                .desc("Alternate path for the user yum-store.xml file" )
                .build());
		
		try {
		    CommandLine line = parser.parse( options, args );
		    List<String> arglist = line.getArgList();
		    if(line.hasOption(Launcher.HELP_OPTION.getOpt()) || arglist.size() < 1){
		    	printHelp(options);
		    	return ;
		    }
		    File storeXml = new File(line.getOptionValue("config", "conf/yum-store.xml"));
		    
		    clean(storeXml);
		}
		catch( ParseException exp ) {
			printHelp(options);
		}catch (RuntimeException e) {
			log.info("clean error " + e.getMessage());
		}
		
	}
	
	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp(Launcher.CALLCOMMAND + " clean [options]", options);
	}
	
	public static void clean(File storeXml) {
		YumStore yumStore = new YumStore(storeXml);
		try {
			log.info("clean start.");
			yumStore.clean();
			log.info("clean sucess.");
		} catch (IOException e) {
			log.info("clean fail.");
			throw new YumException(e);
		}
	}
}
