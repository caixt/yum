package com.github.cat.yum.store.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.freecompany.redline.ChannelWrapper;
import org.freecompany.redline.ReadableChannelWrapper;
import org.freecompany.redline.header.AbstractHeader;
import org.freecompany.redline.header.Flags;
import org.freecompany.redline.header.Format;
import org.freecompany.redline.header.Header;
import org.freecompany.redline.header.Signature;
import com.github.cat.yum.store.model.ChangeLog;
import com.github.cat.yum.store.model.Entry;
import com.github.cat.yum.store.model.RpmFormat;
import com.github.cat.yum.store.model.RpmMetadata;
import com.google.common.collect.Lists;

public class RpmScan {
	
	private File rpm;
	
	public RpmScan(File rpm){
		this.rpm = rpm;
	}

	public RpmMetadata getRpmMetadata() throws IOException{
		RpmFormat rpmFormat = getRpmFormat();
		RpmMetadata data = analysis(rpmFormat);
		return data;
	}
	
	
	@SuppressWarnings("unused")
	private RpmFormat getRpmFormat() throws IOException {
		InputStream sourceStream =  null;
		try{
			sourceStream = new FileInputStream(this.rpm);
	
			RpmFormat rpmFormat = new RpmFormat();
			ReadableChannelWrapper in = getReadableChannel(sourceStream);
	
			Format format = new Format();
			ChannelWrapper.Key<Integer> headerStartKey = in.start();
	
			ChannelWrapper.Key<Integer> lead = in.start();
			format.getLead().read(in);
	
			ChannelWrapper.Key<Integer> signature = in.start();
			int count = format.getSignature().read(in);
			AbstractHeader.Entry<?> sigEntry = format.getSignature().getEntry(Signature.SignatureTag.SIGNATURES);
			int expected = sigEntry == null ? 0
					: ByteBuffer.wrap((byte[]) (byte[]) sigEntry.getValues(), 8, 4).getInt() / -16;
	
			Integer headerStartPos = (Integer) in.finish(headerStartKey);
			format.getHeader().setStartPos(headerStartPos.intValue());
			ChannelWrapper.Key<Integer> headerKey = in.start();
			count = format.getHeader().read(in);
			AbstractHeader.Entry<?> immutableEntry = format.getHeader().getEntry(Header.HeaderTag.HEADERIMMUTABLE);
			expected = immutableEntry == null ? 0
					: ByteBuffer.wrap((byte[]) (byte[]) immutableEntry.getValues(), 8, 4).getInt() / -16;
			Integer headerLength = (Integer) in.finish(headerKey);
			format.getHeader().setEndPos(headerStartPos.intValue() + headerLength.intValue());
	
			rpmFormat.headerStart = headerStartPos.intValue();
			rpmFormat.headerEnd = (headerStartPos.intValue() + headerLength.intValue());
			rpmFormat.format = format;

			return rpmFormat;
		}finally{
			try{
				sourceStream.close();
			}catch(IOException ignore){
				
			}
		}
	}
	

	private ReadableChannelWrapper getReadableChannel(
			InputStream inputStream) {
		return  new ReadableChannelWrapper(Channels.newChannel(inputStream));
	}
	
	public RpmMetadata analysis(RpmFormat rawMetadata) {
		RpmMetadata rpmMetadata = new RpmMetadata();

		Header header = rawMetadata.format.getHeader();
		Signature signature = rawMetadata.format.getSignature();

		rpmMetadata.headerStart = rawMetadata.headerStart;
		rpmMetadata.headerEnd = rawMetadata.headerEnd;
		rpmMetadata.name = getName(header);
		rpmMetadata.architecture = getArchitecture(header);
		rpmMetadata.version = getVersion(header);
		rpmMetadata.epoch = getEpoch(header);
		rpmMetadata.release = getRelease(header);
		rpmMetadata.summary = getSummary(header);
		rpmMetadata.description = getDescription(header);
		rpmMetadata.packager = getPackager(header);
		rpmMetadata.url = getUrl(header);
		rpmMetadata.buildTime = getBuildTime(header);
		rpmMetadata.installedSize = getInstalledSize(header);
		rpmMetadata.archiveSize = getArchiveSize(signature);
		rpmMetadata.license = getLicense(header);
		rpmMetadata.vendor = getVendor(header);
		rpmMetadata.group = getGroup(header);
		rpmMetadata.sourceRpm = getSourceRpm(header);
		rpmMetadata.buildHost = getBuildHost(header);

		rpmMetadata.provide = resolveEntriesEntries(header,
				Header.HeaderTag.PROVIDENAME, Header.HeaderTag.PROVIDEFLAGS,
				Header.HeaderTag.PROVIDEVERSION);

		rpmMetadata.require = resolveEntriesEntries(header,
				Header.HeaderTag.REQUIRENAME, Header.HeaderTag.REQUIREFLAGS,
				Header.HeaderTag.REQUIREVERSION);

		rpmMetadata.conflict = resolveEntriesEntries(header,
				Header.HeaderTag.CONFLICTNAME, Header.HeaderTag.CONFLICTFLAGS,
				Header.HeaderTag.CONFLICTVERSION);

		rpmMetadata.obsolete = resolveEntriesEntries(header,
				Header.HeaderTag.OBSOLETENAME, Header.HeaderTag.OBSOLETEFLAGS,
				Header.HeaderTag.OBSOLETEVERSION);

		rpmMetadata.files = resolveFiles(header);
		rpmMetadata.changeLogs = resolveChangeLogs(header);

		return rpmMetadata;
	}

