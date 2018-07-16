package edu.handong.isel.generaters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

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

	private void run(String[] args) throws IOException, InterruptedException {
		Generater gn = new Generater();

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

		System.out.println(System.getProperty("user.dir"));

		// dirFile = new File(".");
		// System.out.println(dirFile.getAbsolutePath());

		// File[] fileList2 = new File(".").listFiles();
		// for(File tempFile : fileList2) {
		// System.out.println(tempFile);
		// }

		System.out.println("making from data...");
		for (File data : datas) {

			String lineNum = String.valueOf(gn.getWordOfNum(data) / 96);

			String[] cmd1 = { "python", "markov.py", "parse", "temp", "2", data.getAbsolutePath() };
			String[] cmd2 = { "python", "markov.py", "gen", "temp", lineNum };

			// String[] cmd1 = { "python", "markov2.py", "parse", "temp", "2",
			// data.getAbsolutePath() };
			// String[] cmd2 = { "python", "markov2.py", "gen", "temp", lineNum };

			gn.executeCmd1(cmd1, gn.getPathOfIm());
			gn.executeCmd2(cmd2, gn.getPathOfIm(), data);
		}

		System.out.println("saved in result");

	}

	private void editData(File data, ArrayList<String> newLines) throws IOException {
		FileWriter fw = new FileWriter(data, false);

		for (String line : newLines) {
			fw.write(line + "\n");
		}
		fw.flush();

	}

	private ArrayList<String> extractOfData(File data) throws IOException {
		ArrayList<String> newLines = new ArrayList<String>();
		ArrayList<String> oldLines = new ArrayList<String>();

		FileReader fr = new FileReader(data);
		BufferedReader br = new BufferedReader(fr);

		String line;
		while ((line = br.readLine()) != null) {
			// System.out.println(line);
			oldLines.add(line);
		}

		for (String temp : oldLines) {
			if (temp.length() > 100) {
				newLines.addAll(this.divideLines(temp));
			}
		}

		return newLines;
	}

	private ArrayList<String> divideLines(String line) {

		ArrayList<String> newLines = new ArrayList<String>();
		int max = line.length();
		int i, lastest = 0;
		for (i = 0; i < max; i++) {
			if (line.charAt(i) == '.' || lastest - i > 100) {
				newLines.add(line.substring(lastest + 1, i) + ".");
				lastest = i + 1;
			}
		}

		return newLines;
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
			System.err.println(e); // 에러가 있다면 메시지 출력
			System.exit(1);
		}
		System.out.println("word: " + numOfWord);
		return numOfWord;
	}

	private void executeCmd1(String[] cmd, String pathOfIm) throws IOException, InterruptedException {
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
		while (recur) {
			while (true) {
				boolean recur2 = false;
				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.directory(new File(pathOfIm));
				pb.redirectErrorStream(true);
				Process process = pb.start();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream(), "ISO-8859-1"));
				String line;
				while ((line = reader.readLine()) != null) {
					String nline;

					nline = new String(line.getBytes("iso-8859-1"), "ksc5601");
					if (nline.contains("Traceback")) {
						recur2 = true;
						break;
					}
					lines.add(nline);
				}
				process.waitFor();
				if (recur2)
					break;
			}
			if(count>50||gn.makeOut(lines, file)>3*gn.getWordOfNum(file)/4)
				recur = false;
		}
	}

	private int makeOut(ArrayList<String> lines, File file) throws IOException {
		File curDir = new File("result");

		if (!curDir.exists()) {
			curDir.mkdir();
		}

		File newFile = new File(curDir.getAbsolutePath() + "/" + file.getName());

		FileWriter fw = new FileWriter(newFile, false);

		for (String txt : lines) {
			fw.write(txt +" ");
			fw.flush();
		}

		fw.close();
		System.out.println(" " + newFile.getAbsolutePath());
		
		return this.getWordOfNum(newFile);

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
			System.err.println(e); // 에러가 있다면 메시지 출력
			System.exit(1);
		}
		return numOfLine;
	}
}
