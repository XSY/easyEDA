package ken.event.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import ken.event.bus.Message;

import org.apache.commons.io.FileUtils;

/**
 * @author KennyZJ
 * 
 */
public class FileUtil {

	public static void writeLine(byte[] line, String directory, String filename) throws IOException {
		File dir = new File(directory);
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}
		File file = new File(dir, filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		RandomAccessFile rf = new RandomAccessFile(file, "rw");
		if (rf.length() != 0) {
			rf.seek(rf.length());
			rf.writeBytes("\n");
		}
		rf.write(line);
		rf.close();
	}

	public static void writeLines(List<byte[]> lines, String directory,
			String filename) throws IOException {
		File dir = new File(directory);
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}
		File file = new File(dir, filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		RandomAccessFile rf = new RandomAccessFile(file, "rw");
		if (rf.length() != 0) {
			rf.seek(rf.length());
		}
		for (int i = 0; i < lines.size(); i++) {
			rf.write(lines.get(i));
			if (i != lines.size() - 1) {
				rf.writeBytes("\n");
			}
		}
		rf.close();
	}

	

	public static void writeLineNIO(byte[] line, String directory,
			String filename) {
	    ByteBuffer buffer = ByteBuffer.allocate(1024);
		File dir = new File(directory);
		if (!dir.isDirectory()) {
			dir.mkdirs();
		}
		File file = new File(dir, filename);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			RandomAccessFile rf = new RandomAccessFile(file, "rw");
			FileChannel fc = rf.getChannel();

			buffer.put(line);
			buffer.put("\n".getBytes());
			buffer.flip();
			fc.write(buffer, rf.length());
			// fc.force(false);
			buffer.clear();
			fc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] readFile(String absolutfilename) throws Exception {
		File file = new File(absolutfilename);
		if (!file.exists()) {
			throw new FileNotFoundException();
		}
		return read(file);
	}

	public static byte[] readFile(String directory, String filename)
			throws Exception {
		return readFile(directory + File.separator + filename);
	}

