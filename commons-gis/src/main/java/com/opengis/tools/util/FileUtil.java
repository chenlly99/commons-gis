package  com.opengis.tools.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * 文件管理帮助类 author chenll Date 2011-5-11
 */
public class FileUtil {

	public static Logger log = Logger.getLogger("FileUtil");

	
	/**
	 * 
	 * @param fileName
	 * @param fileBody
	 * @param charSet 字符集
	 * @param append  是否追加
	 */
	public static synchronized void writeFile(String fileName, String fileBody,String charSet,boolean append) {
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;
		BufferedWriter bw = null;
		try {
			fos = new FileOutputStream(fileName,append);
			osw = new OutputStreamWriter(fos, charSet);
			bw = new BufferedWriter(osw);
			bw.write(fileBody);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (osw != null) {
				try {
					osw.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * 创建新文件
	 * 
	 * @param path
	 */
	public static synchronized void newFile(String path) {
		if (StringUtils.isEmpty(path))
			throw new RuntimeException("[文件路径为空]");
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			log.info("文件创建失败[" + path + "]");
			e.printStackTrace();
		}
	}

	/**
	 * 新建目录
	 * 
	 * @param folderPath
	 *            String 如 c:/fqf
	 * @return boolean
	 */
	public static synchronized void newFolder(String folderPath) {
		try {
			String filePath = folderPath;
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				myFilePath.mkdirs();
			}
		} catch (Exception e) {
			log.info("目录创建失败[" + folderPath + "]");
			e.printStackTrace();
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            String 文件路径及名称 如c:/fqf.txt
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static synchronized void delFile(String filePathAndName) {
		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			java.io.File myDelFile = new java.io.File(filePath);
			if (myDelFile.exists() && myDelFile.isFile())
				myDelFile.delete();

		} catch (Exception e) {
			log.info("删除文件失败[" + filePathAndName + "]");
			e.printStackTrace();
		}
	}

	/**
	 * 删除目录
	 * 
	 * 
	 * @param filePathAndName
	 *            String 文件夹路径及名称 如c:/fqf
	 * @param fileContent
	 *            String
	 * @return boolean
	 */
	public static synchronized void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			if (myFilePath.exists())
				myFilePath.delete(); // 删除空文件夹

		} catch (Exception e) {
			log.info("删除目录失败[" + folderPath + "]");
			e.printStackTrace();
		}
	}

	/**
	 * 删除文件夹里面的所有文件
	 * 
	 * 
	 * @param path
	 *            String 目录路径 如 c:/fqf 或者c:/fqf/
	 */
	public static synchronized void delAllFile(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (!file.isDirectory()) {
			return;
		}
		// 逐步删除
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹

			}
		}
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static synchronized void copyFile(String oldPath, String newPath) {

		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				inStream = new FileInputStream(oldPath); // 读入原文件
				fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[8192];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					fs.write(buffer, 0, byteread);
				}
			}
		} catch (Exception e) {
			log.info("复制单个文件操作出错[oldPath=" + oldPath + ", newPath=" + newPath
					+ "]");
			e.printStackTrace();
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
				if (fs != null) {
					fs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 复制整个目录内容
	 * 
	 * 
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf/ff
	 * @return boolean
	 */
	public static synchronized void copyFolder(String oldPath, String newPath) {

		FileInputStream input = null;
		FileOutputStream output = null;
		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}
				if (temp.isFile()) {
					input = new FileInputStream(temp);
					output = new FileOutputStream(newPath + "/"
							+ (temp.getName()).toString());
					byte[] b = new byte[8192];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
				}
				if (temp.isDirectory()) {// 如果是子文件夹

					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			log.info("复制整个文件夹内容操作出错[oldPath=" + oldPath + ", newPath="
					+ newPath + "]");
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 移动文件到指定目录
	 * 
	 * 
	 * @param oldPath
	 *            String 如：c:/fqf.txt
	 * @param newPath
	 *            String 如：d:/fqf.txt
	 */
	public static synchronized void moveFile(String oldPath, String newPath) {
		copyFile(oldPath, newPath);
		delFile(oldPath);
	}

	/**
	 * 
	 * @param path
	 * @param fileName
	 * @return
	 * @throws FileNotFoundException
	 */
	public static synchronized List<String> readeFile(String fileName) throws FileNotFoundException {
		InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
		List<String> list = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();
			while (!StringUtils.isEmpty(line)) {
				list.add(line.trim());
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			log.info("配置文件不存在[" + fileName + "]");
		} catch (IOException e) {
			log.info("读取配置文件失败[" + fileName + "]");
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 读取配置文件
	 * 
	 * @param fileName
	 *            文件名
	 * @return
	 */
	public static HashMap<String, String> getConfig(String fileName) {
		InputStream in = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
		HashMap<String, String> hm = new HashMap<String, String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();
			while (line != null) {
				if (!"".equals(line)) {
					if (line.indexOf("#") == -1) {
						String stage[] = line.split("=");
						if (stage.length == 2) {
							hm.put(stage[0].trim(), stage[1].trim());
						}
					}
				}
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			log.info("配置文件不存在[" + fileName + "]");
		} catch (IOException e) {
			log.info("读取配置文件失败[" + fileName + "]");
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return hm;
	}

	/**
	 * 获得指定路径下以指定格式结尾的所有文件
	 * 
	 * @param path
	 * @param end
	 * @return
	 */
	public static List<File> getEndFiles(String path, String end,
			List<File> fileEndList) {
		if (StringUtils.isEmpty(path))
			throw new RuntimeException("[文件路径为空]");
		File f[] = new File(path).listFiles();
		if(f==null || f.length==0) return null;
		for (int i = 0; i < f.length; i++) {
			File file = f[i];
			if (file.isFile()
					&& file.getName().toUpperCase().endsWith(end.toUpperCase())) {
				fileEndList.add(file);
			} else if (file.isDirectory()) {
				getEndFiles(file.getAbsolutePath(), end, fileEndList);
			}
		}
		return fileEndList;
	}

	/**
	 * 获得指定路径下以指定格式开头的所有文件
	 * 
	 * @param path
	 * @param start
	 * @return
	 */
	public static List<File> getStartFiles(String path, String start,
			List<File> fileStartList) {
		if (StringUtils.isEmpty(path))
			throw new RuntimeException("[文件路径为空]");
		File[] f = new File(path).listFiles();
		if(f==null || f.length==0) return null;
		for (int i = 0; i < f.length; i++) {
			File file = f[i];
			if (file.isFile()
					&& file.getName().toUpperCase()
							.startsWith(start.toUpperCase())) {
				fileStartList.add(file);
			} else if (file.isDirectory()) {
				getStartFiles(file.getAbsolutePath(), start, fileStartList);
			}
		}
		return fileStartList;
	}

	/**
	 * 获得指定路径下以指定格式开头并且以指定格式结尾的所有文件
	 * 
	 * @param path
	 * @param end
	 * @return
	 */
	public static List<File> getAllFiles(String path, String start, String end,
			List<File> fileAlltList) {
		if (StringUtils.isEmpty(path))
			throw new RuntimeException("[文件路径为空]");
		File f[] = new File(path).listFiles();
		if(f==null || f.length==0) return null;
		for (int i = 0; i < f.length; i++) {
			File file = f[i];
			boolean flag1 = file.isFile();
			boolean flag2 = file.getName().toUpperCase()
					.startsWith(start.toUpperCase());
			boolean flag3 = file.getName().toUpperCase()
					.endsWith(end.toUpperCase());
			if (flag1 && flag2 && flag3) {
				fileAlltList.add(file);
			} else if (file.isDirectory()) {
				getAllFiles(file.getAbsolutePath(), start, end, fileAlltList);
			}
		}
		return fileAlltList;
	}
}
