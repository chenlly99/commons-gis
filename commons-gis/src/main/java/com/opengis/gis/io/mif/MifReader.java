package com.opengis.gis.io.mif;

import java.io.File;
import java.util.Iterator;

import com.opengis.gis.geo.FeatureCollection;
import com.opengis.gis.io.FileIterator;



/**
 * 
 * @author chenlly(chenlly@126.com)
 * 
 */
public class MifReader implements Iterator<MifFeature> {

	private FileIterator mid;
	private FileIterator mif;
	private MifSchema schema;


	public MifReader(String file) {
		String uc = file.toUpperCase();
		if (uc.endsWith(".MID") || uc.endsWith(".MIF")) {
			file = file.substring(0, file.length() - 4);
		}
		String path = new File(file).getAbsolutePath();
		File mid = new File(path + ".MID");
		if (!mid.exists())
			throw new RuntimeException("File is not found "
					+ mid.getAbsolutePath());
		File mif = new File(path + ".MIF");
		if (!mif.exists())
			throw new RuntimeException("File is not found "
					+ mif.getAbsolutePath());

		this.mid = new FileIterator(mid.getAbsolutePath(), "GBK");
		this.mif = new FileIterator(mif.getAbsolutePath(), "GBK");

		schema = this.create(this.mif);
	}
	

	public MifReader(String file , String charset) {
		String uc = file.toUpperCase();
		if (uc.endsWith(".MID") || uc.endsWith(".MIF")) {
			file = file.substring(0, file.length() - 4);
		}
		String path = new File(file).getAbsolutePath();
		File mid = new File(path + ".MID");
		if (!mid.exists())
			throw new RuntimeException("File is not found "
					+ mid.getAbsolutePath());
		File mif = new File(path + ".MIF");
		if (!mif.exists())
			throw new RuntimeException("File is not found "
					+ mif.getAbsolutePath());

		this.mid = new FileIterator(mid.getAbsolutePath(), charset);
		this.mif = new FileIterator(mif.getAbsolutePath(), "GBK");

		schema = this.create(this.mif);
	}
	

	public MifSchema getSchema() {
		return schema;
	}

	public FeatureCollection read() {
		FeatureCollection fc = new FeatureCollection(schema);
		while (hasNext()) {
			fc.add(next());
		}
		this.close();
		return fc;
	}

	public boolean hasNext() {
		boolean rtn = mid.hasNext();
		if (!rtn) {
			close();
		}
		return rtn;
	}

	public MifFeature next() {
		if (!mid.hasNext()) {
			close();
			throw new IllegalStateException();
		}
		try {
			MifFeature item = new MifFeature(schema);
			while (mif.hasNext()) {
				String mifLine = (String) mif.next();
				String fs = mifLine.toLowerCase();
				if (fs.startsWith("point")) {
					MifFeatureBuilder.pointFeature(item, mifLine, mif, mid);
					break;
				} else if (fs.startsWith("multipoint")) {
					MifFeatureBuilder.mpointFeature(item, mifLine, mif, mid);
					break;
				} else if (fs.startsWith("line")) {
					MifFeatureBuilder.lineFeature(item, mifLine, mif, mid);
					break;
				} else if (fs.startsWith("pline multiple")) {
					MifFeatureBuilder.mplineFeature(item, mifLine, mif, mid);
					break;
				} else if (fs.startsWith("pline")) {
					MifFeatureBuilder.plineFeature(item, mifLine, mif, mid);
					break;
				} else if (fs.startsWith("region")) {
					MifFeatureBuilder.regionFeature(item, mifLine, mif, mid);
					break;
				} else if (fs.startsWith("none")) {
					MifFeatureBuilder.noneFeature(item, mifLine, mif, mid);
					break;
				}
				// TODO
			}
			return item;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Mif line:" + mif.getLineNumber()
					+ ", Mid line:" + mid.getLineNumber(), e);
		}
	}

	public void remove() {
		// do nothing
	}

	public void close() {
		mid.close();
		mif.close();
	}

	public  MifSchema create(FileIterator mif) {
		MifSchema schema = new MifSchema();
		StringBuffer buf = new StringBuffer();

		String mifLine, mifLineLowerCase;
		while (true) {
			mifLineLowerCase = (mifLine = mif.next()).toLowerCase();
			if (mifLineLowerCase.startsWith("columns")) {
				int len = Integer.parseInt(mifLine.trim().split(" ")[1]);
				for (int i = 0; i < len; i++) {
					String col = mif.next().trim();
					
					int idx = col.indexOf(" ");
					schema.addAttribute(col.substring(0, idx),
							col.substring(idx + 1));
				}
			} else if (mifLineLowerCase.startsWith("data")) {
				schema.setData(mifLine);
				break;
			} else {
				buf.append(mifLine).append("\r\n");
				if (mifLineLowerCase.startsWith("delimiter")) {
					int idx = mifLine.indexOf("\"");
					schema.setDelimiter(mifLine.substring(idx + 1, idx + 2));
				}
			}
		}
		schema.setMeta(buf.toString());
/*		for(int i=0;i<schema.getAttributeCount();i++){
			System.out.println(schema.getAttributeName(i));
		}*/
		return schema;
	}
}
