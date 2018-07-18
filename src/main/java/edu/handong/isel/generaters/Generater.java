package edu.handong.isel.generaters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Generater {

	public String getPathOfIm() {
		return pathOfIm;
	}

	public void setPathOfIm(String PathOfIm) {
		this.pathOfIm = PathOfIm;
	}

	String pathOfIm;

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	String dir;

	public static void main(String[] args) {
		Generater gn = new Generater();

		try {
			gn.run(args);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	private void run(String[] args) throws Exception {
		Generater gn = new Generater();

		System.out.println("start Program from... ");
		System.out.println(System.getProperty("user.dir"));

		gn.setDir("Data");
		gn.setPathOfIm("markov-text");

		File dirFile = new File(gn.getDir());
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		File[] fileList = dirFile.listFiles();
		ArrayList<File> datas = new ArrayList<File>();

		for (File tempFile : fileList) {
			if (tempFile.isFile()) {
				if (tempFile.getName().endsWith(".txt")) {
					datas.add(tempFile);
				}
			}
		}

		System.out.println("making from data...");
		for (File data : datas) {
			// System.out.println("gn.getWordOfNum(data): " +gn.getWordOfNum(data));

			String lineNum = String.valueOf(gn.getWordOfNum(data) / 96);

			String[] cmd1 = { "python", "markov.py", "parse", "temp", "2", data.getAbsolutePath() };
			String[] cmd2 = { "python", "markov.py", "gen", "temp", lineNum };

			gn.executeCmd1(cmd1, gn.getPathOfIm());
			gn.executeCmd2(cmd2, gn.getPathOfIm(), data);
		}

		System.out.println("saved in result");
	}

	private int getWordOfNum(File data) {
		int numOfWord = -1;
		try {
			numOfWord = 0;
			////////////////////////////////////////////////////////////////
			BufferedReader in = new BufferedReader(new FileReader(data));
			while (in.read() != -1) {
				numOfWord++;
			}
			in.close();
			////////////////////////////////////////////////////////////////
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		}
		return numOfWord;
	}

	private void executeCmd1(String[] cmd, String pathOfIm) throws Exception {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(pathOfIm));
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		String line;
		while ((line = stdOut.readLine()) != null) {
			System.out.println(line);
		}
		while ((line = stdError.readLine()) != null)
			System.err.println("error: " + line);
		process.waitFor();

	}

	private void executeCmd2(String[] cmd, String pathOfIm, File file) throws IOException, InterruptedException {
		ArrayList<String> lines = new ArrayList<String>();
		Generater gn = new Generater();
		boolean recur = true;
		int count = 0;
		int beforeWordCount = gn.getWordOfNum(file);
//		System.out.println("before word count: " + beforeWordCount);
		while (recur) {
			count++;
			System.out.println("try " + count + "ed.. ");
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(new File(pathOfIm));
			pb.redirectErrorStream(true);
			Process process = pb.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "ISO-8859-1"));
			String line;
			while ((line = reader.readLine()) != null) {
				String nline;

				nline = new String(line.getBytes("iso-8859-1"), "ksc5601");
				if (nline.contains("Traceback") || gn.getLinesWord(lines) > beforeWordCount) {
					break;
				}
				lines.add(nline);
			}
			process.waitFor();
			int AfternumOfWord = gn.getWordOfNum(gn.makeOut(lines, file));
//			System.out.println("current word: " + AfternumOfWord);
			if ((AfternumOfWord >= 3 * beforeWordCount / 4 || count > 50)) {
				recur = false;
			}
		}
	}

	private int getLinesWord(ArrayList<String> lines) {
		int sum = 0;
		for (String line : lines) {
			sum += line.length();
		}
		return sum;
	}

	private File makeOut(ArrayList<String> lines, File file) throws IOException {
		File curDir = new File("result");

		if (!curDir.exists()) {
			curDir.mkdir();
		}

		File newFile = new File(curDir.getAbsolutePath() + File.separator + file.getName());
		if (newFile.exists()) {
			if (newFile.delete()) {
				System.out.println(newFile.getName() + "을 삭제하였습니다.");
			} else {
				System.out.println(newFile.getName() + "을 삭제하는데 실패하였습니다.");
			}
		} else {
			System.out.println(newFile.getName() + "을 만들기 시작합니다.");
		}

		FileWriter fw = new FileWriter(newFile, false);

		for (String txt : lines) {
			fw.write(txt + ". ");
			fw.flush();
		}

		fw.close();
		System.out.println(" " + newFile.getAbsolutePath());

		return newFile;

	}

	private int getLineOfNum(File data) {

		int numOfLine = -1;
		try {
			numOfLine = 0;
			////////////////////////////////////////////////////////////////
			BufferedReader in = new BufferedReader(new FileReader(data));
			while (in.readLine() != null) {
				numOfLine++;
			}
			in.close();
			////////////////////////////////////////////////////////////////
		} catch (IOException e) {
			System.err.println(e); //
			System.exit(1);
		}
		return numOfLine;
	}
}
