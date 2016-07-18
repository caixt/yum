package com.github.cat.yum.store.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.cat.yum.store.RpmScan;
import com.github.cat.yum.store.filter.PrivateFileDirFilter;
import com.github.cat.yum.store.filter.PrivateFileFilter;
import com.github.cat.yum.store.filter.PrivateRequireFilter;
import com.github.cat.yum.store.filter.YumFileter;
import com.github.cat.yum.store.model.ChangeLog;
import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.RepoModule;
import com.github.cat.yum.store.model.RpmMetadata;

public class YumUtil {
	private static Logger log = LoggerFactory.getLogger(YumUtil.class);
	
	public static String REPOPATH = "repodata";
	
	public static String ALGORITHM = "sha";
	
	public static Namespace REPONAMESPACE = Namespace.getNamespace("", "http://linux.duke.edu/metadata/repo");
	public static Namespace RPMNAMESPACE = Namespace.getNamespace("rpm", "http://linux.duke.edu/metadata/rpm");
	public static Namespace FILELISTSNAMESPACE = Namespace.getNamespace("", "http://linux.duke.edu/metadata/filelists");
	public static Namespace COMMONNAMESPACE = Namespace.getNamespace("", "http://linux.duke.edu/metadata/common");
	public static Namespace OTHERNAMESPACE = Namespace.getNamespace("", "http://linux.duke.edu/metadata/other");
	
	
	public static boolean createRepoData(File dir) {
		try{
			if(!dir.exists() || !dir.isDirectory()){
				throw new IllegalArgumentException(dir + " is not directory or not exists");
			}
			String rootPath = dir.getAbsolutePath();
			File repoDataDir = new File(rootPath + File.separator + REPOPATH);
			if(repoDataDir.exists()){
				FileUtils.deleteDirectory(repoDataDir);
			}
			FileUtils.forceMkdir(repoDataDir);
			File[] rpms =  FileUtils.listFiles(dir, new String[]{"rpm"}, true).toArray(new File[]{});
			
			RpmData[] rpmDatas = new RpmData[rpms.length];
		    for(int i = 0; i < rpms.length; i++){
		    	File rpm = rpms[i];
	        	RpmScan rpmScan = new RpmScan(rpm);
	    		RpmMetadata rpmMetadata = rpmScan.getRpmMetadata();
	    		rpmDatas[i] = new RpmData(rpm, rpmMetadata);
		     }
			
			RepoModule repoFilelists = createFilelitsts(rpmDatas, rootPath);
			RepoModule repoPrimary = createPrimary(rpmDatas, rootPath);
			RepoModule repoOther = createOther(rpmDatas, rootPath);
			
			createRepoMd(rootPath, repoOther, repoFilelists, repoPrimary);
		} catch (NoSuchAlgorithmException ignore) {
			log.error("", ignore);
			return false;
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
		return true;
	}
	
	private static void createRepoMd(String rootPath, RepoModule ...repos) throws IOException{
		Document doc = new Document();
        Element root = new Element("repomd", REPONAMESPACE);
        doc.addContent(root);
        root.addNamespaceDeclaration(RPMNAMESPACE);
        long now = System.currentTimeMillis();
        
        for(RepoModule repo : repos){
        	Element data = new Element("data", REPONAMESPACE);
        	data.setAttribute("type", repo.getModule());
    	    Element location = new Element("location", REPONAMESPACE);
    	    File xmlGzFie = getXmlGzFile(repo, repo.getXmlGzCode());
    	    location.setAttribute("href", replacePath(FileUtils.getFileRelativePath(repo.getRootPath(), xmlGzFie)));
    	    data.addContent(location);
    	    Element checksum = new Element("checksum", REPONAMESPACE);
    	    checksum.setAttribute("type", ALGORITHM);
    	    checksum.setAttribute("pkgid", "YES");
    	    checksum.setText(repo.getXmlGzCode());
    	    data.addContent(checksum);
    	    Element size = new Element("size", REPONAMESPACE);
    	    size.setText(repo.getXmlGzSize() + "");
    	    data.addContent(size);
    	    Element timestamp = new Element("timestamp", REPONAMESPACE);
    	    timestamp.setText(now + "");
    	    data.addContent(timestamp);
    	    Element openCheckSum  = new Element("open-checksum", REPONAMESPACE);
    	    openCheckSum.setAttribute("type", ALGORITHM);
    	    openCheckSum.setAttribute("pkgid", "YES");
    	    openCheckSum.setText(repo.getXmlCode());
    	    data.addContent(openCheckSum);
    	    Element openSize  = new Element("open-size", REPONAMESPACE);
    	    openSize.setText(repo.getXmlSize() + "");
    	    data.addContent(openSize);
    	    Element revision  = new Element("revision", REPONAMESPACE);
    	    data.addContent(revision);
    	    root.addContent(data);
        }
        File repoMd = new File(rootPath+ File.separator + REPOPATH 
				+ File.separator + "repomd" + ".xml");
        xmlToFile(doc, repoMd);
	}
	
	private static RepoModule createOther(RpmData[] rpmdatas, String rootPath) throws IOException, NoSuchAlgorithmException{
		RepoModule repo = new RepoModule(rootPath, "other");
		Document doc = new Document();
        Element root = new Element("otherdata", OTHERNAMESPACE);
        doc.addContent(root);
        root.setAttribute("packages", rpmdatas.length + "");
        
        for(RpmData rpmdata : rpmdatas){
    		RpmMetadata rpmMetadata = rpmdata.rpmMetadata;
    		
	        Element packAge =  new Element("package", OTHERNAMESPACE);
	        packAge.setAttribute("pkgid", HashFile.getsum(rpmdata.rpm, ALGORITHM));
	        packAge.setAttribute("name",  rpmMetadata.name);
	        packAge.setAttribute("arch",  rpmMetadata.architecture);
	        
	        root.addContent(packAge);
	        
	        Element version =  new Element("version", OTHERNAMESPACE);
	        version.setAttribute("epoch", rpmMetadata.epoch + "");
	        version.setAttribute("ver", rpmMetadata.version);
	        version.setAttribute("rel", rpmMetadata.release);
	        packAge.setContent(version);
	        
	        for(ChangeLog log : rpmMetadata.changeLogs){
	        	Element fileElement =  new Element("changelog", OTHERNAMESPACE);
	        	fileElement.setAttribute("author", log.author);
	        	fileElement.setAttribute("date", log.date + "");
	        	fileElement.setText(log.text);
	        	packAge.addContent(fileElement);
	        }
        }
	    yumXmlSave(doc, repo);
	    return repo;
	}
	
	
	private static RepoModule createPrimary(RpmData[] rpmdatas, String rootPath) throws IOException, NoSuchAlgorithmException{
		RepoModule repo = new RepoModule(rootPath, "primary");
		Document doc = new Document();
        Element root = new Element("metadata", COMMONNAMESPACE);
        doc.addContent(root);
        root.addNamespaceDeclaration(RPMNAMESPACE);
        root.setAttribute("packages", rpmdatas.length + "");
        
        for(RpmData rpmdata :rpmdatas){
        	RpmMetadata rpmMetadata = rpmdata.rpmMetadata;
        
	        Element packAge =  new Element("package", COMMONNAMESPACE);
	        packAge.setAttribute("type", "rpm");
	        root.addContent(packAge);
        
	        Element name =  new Element("name", COMMONNAMESPACE);
	        name.setText(rpmMetadata.name);
	        packAge.addContent(name);
	        
	        Element arch =  new Element("arch", COMMONNAMESPACE);
	        arch.setText(rpmMetadata.architecture);
	        packAge.addContent(arch);
	        
	        Element version =  new Element("version", COMMONNAMESPACE);
	        version.setAttribute("epoch", rpmMetadata.epoch + "");
	        version.setAttribute("ver", rpmMetadata.version);
	        version.setAttribute("rel", rpmMetadata.release);
	        packAge.addContent(version);
	        
	        
	        Element checksum =  new Element("checksum", COMMONNAMESPACE);
	        checksum.setAttribute("type", ALGORITHM);
	        checksum.setAttribute("pkgid", "YES");
	        checksum.setText(HashFile.getsum(rpmdata.rpm, ALGORITHM));
	        packAge.addContent(checksum);
	        
	        Element summary =  new Element("summary", COMMONNAMESPACE);
	        summary.setText(rpmMetadata.summary);
	        packAge.addContent(summary);
	        
	        Element description =  new Element("description", COMMONNAMESPACE);
	        description.setText(rpmMetadata.description);
	        packAge.addContent(description);
	        
	        Element packager =  new Element("packager", COMMONNAMESPACE);
	        packager.setText(rpmMetadata.packager);
	        packAge.addContent(packager);
	        
	        Element url =  new Element("url", COMMONNAMESPACE);
	        url.setText(rpmMetadata.url);
	        packAge.addContent(url);
	        
	        Element time =  new Element("time", COMMONNAMESPACE);
	        time.setAttribute("file", rpmdata.rpm.lastModified()/1000 + "");
	        time.setAttribute("build", rpmMetadata.buildTime + "");
	        packAge.addContent(time);
	        
	        Element size =  new Element("size", COMMONNAMESPACE);
	        size.setAttribute("package", rpmdata.rpm.length() + "");
	        size.setAttribute("installed", rpmMetadata.installedSize + "");
	        size.setAttribute("archive", rpmMetadata.archiveSize + "");
	        packAge.addContent(size);
	        
	        Element location =  new Element("location", COMMONNAMESPACE);
	        location.setAttribute("href", replacePath(FileUtils.getFileRelativePath(rootPath, rpmdata.rpm)));
	        packAge.addContent(location);
	        
	        Element format =  new Element("format", COMMONNAMESPACE);
	        packAge.addContent(format);
	        
	        Element license =  new Element("license", RPMNAMESPACE);
	        license.setText(rpmMetadata.license);
	        format.addContent(license);
	        
	        Element vendor =  new Element("vendor", RPMNAMESPACE);
	        vendor.setText(rpmMetadata.vendor);
	        format.addContent(vendor);
	        
	        Element group =  new Element("group", RPMNAMESPACE);
	        group.setText(rpmMetadata.group);
	        format.addContent(group);
	        
	        Element buildhost =  new Element("buildhost", RPMNAMESPACE);
	        buildhost.setText(rpmMetadata.buildHost);
	        format.addContent(buildhost);
	        
	        Element sourcerpm =  new Element("sourcerpm", RPMNAMESPACE);
	        sourcerpm.setText(rpmMetadata.sourceRpm);
	        format.addContent(sourcerpm);
	        
	        Element headerRange =  new Element("header-range", RPMNAMESPACE);
	        headerRange.setAttribute("start", rpmMetadata.headerStart + "");
	        headerRange.setAttribute("end", rpmMetadata.headerEnd + "");
	        format.addContent(headerRange);
	        
	        Element provides =  new Element("provides", RPMNAMESPACE);
	        format.addContent(provides);
	        addEntry(provides, rpmMetadata.provide, null);
	        
	        Element requires =  new Element("requires", RPMNAMESPACE);
	        format.addContent(requires);
	        addEntry(requires, rpmMetadata.require, new PrivateRequireFilter());
	
	        Element conflicts =  new Element("conflicts", RPMNAMESPACE);
	        format.addContent(conflicts);
	        addEntry(conflicts, rpmMetadata.conflict, null);
	        
	        Element obsoletes =  new Element("obsoletes", RPMNAMESPACE);
	        format.addContent(obsoletes);
	        addEntry(obsoletes, rpmMetadata.obsolete, null);
	        
	        YumFileter fileflter = new PrivateFileFilter();
	        YumFileter fileDirflter = new PrivateFileDirFilter();
	        for(com.github.cat.yum.store.model.File file : rpmMetadata.files){
	        	if(StringUtils.isBlank(file.type)){
	        		if(fileflter.filter(file.path)){
	        			continue;
	        		}
	        	}
	        	else if("dir".equals(file.type)){
	        		if(fileDirflter.filter(file.path)){
	        			continue;
	        		}
	        	}
	        	
	        	Element fileElemenrt =  new Element("file", COMMONNAMESPACE);
	        	fileElemenrt.setText(file.path);
	        	if(!StringUtils.isBlank(file.type)){
	        		fileElemenrt.setAttribute("type", file.type);
	        	}
	 	        format.addContent(fileElemenrt);
	        }
        }
        yumXmlSave(doc, repo);
        return repo;
	}
	
	private static RepoModule createFilelitsts(RpmData[] rpmdatas, String rootPath) throws IOException, NoSuchAlgorithmException{
		RepoModule repo = new RepoModule(rootPath, "filelists");
		Document doc = new Document();
        Element root = new Element("filelists", FILELISTSNAMESPACE);
        doc.addContent(root);
        root.setAttribute("packages", rpmdatas.length + "");
        
        for(RpmData rpmdata : rpmdatas){
    		RpmMetadata rpmMetadata = rpmdata.rpmMetadata;
        	
	        Element packAge =  new Element("package", FILELISTSNAMESPACE);
	        packAge.setAttribute("pkgid", HashFile.getsum(rpmdata.rpm, ALGORITHM));
	        packAge.setAttribute("name",  rpmMetadata.name);
	        packAge.setAttribute("arch",  rpmMetadata.architecture);
	        root.addContent(packAge);
        
	        Element version =  new Element("version", FILELISTSNAMESPACE);
	        version.setAttribute("epoch", rpmMetadata.epoch + "");
	        version.setAttribute("ver", rpmMetadata.version);
	        version.setAttribute("rel", rpmMetadata.release);
	        packAge.setContent(version);
	        
	        for(com.github.cat.yum.store.model.File file : rpmMetadata.files){
	        	Element fileElement =  new Element("file", FILELISTSNAMESPACE);
	        	fileElement.setText(file.path);
	        	if(file.type != null){
	        		fileElement.setAttribute("type", file.type);
	        	}
	        	packAge.addContent(fileElement);
	        }
        }
        yumXmlSave(doc, repo);
        return repo;
	}
	
	private static void yumXmlSave(Document doc, RepoModule repo) throws IOException, NoSuchAlgorithmException{
		File outfile = getXmlFile(repo);
        xmlToFile(doc, outfile);
    
	    String xmlCode = HashFile.getsum(outfile, ALGORITHM);
	    repo.setXmlCode(xmlCode);
	    repo.setXmlSize(outfile.length());
	    GZipUtils.compress(outfile);
	    
	    outfile = getXmlGzFile(repo);
	    String xmlGzode = HashFile.getsum(outfile, ALGORITHM);
	    repo.setXmlGzCode(xmlGzode);
	    repo.setXmlGzSize(outfile.length());
	    
	    outfile.renameTo(getXmlGzFile(repo, xmlGzode));
	}
	
	private static File getXmlFile(RepoModule repo){
		return new File(repo.getRootPath() + File.separator 
				+ REPOPATH + File.separator + repo.getModule() + ".xml");
	}
	
	private static File getXmlGzFile(RepoModule repo, String code){
		return new File(repo.getRootPath() + File.separator + REPOPATH 
				+ File.separator + code + "-" + repo.getModule() + ".xml.gz");
	}
	
	private static File getXmlGzFile(RepoModule repo){
		return new File(repo.getRootPath() + File.separator + REPOPATH 
				+ File.separator + repo.getModule() + ".xml.gz");
	}
	
	private static String replacePath(String path){
		return path.replace(File.separatorChar, '/');
	}
	
	
	private static void addEntry(Element parent, List<Entry> entrys, YumFileter filter){
        for(Entry entry : entrys){
        	Element entryElement =  new Element("entry", RPMNAMESPACE);
        	String name = entry.name;
        	if(null != filter && filter.filter(name)){
        		continue;
        	}
        	entryElement.setAttribute("name", name);
        	if(null != entry.flags){
        		entryElement.setAttribute("flags", entry.flags);
        	}
        	if(null != entry.epoch){
        		entryElement.setAttribute("epoch", entry.epoch);
        	}
        	if(null != entry.version){
        		entryElement.setAttribute("ver", entry.version);
        	}
        	if(null != entry.release){
        		entryElement.setAttribute("rel", entry.release);
        	}
        	if(null != entry.pre){
        		entryElement.setAttribute("pre", entry.pre);
        	}
        	parent.addContent(entryElement);
        }
	}
	
	private static void xmlToFile(Document doc, File outfile) throws IOException {
		FileOutputStream fileOutputStream = null;
		try{
			Format formate = Format.getPrettyFormat();
			formate.setOmitEncoding(true);
			formate.setLineSeparator(LineSeparator.NL);
			// System.out.println(new XMLOutputter(formate).outputString(doc));
			fileOutputStream = new FileOutputStream(outfile, false);
			new XMLOutputter(formate).output(doc, fileOutputStream);
		}finally{
			try{
				fileOutputStream.close();
			}catch(IOException ignore){
				
			}
		}
	}
	
	private static class RpmData {
		public RpmData(File rpm, RpmMetadata rpmMetadata) {
			super();
			this.rpm = rpm;
			this.rpmMetadata = rpmMetadata;
		}
		
		protected File rpm;
		private RpmMetadata rpmMetadata;
	}
}