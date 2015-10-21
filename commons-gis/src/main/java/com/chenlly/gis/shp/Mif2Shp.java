package com.chenlly.gis.shp;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.shapefile.ShapefileFeatureLocking;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.chenlly.gis.io.mif.MifFeature;
import com.chenlly.gis.io.mif.MifReader;
import com.chenlly.gis.io.mif.MifSchema;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class Mif2Shp
{
	private class FileScan
	{
		public List<File> getFiles(String strRoot, int flag)
		{
			if (flag == 1)
			{
				return this.scanFileShp(new File(strRoot));
			}
			else if(flag == 2)
			{
				return this.scanFileMif(new File(strRoot));
			}
			return null;
		}

		private List<File> scanFileMif(File root)
		{
			List<File> fileInfo = new ArrayList<File>();

			File[] files = root.listFiles(new FileFilter()
			{
				public boolean accept(File pathname)
				{
					// 去掉隐藏文件夹
					if (pathname.isDirectory() && !pathname.isHidden())
					{
						return true;
					}

					// 去掉隐藏文件
					if (pathname.isFile()
							&& pathname.getName().toLowerCase().endsWith("mif"))
					{
						return true;
					}

					return false;
				}
			});

			for (File file : files)
			{
				// 逐个遍历文件
				if (file.isDirectory())
				{
					// 如果是文件夹，则进一步遍历，属于递归算法
					List<File> ff = scanFileMif(file);
					fileInfo.addAll(ff);
				}
				else
				{
					fileInfo.add(file); // 如果不是文件夹，则添加入文件列表
				}
			}

			return fileInfo;
		}
		
		private List<File> scanFileShp(File root)
		{
			List<File> fileInfo = new ArrayList<File>();

			File[] files = root.listFiles(new FileFilter()
			{
				public boolean accept(File pathname)
				{
					// 去掉隐藏文件夹
					if (pathname.isDirectory() && !pathname.isHidden())
					{
						return true;
					}

					// 去掉隐藏文件
					if (pathname.isFile()
							&& pathname.getName().toLowerCase().endsWith("shp"))
					{
						return true;
					}

					return false;
				}
			});

			for (File file : files)
			{
				// 逐个遍历文件
				if (file.isDirectory())
				{
					// 如果是文件夹，则进一步遍历，属于递归算法
					List<File> ff = scanFileShp(file);
					fileInfo.addAll(ff);
				}
				else
				{
					fileInfo.add(file); // 如果不是文件夹，则添加入文件列表
				}
			}

			return fileInfo;
		}
	}

	@SuppressWarnings("rawtypes")
	static Map<String, Class> typeMap = new HashMap<String, Class>();

	@SuppressWarnings("rawtypes")
	static Map<Class, String> typeEncode = new HashMap<Class, String>();

	static
	{
		typeEncode.put(String.class, "String");
		typeMap.put("String", String.class);
		typeMap.put("string", String.class);
		typeMap.put("\"\"", String.class);

		typeEncode.put(Integer.class, "Integer");
		typeMap.put("Integer", Integer.class);
		typeMap.put("int", Integer.class);
		typeMap.put("0", Integer.class);

		typeEncode.put(Double.class, "Double");
		typeMap.put("Double", Double.class);
		typeMap.put("double", Double.class);
		typeMap.put("0.0", Double.class);

		typeEncode.put(Float.class, "Float");
		typeMap.put("Float", Float.class);
		typeMap.put("float", Float.class);
		typeMap.put("0.0f", Float.class);

		typeEncode.put(Boolean.class, "Boolean");
		typeMap.put("Boolean", Boolean.class);
		typeMap.put("true", Boolean.class);
		typeMap.put("false", Boolean.class);

		typeEncode.put(Geometry.class, "Geometry");
		typeMap.put("Geometry", Geometry.class);

		typeEncode.put(Point.class, "Point");
		typeMap.put("Point", Point.class);

		typeEncode.put(LineString.class, "LineString");
		typeMap.put("LineString", LineString.class);

		typeEncode.put(Polygon.class, "Polygon");
		typeMap.put("Polygon", Polygon.class);

		typeEncode.put(MultiPoint.class, "MultiPoint");
		typeMap.put("MultiPoint", MultiPoint.class);

		typeEncode.put(MultiLineString.class, "MultiLineString");
		typeMap.put("MultiLineString", MultiLineString.class);

		typeEncode.put(MultiPolygon.class, "MultiPolygon");
		typeMap.put("MultiPolygon", MultiPolygon.class);

		typeEncode.put(GeometryCollection.class, "GeometryCollection");
		typeMap.put("GeometryCollection", GeometryCollection.class);

		typeEncode.put(Date.class, "Date");
		typeMap.put("Date", Date.class);
	}

	@SuppressWarnings("rawtypes")
	static Class CTypeMap(String typeName) throws ClassNotFoundException
	{
		if (typeMap.containsKey(typeName))
		{
			return (Class) typeMap.get(typeName);
		}

		return Class.forName(typeName);
	}

	public static boolean isReadConfig()
	{
		return isReadConfig;
	}

	public static void main(String[] args)
	{
		if (args.length != 2)
		{
			System.out.println(args[0]);
			System.out.println("参数错误");
		}
		Mif2Shp m = new Mif2Shp();
		if (args[0].toLowerCase().equals("-mif2shp"))
		{
			System.out.print("Mif2Shp开始");
			m.transitionMif2Shp(args[1]);
			System.out.print("Mif2Shp结束");
		}
		else if (args[0].toLowerCase().equals("-shp2mif"))
		{
			System.out.print("Shp2Mif开始");
			m.transitionShp2Mif(args[1]);
			System.out.print("Shp2Mif结束");
		}
		else
		{
			System.out.println(args[0]);
			System.out.println("参数错误");
		}

		// if (args.length == 0 || args.length > 2)
		// {
		// System.out.println(args[0]);
		// System.out.println("参数错误");
		// }
		// else if (args.length == 1
		// && args[0].toLowerCase().equals("-readconfig"))
		// {
		// System.out.println("读取配置文件中的内容");
		// Mif2Shp.isReadConfig = true;
		// Mif2Shp mif2Shp = new Mif2Shp();
		// mif2Shp.transition("", "");
		//
		// }
		// else if (args.length == 1
		// && !args[0].toLowerCase().equals("-readconfig"))
		// {
		// System.out.println("不读取配置文件中的内容");
		// if (!args[0].contains("data"))
		// {
		// System.out.println("错误：必须把数据放到data文件夹下面");
		// return;
		// }
		// Mif2Shp.isReadConfig = false;
		// Mif2Shp mif2Shp = new Mif2Shp();
		// mif2Shp.transitionShp2Mif(args[0]);
		//
		// }
		// else if (args.length == 2)
		// {
		// System.out.println("不读取配置文件中的内容");
		// Mif2Shp.isReadConfig = false;
		// Mif2Shp mif2Shp = new Mif2Shp();
		// mif2Shp.transition(args[0], args[1]);
		// }
	}

	private String mifPath = "";

	private String shpPath = "";

	private String mifDirPath = "";

	private String[] expectedColumns;

	private String[] expectedAddColumns;

	private static boolean isReadConfig = false;

	private FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(
			String mifPath, ShapefileDataStore shapefileDataStore)
			throws IOException
	{
		//FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = SimpleFeatureBuilder.
		List<SimpleFeature> list = new ArrayList<SimpleFeature>();
		SimpleFeatureBuilder featureBuilder = null;
		featureBuilder = new SimpleFeatureBuilder(shapefileDataStore.getSchema());

		ArrayList<Map.Entry<String, String>> arrayList = this.getMifFileAttributeTypeAndAttributeName(mifPath);

		MifReader mifReader = new MifReader(mifPath);

		int allAttributeCount = arrayList.size();
		while (mifReader.hasNext())
		{
			MifFeature mf = mifReader.next();
			Object[] obj = new Object[allAttributeCount + 1];
			obj[0] = mf.getGeometry();
			for (int i = 0; i < arrayList.size(); i++)
			{
				String attributeName = arrayList.get(i).getValue();

				if (Mif2Shp.isReadConfig)
				{
					boolean isAddColumn = false;

					for (int j = 0; j < this.expectedAddColumns.length; j++)
					{
						if (this.expectedAddColumns[j]
								.equalsIgnoreCase(attributeName.toLowerCase()))
						{
							isAddColumn = true;
							break;
						}
					}

					if (!isAddColumn)
					{
						String attribute = mf.getAttribute(attributeName)
								.toString();
						obj[i + 1] = attribute;
					}
					else
					{
						obj[i + 1] = "";
					}
				}
				else
				{
					String attribute = mf.getAttribute(attributeName)
							.toString();
					obj[i + 1] = attribute;
				}
			}

			// 构造一个Feature
			SimpleFeature feature = featureBuilder.buildFeature(null, obj);
			// 添加到集合
			list.add(feature);
		}

		mifReader.close();
		
		return DataUtilities.collection(list);
	}

	/*
	 * 创建shape数据源
	 */
	private ShapefileDataStore createShapeFile(
			String shapePath,
			ArrayList<Map.Entry<String, String>> mifFileAttributeAndAttributeName,
			String mifGeometryType) throws IOException, SchemaException
	{
		File shapeFile = new File(shapePath);

		String typeSpec = this.getTypeSpec(mifFileAttributeAndAttributeName,
				mifGeometryType);

		String namespace = "MAPBAR";
		String identification = "MIF2SHP";
		SimpleFeatureType simpleFeatureType = null;
		try
		{
			simpleFeatureType = this.createSimpleFeatureType(namespace,
					identification, typeSpec);
			simpleFeatureType = DataUtilities.createSubType(simpleFeatureType,
					null, DefaultGeographicCRS.WGS84);
			simpleFeatureType.getCoordinateReferenceSystem();
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("[创建shape数据源失败]");
			e.printStackTrace();
		}

		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", (Serializable) shapeFile.toURI().toURL());
		params.put("create spatial index", (Serializable) Boolean.TRUE);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		ShapefileDataStore shapefileDataStore = (ShapefileDataStore) dataStoreFactory
				.createNewDataStore(params);
		shapefileDataStore.setStringCharset(Charset.forName("GBK"));
		shapefileDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
		shapefileDataStore.createSchema(simpleFeatureType);

		return shapefileDataStore;
	}

	private SimpleFeatureType createSimpleFeatureType(String namespace,
			String typeName, String typeSpec) throws SchemaException,
			ClassNotFoundException
	{
		SimpleFeatureTypeBuilder simpleFeatureTypeBuilder = new SimpleFeatureTypeBuilder();
		simpleFeatureTypeBuilder.setName(typeName);
		simpleFeatureTypeBuilder.setNamespaceURI(namespace);
		simpleFeatureTypeBuilder.setCRS(DefaultGeographicCRS.WGS84);

		String[] types = typeSpec.split(",");

		for (int i = 0; i < types.length; i++)
		{
			/*
			 * 字符串分割
			 * record = MAPID:String:8
			 * name=MAPID
			 * type=String
			 * typeLength=8
			 * 
			 * record = location:Polygon
			 * name=location
			 * type=Polygon
			 * typeLength=-1
			 */
			String record = types[i];
			int split = record.indexOf(":");
			String name;
			String type;
			int typeLength = -1;
			name = record.substring(0, split);
			int split2 = record.indexOf(":", split + 1);
			if (split2 == -1)
			{
				type = record.substring(split + 1);
			}
			else
			{
				type = record.substring(split + 1, split2);
				typeLength = Integer.parseInt(record.substring(split2 + 1));
			}

			// 定义属性
			AttributeTypeBuilder attributeTypeBuilder = new AttributeTypeBuilder();
			if (typeLength != -1)
			{
				attributeTypeBuilder.setLength(typeLength);
			}
			attributeTypeBuilder.setBinding(CTypeMap(type));
			AttributeDescriptor attributeDescriptor = attributeTypeBuilder
					.buildDescriptor(name);

			// 增加到SimpleFeatureTypeBuilder
			simpleFeatureTypeBuilder.add(attributeDescriptor);
		}

		return simpleFeatureTypeBuilder.buildFeatureType();
	}

	@SuppressWarnings("unused")
	private void createTransaction(
			FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
			ShapefileFeatureLocking featureSource)
	{
		// 创建一个事务
		Transaction transaction = new DefaultTransaction("create");
		featureSource.setTransaction(transaction);

		try
		{
			featureSource.addFeatures(featureCollection);
			// 提交事务
			transaction.commit();
		}
		catch (IOException e1)
		{
			System.out.println("[写shape文件事物提交失败]");
			e1.printStackTrace();
			try
			{
				transaction.rollback();
			}
			catch (IOException e2)
			{
				System.out.println("[写shape文件事物回滚失败]");
				e2.printStackTrace();
			}
		}
		finally
		{
			try
			{
				transaction.close();
			}
			catch (IOException e)
			{
				System.out.println("[写shape文件事物关闭失败]");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取mif文件的字段类型和字段名称 Key:字段类型 Value:字段名称
	 */
	private ArrayList<Map.Entry<String, String>> getMifFileAttributeTypeAndAttributeName(
			String mifPath)
	{
		ArrayList<Map.Entry<String, String>> arrayList = new ArrayList<Map.Entry<String, String>>();

		MifReader mifReader = new MifReader(mifPath);
		MifSchema mifSchema = mifReader.getSchema();
		int attributeCount = mifSchema.getAttributeCount();

		for (int i = 0; i < attributeCount; i++)
		{
			String attributeName = mifSchema.getAttributeName(i).toUpperCase();
			String attributeType = mifSchema.getAttributeType(i).toUpperCase();

			if (Mif2Shp.isReadConfig)
			{
				for (int j = 0; j < this.expectedColumns.length; j++)
				{
					if (this.expectedColumns[j].equalsIgnoreCase(attributeName))
					{
						arrayList
								.add(new AbstractMap.SimpleEntry<String, String>(
										attributeType, attributeName));
						break;
					}
				}
			}
			else
			{
				arrayList.add(new AbstractMap.SimpleEntry<String, String>(
						attributeType, attributeName));
			}
		}

		if (Mif2Shp.isReadConfig)
		{
			for (int i = 0; i < this.expectedAddColumns.length; i++)
			{
				arrayList.add(new AbstractMap.SimpleEntry<String, String>(
						"Char(60)", this.expectedAddColumns[i]));
			}
		}

		mifReader.close();

		return arrayList;
	}

	/*
	 * 获取mif文件的几何类型。可以有优化
	 */
	private String getMifFileGeometryType(String mifPath)
	{
		String geometryType = "";

		MifReader mifReader = new MifReader(mifPath);

		while (mifReader.hasNext())
		{
			MifFeature mf = mifReader.next();
			geometryType = mf.getGeometry().getGeometryType();
			break;
		}

		mifReader.close();

		return geometryType;
	}

	/*
	 * mif数据类型和shp数据类型转换
	 */
	private String getType(String attribute)
	{
		if (attribute.toUpperCase().startsWith("C"))
		{
			// 截取char类型长度。char(8)，attributeLength=8
			int beginBracketIndex = attribute.indexOf("(");
			int endBracketIndex = attribute.indexOf(")");
			String attributeLength = attribute.substring(beginBracketIndex + 1,
					endBracketIndex);

			return "String:" + attributeLength;
		}
		else if (attribute.toUpperCase().startsWith("I") || attribute.toUpperCase().equals("SMALLINT"))
		{
			return "Integer";
		}
		else if (attribute.toUpperCase().startsWith("F"))
		{
			return "Double";
		}
		return "String";
	}

	private String getTypeSpec(
			ArrayList<Map.Entry<String, String>> mifFileAttributeAndAttributeName,
			String mifGeometryType)
	{
		String typeSpec = "location:" + mifGeometryType + ",";

		for (int i = 0; i < mifFileAttributeAndAttributeName.size(); i++)
		{
			String attributeName = mifFileAttributeAndAttributeName.get(i)
					.getValue();
			String attributeType = this
					.getType(mifFileAttributeAndAttributeName.get(i).getKey());

			typeSpec += attributeName + ":" + attributeType + ",";
		}

		return typeSpec.substring(0, typeSpec.length() - 1);// 截取最后一个','
	}

	/*private void readConfig()
	{
		String config = "shpConfig" + File.separator
				+ "shpDataTable.properties";
		ShpConfig shpConfig = new ShpConfig(config);
		this.mifPath = shpConfig.getInput();
		this.shpPath = shpConfig.getOutput();

		if (shpPath.trim().length() == 0)
		{
			this.mifDirPath = this.mifPath;
		}

		this.expectedColumns = shpConfig.getColumns();
		this.expectedAddColumns = shpConfig.getAddColumns();
	}*/

	public void setReadConfig(boolean isReadConfig)
	{
		Mif2Shp.isReadConfig = isReadConfig;
	}

	public void transitionShp2Mif(String shpDirPath)
	{
		this.shpPath = shpDirPath;

		FileScan fileScan = new FileScan();
		List<File> shpFiles = fileScan.getFiles(this.shpPath, 1);
		if(shpFiles == null || shpFiles.size() == 0)
		{
			System.err.println("亲，放东西了吗？敢放点正常点的东西吗？？");
		}
		for (int i = 0; i < shpFiles.size(); i++)
		{
			/**
			 * 注意：shp数据必须放在data文件夹内
			 * 与data文件夹平级建立文件夹data_mif
			 * 把转换后的mif数据放到data_mif文件夹中
			 * 转换后的mif数据和shp数据名称一致，后缀名为mif
			 */
			File tempShpFile = shpFiles.get(i);
			String tempShpFilePath = tempShpFile.getPath()
					.replace("SHP", "shp");
			String tempShpFileName = tempShpFile.getName();
			String tempMifFilePath = tempShpFilePath
					.replace("data", "data_mif");
			tempMifFilePath = tempMifFilePath.subSequence(0,
					tempMifFilePath.lastIndexOf("\\")).toString();

			File tempMifFile = new File(tempMifFilePath);
			tempMifFile.mkdirs();

			// 把mif文件后缀名改为shp(mif文件后缀名为MIF或者mif)
			if (tempShpFileName.endsWith("SHP"))
			{
				tempMifFilePath += "\\" + tempShpFileName.replace("SHP", "mif");
			}
			else if (tempShpFileName.endsWith("shp"))
			{
				tempMifFilePath += "\\" + tempShpFileName.replace("shp", "mif");
			}

			System.out
					.println(tempShpFilePath + "---转换为:---" + tempMifFilePath);

			// 调用chenll的程序
			Shp2Mif a = new Shp2Mif();
			a.transition(tempShpFilePath, tempMifFilePath);
		}
	}

	public void transitionMif2Shp(String mifDirPath)
	{
		if (mifDirPath.equals(""))
		{
			//this.readConfig();
		}
		else
		{
			this.mifDirPath = mifDirPath;
		}
		FileScan fileScan = new FileScan();
		List<File> mifFiles = fileScan.getFiles(this.mifDirPath, 2);
		if(mifFiles == null || mifFiles.size() == 0)
		{
			System.err.println("亲，放东西了吗？敢放点正常点的东西吗？？");
		}
		for (int i = 0; i < mifFiles.size(); i++)
		{
			/**
			 * 注意：mif数据必须放在data文件夹内
			 * 与data文件夹平级建立文件夹data_shp
			 * 把转换后的shp数据放到data_shp文件夹中
			 * 转换后的shp数据和mif数据名称一致，后缀名为shp
			 */
			File tempMifFile = mifFiles.get(i);
			String tempMifFilePath = tempMifFile.getPath()
					.replace("MIF", "mif");
			String tempMifFileName = tempMifFile.getName();
			String tempShpFilePath = tempMifFilePath
					.replace("data", "data_shp");
			tempShpFilePath = tempShpFilePath.subSequence(0,
					tempShpFilePath.lastIndexOf("\\")).toString();

			File tempShpFile = new File(tempShpFilePath);
			tempShpFile.mkdirs();

			// 把mif文件后缀名改为shp(mif文件后缀名为MIF或者mif)
			if (tempMifFileName.endsWith("MIF"))
			{
				tempShpFilePath += "\\" + tempMifFileName.replace("MIF", "shp");
			}
			else if (tempMifFileName.endsWith("mif"))
			{
				tempShpFilePath += "\\" + tempMifFileName.replace("mif", "shp");
			}

			System.out
					.println(tempMifFilePath + "---转换为:---" + tempShpFilePath);
			this.transition(tempMifFilePath, tempShpFilePath);
		}
	}

	/**
	 * 1.读取配置文件
	 * 
	 * 2.读取mif文件的字段属性和字段值
	 * 
	 * 3.读取mif文件的几何类型
	 * 
	 * 4.创建shp数据源
	 * 
	 * 5.创建要素集合
	 * 
	 * 6.写入shp文件
	 */
	public void transition(String mifPath, String shpPath)
	{

		if (mifPath.equals("") && shpPath.equals(""))
		{
			//this.readConfig();
		}
		else
		{
			this.mifPath = mifPath;
			this.shpPath = shpPath;
		}
		System.out.println("MifPath:" + this.mifPath);
		System.out.println("ShpPath:" + this.shpPath);

		ArrayList<Map.Entry<String, String>> mifFileAttributeAndAttributeName = this
				.getMifFileAttributeTypeAndAttributeName(this.mifPath);
		String mifGeometryType = this.getMifFileGeometryType(this.mifPath);
		if (mifGeometryType.length() == 0)
		{
			System.out.println("MifPath:" + this.mifPath + "不能转换，没有几何数据类型");
			return;
		}

		ShapefileDataStore shapefileDataStore = null;
		try
		{
			shapefileDataStore = this.createShapeFile(this.shpPath,
					mifFileAttributeAndAttributeName, mifGeometryType);
		}
		catch (SchemaException e)
		{
			System.out.println("[创建shape文件失败]" + this.mifPath);
			e.printStackTrace();
			return;
		}
		catch (IOException e)
		{
			System.out.println("[创建shape文件失败]" + this.mifPath);
			e.printStackTrace();
			return;
		}

		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
		try
		{
			featureCollection = this.createFeatureCollection(this.mifPath,
					shapefileDataStore);
		}
		catch (IOException e)
		{
			System.out.println("[创建shape文件要素失败]" + this.mifPath);
			e.printStackTrace();
			return;
		}

		try
		{
			this.writeToShapefile(shapefileDataStore, featureCollection);
		}
		catch (IOException e)
		{
			System.out.println("[写shape文件失败]" + this.mifPath);
			e.printStackTrace();
		}
	}

	private void writeToShapefile(
			ShapefileDataStore shapefileDataStore,
			FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection)
			throws IOException
	{
		/**
		 * 为节省内存，打算每100条数据写一次文件，但是索引创建总失败，待优化
		 * String typeName = shapefileDataStore.getTypeNames()[0];
		 * ShapefileFeatureLocking featureSource = null;
		 * 
		 * featureSource = (ShapefileFeatureLocking) shapefileDataStore
		 * .getFeatureSource(typeName);
		 * 
		 * FeatureIterator<SimpleFeature> iterator =
		 * featureCollection.features();
		 * try
		 * {
		 * int i = 0;
		 * FeatureCollection<SimpleFeatureType, SimpleFeature>
		 * tempFeatureCollection = FeatureCollections
		 * .newCollection();
		 * while (iterator.hasNext())
		 * {
		 * SimpleFeature feature = iterator.next();
		 * i++;
		 * tempFeatureCollection.add(feature);
		 * if (i % 100 == 0)
		 * {
		 * this.createTransaction(tempFeatureCollection, featureSource);
		 * tempFeatureCollection.clear();
		 * }
		 * }
		 * this.createTransaction(tempFeatureCollection, featureSource);
		 * }
		 * finally
		 * {
		 * iterator.close();
		 * }
		 */
		String typeName = shapefileDataStore.getTypeNames()[0];
		ShapefileFeatureLocking featureSource = null;

		featureSource = (ShapefileFeatureLocking) shapefileDataStore
				.getFeatureSource(typeName);

		Transaction transaction = new DefaultTransaction("create");
		featureSource.setTransaction(transaction);

		try
		{
			featureSource.addFeatures(featureCollection);
			transaction.commit();
		}
		catch (IOException e1)
		{
			System.out.println("[创建shape文件失败]");
			e1.printStackTrace();
			try
			{
				transaction.rollback();
			}
			catch (IOException e2)
			{
				System.out.println("[创建shape文件失败]");
				e2.printStackTrace();
			}
		}
		finally
		{
			try
			{
				transaction.close();
			}
			catch (IOException e)
			{
				System.out.println("[创建shape文件失败]");
				e.printStackTrace();
			}
		}
	}

}
