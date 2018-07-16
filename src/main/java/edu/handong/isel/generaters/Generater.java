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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void run(String[] args) throws IOException, InterruptedException {
		Generater gn = new Generater();
		
		/**/

		String word = "인코딩 문제인가? 이클립스 문제인가? WAS문제 인가 그것이 알고 싶다....";
		System.out.println("utf-8 -> euc-kr        : " + new String(word.getBytes("utf-8"), "euc-kr"));
		System.out.println("utf-8 -> ksc5601       : " + new String(word.getBytes("utf-8"), "ksc5601"));
		System.out.println("utf-8 -> x-windows-949 : " + new String(word.getBytes("utf-8"), "x-windows-949"));
		System.out.println("utf-8 -> iso-8859-1    : " + new String(word.getBytes("utf-8"), "iso-8859-1"));
		System.out.println("iso-8859-1 -> euc-kr        : " + new String(word.getBytes("iso-8859-1"), "euc-kr"));
		System.out.println("iso-8859-1 -> ksc5601       : " + new String(word.getBytes("iso-8859-1"), "ksc5601"));
		System.out.println("iso-8859-1 -> x-windows-949 : " + new String(word.getBytes("iso-8859-1"), "x-windows-949"));
		System.out.println("iso-8859-1 -> utf-8         : " + new String(word.getBytes("iso-8859-1"), "utf-8"));
		System.out.println("euc-kr -> utf-8         : " + new String(word.getBytes("euc-kr"), "utf-8"));
		System.out.println("euc-kr -> ksc5601       : " + new String(word.getBytes("euc-kr"), "ksc5601"));
		System.out.println("euc-kr -> x-windows-949 : " + new String(word.getBytes("euc-kr"), "x-windows-949"));
		System.out.println("euc-kr -> iso-8859-1    : " + new String(word.getBytes("euc-kr"), "iso-8859-1"));
		System.out.println("ksc5601 -> euc-kr        : " + new String(word.getBytes("ksc5601"), "euc-kr"));
		System.out.println("ksc5601 -> utf-8         : " + new String(word.getBytes("ksc5601"), "utf-8"));
		System.out.println("ksc5601 -> x-windows-949 : " + new String(word.getBytes("ksc5601"), "x-windows-949"));
		System.out.println("ksc5601 -> iso-8859-1    : " + new String(word.getBytes("ksc5601"), "iso-8859-1"));
		System.out.println("x-windows-949 -> euc-kr     : " + new String(word.getBytes("x-windows-949"), "euc-kr"));
		System.out.println("x-windows-949 -> utf-8      : " + new String(word.getBytes("x-windows-949"), "utf-8"));
		System.out.println("x-windows-949 -> ksc5601    : " + new String(word.getBytes("x-windows-949"), "ksc5601"));
		System.out.println("x-windows-949 -> iso-8859-1 : " + new String(word.getBytes("x-windows-949"), "iso-8859-1"));


		//출처: http://devlop.tistory.com/6 [삽질 LEE의 아이티 이야기]
		
		/**/
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
//		ArrayList<Integer> Ilist = new ArrayList<Integer>();
//		System.out.println("80번 반복");
//		int i;
//		for (i = 0; i < 80; i++) {
			for (File data : datas) {

				String lineNum = String.valueOf(gn.getWordOfNum(data)/96);
				// String lineNum = String.valueOf(gn.getLineOfNum(data));
//				String wordNum = String.valueOf(gn.getWordOfNum(data));
				// System.out.println("라인 수는:" + lineNum);
				// System.out.println("글자 수는:" + wordNum);

				String[] cmd1 = { "python", "markov2.py", "parse", "temp", "2", data.getAbsolutePath() };
				String[] cmd2 = { "python", "markov2.py", "gen", "temp", lineNum };

				gn.executeCmd1(cmd1, gn.getPathOfIm());
				gn.executeCmd2(cmd2, gn.getPathOfIm(), data);

//				File nnf = new File("result/" + data.getName());

//				wordNum = String.valueOf(gn.getWordOfNum(nnf));
//				lineNum = String.valueOf(gn.getLineOfNum(nnf));

				// System.out.println(nnf+"의 라인 수는:" + lineNum);
				// System.out.println(nnf+"의 글자 수는:" + wordNum);

//				 System.out.println("(원래 있던 글자수)/(라인 수): " +
//				 (gn.getWordOfNum(data))/Integer.parseInt(lineNum));
				
//				System.out.println("원래 글자수 : " + gn.getWordOfNum(data) + ", 결과 글자수: " + wordNum);
				
//				Ilist.add(gn.getWordOfNum(data) / Integer.parseInt(lineNum));

			}
//		}
//		int sum = 0;
//		for(Integer member : Ilist) {
//			sum+=member;
//		}
//		int aver = sum / Ilist.size();
//		System.out.println("80번 반복했을 때 평균: "+aver);
		
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
			System.err.println(e); // 에러가 있다면 메시지 출력
			System.exit(1);
		}
		return numOfWord;
	}

	private void executeCmd1(String[] cmd, String pathOfIm) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(pathOfIm));
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null)
			System.out.println(line);
		process.waitFor();

	}

	private void executeCmd2(String[] cmd, String pathOfIm, File file) throws IOException, InterruptedException {
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Generater gn = new Generater();
		pb.directory(new File(pathOfIm));
		pb.redirectErrorStream(true);
		Process process = pb.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),"EUC-KR"));
		String line;
		ArrayList<String> lines = new ArrayList<String>();
		while ((line = reader.readLine()) != null)
		{
			System.out.println(line);//.getBytes("UTF-8"));
			lines.add(line);
		}
		process.waitFor();
		gn.makeOut(lines, file);
	}

	private void makeOut(ArrayList<String> lines, File file) throws IOException {
		File curDir = new File("result");

		if (!curDir.exists()) {
			curDir.mkdir();
		}

		File newFile = new File(curDir.getAbsolutePath() + "/" + file.getName());

		FileWriter fw = new FileWriter(newFile, false);

		for (String txt : lines) {
			fw.write(txt + "\n");
			fw.flush();
		}

		fw.close();
		 System.out.println(" " +newFile.getAbsolutePath());

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
