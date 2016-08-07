package com.github.cat.yum.store;

import java.io.File;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;
import com.github.cat.yum.store.base.YumStore;
import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.SearchResult;
import com.github.cat.yum.store.sqlite.SqlUtils;

public class YumSearch {
	private static Logger log = LoggerFactory.getLogger(YumSearch.class);
	
	
	public static void main(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = new Options();
		options.addOption(Launcher.HELP_OPTION);
		
		options.addOption(Option.builder("c").longOpt("config").argName("file")
                .hasArg()
                .desc("Alternate path for the user yum-store.xml file" )
                .build());
		
		options.addOption(Option.builder("o").longOpt("output").argName("file")
                .hasArg()
                .desc("output path for zip file" )
                .build());
		
		options.addOption(Option.builder("v").longOpt("version")
                .hasArg()
                .desc("search rpm version")
                .build());
		
		options.addOption(Option.builder("a").longOpt("arch")
                .hasArg()
                .desc("search rpm arch [i686|x86-64]")
                .build());
		
		try {
		    CommandLine line = parser.parse( options, args );
		    List<String> arglist = line.getArgList();
		    if(line.hasOption(Launcher.HELP_OPTION.getOpt()) || arglist.size() < 2){
		    	printHelp(options);
		    	return ;
		    }
		    String rpmName = arglist.get(1);
		    String version = line.getOptionValue("version");
		    String arch = line.getOptionValue("arch");
		    File storeXml = new File(line.getOptionValue("config", "conf/yum-store.xml"));
		    String outpout = line.getOptionValue("output");
		    File outpoutFile = null;
		    if(!StringUtils.isBlank(outpout)){
		    	outpoutFile = new File(outpout);
		    }
		    search(storeXml, rpmName, version, arch, outpoutFile);
		}
		catch( ParseException exp ) {
			printHelp(options);
		}catch (RuntimeException e) {
			log.info("search error " + e.getMessage());
		}
	}
	
	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
	    formatter.printHelp(Launcher.CALLCOMMAND + " search [options] rpmName", options);
	}
	
	
	public static void search(File storeXml, String rpmName, String version, String arch, File outpout) {
		YumStore yumStore = new YumStore(storeXml);
		yumStore.init();
		
		Entry search = new Entry();
		search.name = rpmName;
		if(!StringUtils.isBlank(version)){
			search.flags = "EQ";
			search.release = version;
		}
		
		if(!StringUtils.isBlank(arch)){
			search.name += "(" + arch + ")";
		}
		log.info("search start");
		SearchResult searchResult = yumStore.searchAndDownload(search);
		log.info("search success");
		log.info("zip start");
		if(null == outpout){
			String resultZip = SqlUtils.getUUId() + ".zip";
			if(YumStore.cachedir.exists()){
				resultZip = YumStore.cachedir.getAbsolutePath() + File.separator + resultZip;
			}
			outpout = new File(resultZip);
		}
		ZipUtil.packEntries(searchResult.rpms.toArray(new File[]{}) , outpout);
		log.info("zip success");
		log.info("result zip file path:" + outpout);
	}
}
