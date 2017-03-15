package com.opengis.gis.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;


public class FileWriters {

	protected static final Logger log = Logger.getLogger(FileWriters.class);

	// name->writer
	private Map<String, Writer> writers;
	// temp->filename, only filename exist
	private Map<String, String[]> names;
	private boolean backup;

	public FileWriters() {
		this(true);
	}

	public FileWriters(boolean backup) {
		writers = new LinkedHashMap<String, Writer>();
		names = new HashMap<String, String[]>();
		this.backup = backup;
	}

	public void put(String name, String fileName) {
		put(name, fileName, "GBK");
	}

	public void put(String name, String fileName, String encode) {
		if (writers.containsKey(name))
			return;
		try {
			fileName = putName(name, fileName);
			putWriter(name, new OutputStreamWriter(new FileOutputStream(
					fileName), encode));
		} catch (Exception e) {
			log.error(e);
		}
	}

	public String putName(String name, String fileName) {
		try {
			File file = new File(fileName);
			file.getParentFile().mkdirs();
			if (file.exists()) {
				file = File.createTempFile("fwp", ".tmp", file.getParentFile());
				names.put(name,
						new String[] { file.getAbsolutePath(), fileName });
			}
			return file.getAbsolutePath();
		} catch (Exception e) {
			log.error(e);
		}
		return fileName;
	}

	public void putWriter(String name, Writer writer) {
		writers.put(name, writer);
	}

	public Writer get(String name) {
		return writers.get(name);
	}

	public boolean has(String name) {
		return writers.containsKey(name);
	}

	public synchronized void writeln(String name, String message) {
		write(name, message + "\r\n");
	}

	public synchronized void write(String name, String message) {
		Writer writer = get(name);
		if (writer != null) {
			try {
				writer.append(message);
			} catch (Exception e) {
				log.error(e);
			}
		} else {
			log.error("missing " + name);
		}
	}

	public void close() {
		for (String name : writers.keySet()) {
			Writer writer = writers.get(name);
			if (writer != null) {
				try {
					writer.close();
					// Method m = writer.getClass().getMethod("close",
					// new Class[0]);
					// m.invoke(writer);
				} catch (Exception e) {
					log.error(e);
				}
			}
			String[] fileName = names.get(name);
			if (fileName != null) {
				File out = new File(fileName[1]);
				if (out.exists()) {
					if (backup) {
						out.renameTo(new File(fileName[1] + "_bak_"
								+ System.nanoTime()));
					} else {
						new File(fileName[1]).delete();
					}
				}
				new File(fileName[0]).renameTo(new File(fileName[1]));
			}
		}
		writers.clear();
		names.clear();
	}
}