	private String getName(Header header) {
		return getStringHeader(header, Header.HeaderTag.NAME);
	}

	private String getArchitecture(Header header) {
		return getStringHeader(header, Header.HeaderTag.ARCH);
	}

	private String getVersion(Header header) {
		return getStringHeader(header, Header.HeaderTag.VERSION);
	}

	private int getEpoch(Header header) {
		return getIntHeader(header, Header.HeaderTag.EPOCH);
	}

	private String getRelease(Header header) {
		return getStringHeader(header, Header.HeaderTag.RELEASE);
	}

	private String getSummary(Header header) {
		return getStringHeader(header, Header.HeaderTag.SUMMARY);
	}

	private String getDescription(Header header) {
		return getStringHeader(header, Header.HeaderTag.DESCRIPTION);
	}

	private String getPackager(Header header) {
		return getStringHeader(header, Header.HeaderTag.PACKAGER);
	}

	private String getUrl(Header header) {
		return getStringHeader(header, Header.HeaderTag.URL);
	}

	private int getBuildTime(Header header) {
		return getIntHeader(header, Header.HeaderTag.BUILDTIME);
	}

	private int getInstalledSize(Header header) {
		return getIntHeader(header, Header.HeaderTag.SIZE);
	}

	private int getArchiveSize(Signature signature) {
		return getIntHeader(signature, Signature.SignatureTag.PAYLOADSIZE);
	}

	private String getLicense(Header header) {
		return getStringHeader(header, Header.HeaderTag.LICENSE);
	}

	private String getVendor(Header header) {
		return getStringHeader(header, Header.HeaderTag.VENDOR);
	}

	private String getGroup(Header header) {
		return getStringHeader(header, Header.HeaderTag.GROUP);
	}

	private String getSourceRpm(Header header) {
		return getStringHeader(header, Header.HeaderTag.SOURCERPM);
	}

	private String getBuildHost(Header header) {
		return getStringHeader(header, Header.HeaderTag.BUILDHOST);
	}

	private LinkedList<Entry> resolveEntriesEntries(Header header,
			Header.HeaderTag namesTag, Header.HeaderTag flagsTag,
			Header.HeaderTag versionsTag) {
		LinkedList<Entry> entries = Lists.newLinkedList();

		String[] entryNames = getStringArrayHeader(header, namesTag);
		if (entryNames != null) {
			int[] entryFlags = getIntArrayHeader(header, flagsTag);
			String[] entryVersions = getStringArrayHeader(header, versionsTag);

			for (int i = 0; i < entryNames.length; i++) {
				String entryName = entryNames[i];
				Entry entry = new Entry();
				entry.name = entryName;

				if (entryFlags.length > i) {
					int entryFlag = entryFlags[i];
					setEntryFlags(entryFlag, entry);

					if ((entryFlag & Flags.PREREQ) > 0) {
						entry.pre = "1";
					}
				}
				if (entryVersions.length > i) {
					setEntryVersionFields(entryVersions[i], entry);
				}
				entries.add(entry);
			}
		}
		return entries;
	}

