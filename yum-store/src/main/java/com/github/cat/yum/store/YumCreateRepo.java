package com.github.cat.yum.store;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.cat.yum.store.base.YumException;
import com.github.cat.yum.store.util.FileUtils;
import com.github.cat.yum.store.util.YumUtil;

public class YumCreateRepo {
	
	private static Logger log = LoggerFactory.getLogger(YumCreateRepo.class);
	
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(Launcher.HELP_OPTION);

		try {
		    CommandLine line = parser.parse( options, args );
		    List<String> arglist = line.getArgList();
		    if(line.hasOption(Launcher.HELP_OPTION.getOpt()) || arglist.size() < 2){
		    	printHelp(options);
		    	return ;
		    }
		    String targetDir = arglist.get(1);
		    initRepodata(new File(targetDir));
		}
		catch( ParseException exp ) {
			printHelp(options);
		}catch (RuntimeException e) {
			log.info("create repo error " + e.getMessage());
			System.exit(1);
		}
	}
	
	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp(Launcher.CALLCOMMAND + " createRepo [options] targetDir", options);
	}
	

	public static void initRepodata(File dir){
		
		if(!dir.exists() || !dir.isDirectory()){
			throw new YumException(dir + " is not directory or not exists");
		}
		File root = dir;
		
		
		String rootPath = root.getAbsolutePath();
		File repoDataDir = new File(rootPath + File.separator + YumUtil.REPOPATH);
		try {
			FileUtils.forceDeleteOnExit(repoDataDir);
			FileUtils.forceMkdir(repoDataDir);
		} catch (IOException e) {
			throw new YumException(e);
		}
		if(repoDataDir.exists()){
			try {
				FileUtils.deleteDirectory(repoDataDir);
				FileUtils.forceMkdir(repoDataDir);
			} catch (IOException e) {
				throw new YumException(e);
			}
		}
	    try {
	    	log.info("init repodata start.");
			YumUtil.createRepoData(root);
			log.info("init repodata success.");
		} catch (NoSuchAlgorithmException | IOException e) {
			log.info("init repodata error.");
			throw new YumException(e);
		}
	}
}
