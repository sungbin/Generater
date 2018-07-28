package edu.handong.isel.generaters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openkoreantext.processor.KoreanPosJava;
import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;

import scala.collection.Seq;

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
			File newFile = gn.executeCmd2(cmd2, gn.getPathOfIm(), data);
			
			//int temp2 = this.getWordOfNum(newFile);
			
			/* Editting.. */
			// (1)
			List<KoreanTokenJava> oldTokensList = this.makeTokensFromOneLine(data);
			// (2)
			List<KoreanTokenJava> newTokensList = this.makeTokensFromOneLine(newFile);

			// (3)
			List<KoreanTokenJava> nonContainedWord = this.findNonContainNoun(oldTokensList, newTokensList);// 들어가지 않은 단어
																											// 찾는 부분.
			// (4)
			List<KoreanTokenJava> resultTokenList = this.insertTokenToDuplicatedToken(oldTokensList, nonContainedWord);

			// (5)
			StringBuffer sb = new StringBuffer();
			for (KoreanTokenJava temp : resultTokenList) {
				sb.append(temp.getText().trim());
				//if (temp.getPos() == KoreanPosJava.Verb)
					//sb.append(". ");
			}
			int temp2 = sb.toString().length();
			this.makeOutFile(newFile, sb.toString());
			
//			System.out.print("mid n: " + temp2);
//			System.out.print(", old n: " + this.getWordOfNum(data));
//			System.out.println(", new n: "+this.getWordOfNum(newFile));
			
			/* 			 */
		}

		System.out.println("saved in result");
	}

	private File makeOutFile(File newFile, String string) throws IOException {
		if (newFile.exists()) {
			newFile.delete();
		}
		File parent = null;
		if (!(parent = newFile.getParentFile()).exists()) {
			parent.mkdirs();
		}

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

		fw.write(string);
		fw.flush();

		fw.close();
		System.out.println(" " + newFile.getAbsolutePath());

		return newFile;

	}

	private List<KoreanTokenJava> insertTokenToDuplicatedToken(List<KoreanTokenJava> oldTokensList,
			List<KoreanTokenJava> nonContainedWord) {
		List<KoreanTokenJava> resultTokenList = new ArrayList<KoreanTokenJava>();
		ArrayList<KoreanTokenJava> duplicatedWord = new ArrayList<KoreanTokenJava>();

		int i = 0;
		for (KoreanTokenJava word : oldTokensList) {
			if (i < nonContainedWord.size() && word.getPos() == KoreanPosJava.Noun) {
				if (duplicatedWord.contains(word)) {
					resultTokenList.add(word);
					continue;
				}
				duplicatedWord.add(word);
				
				KoreanTokenJava newWord = nonContainedWord.get(i);
				i++;
//				System.out.print("new Word: " + newWord.getText());
//				System.out.println(" -> old Word: " + word.getText());
				
				resultTokenList.add(newWord);
			} else {
				resultTokenList.add(word);
			}
		}

		return resultTokenList;
	}

	private List<KoreanTokenJava> findNonContainNoun(List<KoreanTokenJava> oldTokensList,
			List<KoreanTokenJava> newTokensList) {
		List<KoreanTokenJava> foundNoun = new ArrayList<KoreanTokenJava>();
		//ArrayList<String> duplicatiedNoun = new ArrayList<String>();
		HashSet<String> duplicatedNoun = new HashSet<String>();
		
		
		ArrayList<KoreanTokenJava> newNounList = new ArrayList<KoreanTokenJava>();
		ArrayList<KoreanTokenJava> oldNounList = new ArrayList<KoreanTokenJava>();

		for (KoreanTokenJava word : newTokensList) {
			if (word.getPos() == KoreanPosJava.Noun) {
				newNounList.add(word);
			}
		}
		for (KoreanTokenJava word : oldTokensList) {
			if (word.getPos() == KoreanPosJava.Noun) {
				oldNounList.add(word);
			}
		}

		for (KoreanTokenJava word : oldNounList) {
			if (!duplicatedNoun.contains(word.getText()) && !newNounList.contains(word)) {
				duplicatedNoun.add(word.getText());
				foundNoun.add(word);
//				System.out.println("this: \"" + word.getText()+"\"");
			}
		}

		return foundNoun;
	}

	private List<KoreanTokenJava> makeTokensFromOneLine(File data) throws IOException {
		Seq<KoreanToken> oldTokens = null;
		String extractedLine = this.extractLineFromFile(data);
		oldTokens = this.tokenization(extractedLine);
		List<KoreanTokenJava> tokenList = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(oldTokens);

		return tokenList;
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

	private File executeCmd2(String[] cmd, String pathOfIm, File file) throws IOException, InterruptedException {
		ArrayList<String> lines = new ArrayList<String>();
		Generater gn = new Generater();
		boolean recur = true;
		int count = 0;
		int beforeWordCount = gn.getWordOfNum(file);
		// System.out.println("before word count: " + beforeWordCount);
		File newFile = null;
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
			newFile = gn.makeOut(lines, file);
			int AfternumOfWord = gn.getWordOfNum(newFile);
			// System.out.println("current word: " + AfternumOfWord);
			if ((AfternumOfWord >= 3 * beforeWordCount / 4 || count > 50)) {
				recur = false;
			}
		}
		return newFile;
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
			fw.write(txt);
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

	private String extractLineFromFile(File data) throws IOException {
		String extractedLine = "";

		FileInputStream fileInputStream = new FileInputStream(data);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
		BufferedReader reader = new BufferedReader(inputStreamReader);

		String line = "";
		while ((line = reader.readLine()) != null) {
			extractedLine += (line + " ");
		}

		return extractedLine;
	}

	private Seq<KoreanTokenizer.KoreanToken> tokenization(String line) {
		CharSequence normalized = OpenKoreanTextProcessorJava.normalize(line);
		return OpenKoreanTextProcessorJava.tokenize(normalized);
		// Seq<KoreanTokenizer.KoreanToken> tokens =
		// OpenKoreanTextProcessorJava.tokenize(normalized);
		// return OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens);

	}
}