	private int setEntryFlags(int entryFlags, Entry entry) {
		if (((entryFlags & Flags.LESS) > 0) && ((entryFlags & Flags.EQUAL) > 0))
			entry.flags = "LE";
		else if (((entryFlags & Flags.GREATER) > 0)
				&& ((entryFlags & Flags.EQUAL) > 0))
			entry.flags = "GE";
		else if ((entryFlags & Flags.EQUAL) > 0)
			entry.flags = "EQ";
		else if ((entryFlags & Flags.LESS) > 0)
			entry.flags = "LT";
		else if ((entryFlags & Flags.GREATER) > 0) {
			entry.flags = "GT";
		}
		return entryFlags;
	}

	private void setEntryVersionFields(String entryVersion, Entry entry) {
		if (StringUtils.isNotBlank(entryVersion)) {
			String[] versionTokens = StringUtils.split(entryVersion, '-');

			String versionValue = versionTokens[0];
			String[] versionValueTokens = StringUtils.split(versionValue, ':');
			if (versionValueTokens.length > 1) {
				entry.epoch = versionValueTokens[0];
				entry.version = versionValueTokens[1];
			} else {
				entry.epoch = "0";
				entry.version = versionValueTokens[0];
			}

			if (versionTokens.length > 1) {
				String releaseValue = versionTokens[1];
				if (StringUtils.isNotBlank(releaseValue))
					entry.release = releaseValue;
			}
		}
	}

	private LinkedList<com.github.cat.yum.store.model.File> resolveFiles(Header header) {
		LinkedList<com.github.cat.yum.store.model.File> files = Lists.newLinkedList();
		String[] baseNames = getStringArrayHeader(header,
				Header.HeaderTag.BASENAMES);
		int[] baseNameDirIndexes = getIntArrayHeader(header,
				Header.HeaderTag.DIRINDEXES);
		List<String> dirPaths = Lists.newArrayList(getStringArrayHeader(header,
				Header.HeaderTag.DIRNAMES));

		for (int i = 0; i < baseNames.length; i++) {
			String baseName = baseNames[i];
			int baseNameDirIndex = baseNameDirIndexes[i];
			String filePath = (String) dirPaths.get(baseNameDirIndex)
					+ baseName;
			boolean dir = dirPaths.contains(filePath + "/");
			com.github.cat.yum.store.model.File file = new com.github.cat.yum.store.model.File();
			file.path = filePath;
			file.type = (dir ? "dir" : null);
			files.add(file);
		}

		return files;
	}

	private LinkedList<ChangeLog> resolveChangeLogs(Header header) {
		LinkedList<ChangeLog> changeLogs = Lists.newLinkedList();
		String[] changeLogAuthors = getStringArrayHeader(header,
				Header.HeaderTag.CHANGELOGNAME);
		int[] changeLogDates = getIntArrayHeader(header,
				Header.HeaderTag.CHANGELOGTIME);
		String[] changeLogTexts = getStringArrayHeader(header,
				Header.HeaderTag.CHANGELOGTEXT);

		for (int i = 0; i < changeLogTexts.length; i++) {
			ChangeLog changeLog = new ChangeLog();
			changeLog.author = changeLogAuthors[i];
			changeLog.date = changeLogDates[i];
			changeLog.text = changeLogTexts[i];
			changeLogs.add(changeLog);
		}
		return changeLogs;
	}

	private <H extends AbstractHeader, T extends AbstractHeader.Tag> String getStringHeader(
			H header, T tag) {
		String[] values = getStringArrayHeader(header, tag);
		if ((values == null) || (values.length < 1)) {
			return null;
		}
		return values[0];
	}

	private <H extends AbstractHeader, T extends AbstractHeader.Tag> String[] getStringArrayHeader(
			H header, T tag) {
		AbstractHeader.Entry<?> entry = header.getEntry(tag);
		if (entry == null) {
			return new String[0];
		}
		return (String[]) (String[]) entry.getValues();
	}

	private <H extends AbstractHeader, T extends AbstractHeader.Tag> int getIntHeader(
			H header, T tag) {
		int[] values = getIntArrayHeader(header, tag);
		if ((values == null) || (values.length < 1)) {
			return 0;
		}
		return values[0];
	}

	private <H extends AbstractHeader, T extends AbstractHeader.Tag> int[] getIntArrayHeader(
			H header, T tag) {
		AbstractHeader.Entry<?> entry = header.getEntry(tag);
		if (entry == null) {
			return new int[0];
		}
		return (int[]) (int[]) entry.getValues();
	}
}
