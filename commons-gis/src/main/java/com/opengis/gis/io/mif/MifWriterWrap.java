package com.opengis.gis.io.mif;


/**  

 * Class MifWriterWrap.java 

 * Description 

 * Company mapbar 

 * author Chenll E-mail: Chenll@mapbar.com

 * Version 1.0 

 * Date 2012-3-1 ����03:49:39

 */
public class MifWriterWrap {
	
	private MifWriter fw;
	
	
	public MifWriterWrap(String file){
		fw = new MifWriter(file);
	}
	public MifWriterWrap(String file,String charset){
		fw = new MifWriter(file,charset);
	}
	
	public MifWriterWrap(String file,boolean append){
		fw = new MifWriter(file,append);
	}
	
	public void  writerHead(String schema){
		fw.writeln(MifWriter.WType.MIF, schema);
	}
	
	public void  writerHead(MifFeature feature){
		fw.writeln(MifWriter.WType.MIF, feature.getSchema().toString());
	}
	
	public void  appWriterln(MifFeature feature){
		fw.writeln(MifWriter.WType.MIF, feature.toMifString());
		fw.writeln(MifWriter.WType.MID, feature.toMidString());
	}
	

	public void close(){
		fw.close();
	}
	
	public void flush(){
		fw.flush();
	}
}