	/**
	 * for reading tiny file size < Interger.MAX_VALUE in byte
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	private static byte[] read(File file) throws Exception {
		RandomAccessFile rf = new RandomAccessFile(file, "r");
		if (Integer.MAX_VALUE < rf.length()) {
			throw new Exception("file [" + file.getAbsolutePath()
					+ file.getName() + "] is too large to read one-time！");
		}
		byte[] content = new byte[Integer.parseInt(rf.length() + "")];
		rf.read(content);
		rf.close();
		return content;
	}

	public static void readFileInLine(String absolutfilename,
			List<String> result) throws IOException {
		File file = new File(absolutfilename);
		if (!file.exists()) {
			throw new FileNotFoundException("The file " + absolutfilename
					+ " does not exist!");
		}
		RandomAccessFile rf = new RandomAccessFile(file, "r");
		String s = "";
		while (true) {
			s = rf.readLine();
			if (s == null) {
				break;
			}
			result.add(s);
		}
		rf.close();
	}

	public static void readMsgFile(String absolutfilename, Message msg)
			throws IOException {
		File file = new File(absolutfilename);
		if (!file.exists()) {
			throw new FileNotFoundException();
		}
		RandomAccessFile rf = new RandomAccessFile(file, "r");
		msg.setId(rf.readLine().getBytes());
		msg.setPart1(rf.readLine().getBytes());
		msg.setPart2(rf.readLine().getBytes());
		byte[] left = new byte[Integer.parseInt((rf.length() - rf
				.getFilePointer()) + "")];
		rf.readFully(left);
		msg.setPart3(left);
		rf.close();
	}

	public static void readFileInLine(String directory, String filename,
			List<String> result) throws IOException {
		readFileInLine(directory + File.separator + filename, result);
	}

	/**
	 * native "mv" opt to file
	 * 
	 * @param file
	 * @param dir
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void mvFile(String file, String dir) throws IOException,
			InterruptedException {
		Runtime run = Runtime.getRuntime();
		Process p = run.exec("bin/router.sh move" + file + " " + dir);// 启动另一个进程来执行命令
		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
		BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
		String lineStr;
		while ((lineStr = inBr.readLine()) != null)
			// 获得命令执行后在控制台的输出信息
			System.out.println(lineStr);// 打印输出信息
		// 检查命令是否执行失败。
		if (p.waitFor() != 0) {
			if (p.exitValue() == 1)// p.exitValue()==0表示正常结束，1：非正常结束
				System.err.println("命令执行失败!");
		} else {
			RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
			String name = runtime.getName(); // format: "pid@hostname"
			try {
				System.out.print(name + "|");
				System.out.println(Integer.parseInt(name.substring(0,
						name.indexOf('@'))));
			} catch (Exception e) {
			}
			System.out.println("成功结束!");
		}
	}

	public static void moveTo(String srcfile, String targetDir)
			throws IOException {
		moveTo(srcfile, targetDir, null);
	}

	public static void moveTo(String srcfile, String targetDir, String fileType)
			throws FileNotFoundException {

		File src = new File(srcfile);
		if (!src.exists()) {
			throw new FileNotFoundException("No file in " + srcfile
					+ " to move!");
		}

		File target = new File(targetDir);
		if (src.isDirectory()) {
			if (!target.isDirectory()) {
				target.mkdirs();
				src.renameTo(target);
				src.mkdirs();
			} else {
				if (fileType != null && !"".equals(fileType)) {
					FileFilter filter = new FileTypeFilter(fileType);
					File[] srcfiles = src.listFiles(filter);
					if (srcfiles != null && srcfiles.length > 0) {
						for (File file : srcfiles) {
							moveTo(file.getAbsolutePath(), targetDir, fileType);
						}
					}
				} else {
					File[] srcfiles = src.listFiles();
					if (srcfiles != null && srcfiles.length > 0) {
						for (File file : srcfiles) {
							moveTo(file.getAbsolutePath(), targetDir, fileType);
						}
					}
				}
			}
		} else {
			if (!target.isDirectory()) {
				target.mkdirs();
			}
			File newfile = new File(targetDir + File.separator + src.getName());
			if (newfile.exists()) {
				newfile.delete();
			}
			src.renameTo(newfile);
		}
		// FileUtils.moveFileToDirectory(src, target, true);
	}

	public static void moveFileToDir(String srcfile, String targetdir)
			throws IOException {
		if (srcfile == null || targetdir == null || "".equals(srcfile)
				|| "".equals(targetdir)) {
			throw new IOException(
					"Neither source file nor target directory is allowed to be empty.");
		}
		FileUtils.moveFileToDirectory(new File(srcfile), new File(targetdir),
				true);
	}

	public static void moveDirContent(String srcdir, String targetdir,
			String filetype) throws IOException {
		if (srcdir == null || targetdir == null || "".equals(srcdir)
				|| "".equals(targetdir)) {
			throw new IOException(
					"Neither source directory nor target directory is allowed to be empty.");
		}
		File[] srcfiles;
		File src = new File(srcdir);
		File target = new File(targetdir);
		if (!src.isDirectory()) {
			throw new IOException("The srcdir:" + srcdir
					+ " is not a directory!");
		}
		if (!target.isDirectory()) {
			target.mkdirs();
		}
		if (filetype != null && !"".equals(filetype)) {
			srcfiles = listFiles(srcdir, filetype);
		} else {
			srcfiles = src.listFiles();
		}
		if (srcfiles != null && srcfiles.length > 0) {
			for (File f : srcfiles) {
				FileUtils.moveFileToDirectory(f, target, true);
			}
		}
	}

	public static boolean isDirEmpty(String dirPath, String fileType) {
		File dir = new File(dirPath);
		if (!dir.isDirectory()) {
			return true;
		}
		FileFilter filter = new FileTypeFilter(fileType);
		File[] files = dir.listFiles(filter);
		if (files == null || files.length == 0) {
			return true;
		}
		return false;
	}

	private static class FileTypeFilter implements FileFilter {
		private String _regex;

		public FileTypeFilter(String regex) {
			_regex = ".+" + regex;
		}

		@Override
		public boolean accept(File pathname) {
			if (!pathname.isFile()) {
				return false;
			}
			return pathname.getName().matches(_regex);
		}
	}

	/**
	 * delete and new
	 * 
	 * @param filename
	 * @throws IOException
	 */
	public static void clearFile(String filename) throws IOException {
		File file = new File(filename);
		if (file.exists()) {
			file.delete();
			file.createNewFile();
		}
	}

	public static File[] listFiles(String directory, String ext) {
		File dir = new File(directory);
		FileTypeFilter filter = new FileTypeFilter(ext);
		return dir.listFiles(filter);
	}

	public static void deleteFile(String filename) {
		File file = new File(filename);
		if (file.exists()) {
			file.delete();
		}
	}

}
