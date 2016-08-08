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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;
import com.github.cat.yum.store.base.Basearch;
import com.github.cat.yum.store.base.YumException;
import com.github.cat.yum.store.base.YumStore;
import com.github.cat.yum.store.model.SearchResult;
import com.github.cat.yum.store.sqlite.SqlUtils;

public class YumRetrive {
	private static Logger log = LoggerFactory.getLogger(YumRetrive.class);
	
	
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(Launcher.HELP_OPTION);
		
		options.addOption(Option.builder("c").longOpt("config").argName("file")
                .hasArg()
                .desc("Alternate path for the user yum-store.xml file" )
                .build());
		
		options.addOption(Option.builder("o").longOpt("output").argName("directory")
                .hasArg()
                .desc("output path for zip directory" )
                .build());
		
		options.addOption(Option.builder("v").longOpt("version")
                .hasArg()
                .desc("search rpm version")
                .build());
		
		try {
		    CommandLine line = parser.parse( options, args );
		    List<String> arglist = line.getArgList();
		    if(line.hasOption(Launcher.HELP_OPTION.getOpt()) || arglist.size() < 5){
		    	printHelp(options);
		    	return ;
		    }
		    String os = arglist.get(1);
		    String releasever = arglist.get(2);
		    Basearch basearch = Basearch.valueOf(arglist.get(3));
		    String rpmName = arglist.get(4);
		    String version = line.getOptionValue("version");
		    File storeXml = new File(line.getOptionValue("config", "conf/yum-store.xml"));
		    String output = line.getOptionValue("output");
		    File outputFile = null;
		    if(!StringUtils.isBlank(output)){
		    	outputFile = new File(output);
		    }
		    search(storeXml, os, releasever, basearch, rpmName, version, outputFile);
		}
		catch( ParseException exp ) {
			printHelp(options);
		}catch (RuntimeException e) {
			log.info("search error " + e.getMessage());
		}
	}
	
	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp(Launcher.CALLCOMMAND + " retrive [options] os releasever basearch rpmName", 
	    		"example: " + Launcher.CALLCOMMAND + " retrive centos 7 x86_64 unzip"  , options , "");
	}
	
	
	public static void search(File storeXml, String os, String releasever, Basearch basearch, String rpmName, String version, File output) {
		YumStore yumStore = new YumStore(storeXml);
		yumStore.init(os, releasever, basearch);
		
		log.info("search start");
		SearchResult result = yumStore.retrive(rpmName, version, basearch);
		log.info("search success");
		log.info("zip start");
		if(null == output){
			if(YumStore.cachedir.exists()){
				output = YumStore.cachedir.getAbsoluteFile();
			}
			else{
				output = new File("").getAbsoluteFile();
			}
		}
		try{
			if(!output.exists()){
				FileUtils.forceMkdir(output);
			}
			for(File rpm : result.rpms){
				FileUtils.copyFileToDirectory(rpm, output);
			}
		}catch(IOException e){
			throw new YumException(e);
		}
		File outputZip = new File(output.getAbsolutePath() + File.separator + SqlUtils.getUUId() + ".zip");
		ZipUtil.packEntries(result.rpms.toArray(new File[]{}) , outputZip);
		log.info("zip success");
		log.info("result zip file path:" + outputZip);
	}
}
