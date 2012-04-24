package ken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class TestShell {
	public static void main(String[] args) {
		Runtime run = Runtime.getRuntime();
		try {
			Process p = run.exec("bin/testshell.sh");// 启动另一个进程来执行命令
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
					System.out.print(name+"|");
					System.out.println(Integer.parseInt(name.substring(0,
							name.indexOf('@'))));
				} catch (Exception e) {
				}
				System.out.println("成功结束!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}