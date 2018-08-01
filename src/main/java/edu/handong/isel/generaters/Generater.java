package edu.handong.isel.generaters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.openkoreantext.processor.KoreanPosJava;
import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer.KoreanToken;
import org.openkoreantext.processor.util.KoreanPos;

import scala.collection.Seq;

public class Generater {

	ArrayList<String> exceptionWord;

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
			Thread.sleep(2000);
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

	public void setStringFromFile(File file, String txt) throws IOException {

		BufferedWriter fw = new BufferedWriter(new FileWriter(file, false));

		// 파일안에 문자열 쓰기
		fw.write(txt);
		fw.flush();

		// 객체 닫기
		fw.close();
	}

	private void run(String[] args) throws Exception {
		Generater gn = new Generater();

		System.out.println("start Program from... ");
		System.out.println(System.getProperty("user.dir"));

		gn.setDir("data");
		// gn.setDir("temp");
		gn.setPathOfIm("markov-text");

		File dirFile = new File(gn.getDir());
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

		// Scanner in = new Scanner(System.in);
		// int j = 0;
		// String line = "";
		// System.out.print("Push Text: ");
		// while((line = in.nextLine()) != null) {
		//
		// }

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

			/* Editting.. */
			
			
			
			// (1)
			List<KoreanTokenJava> oldTokensList = this.makeTokensFromOneLine2(data);
			//ArrayList<String> keywordList = this.extractToken(oldTokensList);
			BufferedWriter fw = new BufferedWriter(new FileWriter(new File("test.txt"), false));
			
			for (KoreanTokenJava temp : oldTokensList) {
				if (temp.getPos() == KoreanPosJava.Noun)
					fw.write(temp.getText() + "\n");
				fw.flush();
			}
			fw.close();
			
			
			

			// (2)
			List<KoreanTokenJava> newTokensList = this.makeTokensFromOneLine(newFile);

			// (3)
			List<KoreanTokenJava> nonContainedWord = this.findNonContainNoun(oldTokensList, newTokensList);// 들어가지 않은 단어

			// (4)
			List<KoreanTokenJava> resultTokenList = this.insertTokenToDuplicatedToken(newTokensList, nonContainedWord);

			// for(KoreanTokenJava word : resultTokenList) {
			// System.out.println(word.getText()); }

			// (5)
			StringBuffer sb = new StringBuffer();
			int i;

			sb.append(resultTokenList.get(0).getText());
			for (i = 1; i < resultTokenList.size(); i++) {
				KoreanTokenJava first = resultTokenList.get(i - 1);
				KoreanTokenJava second = resultTokenList.get(i);

				if ((first.getPos() == KoreanPosJava.Noun) && (second.getPos() == KoreanPosJava.Noun))
					sb.append(" ");
				else if (first.getPos() == KoreanPosJava.Josa) {
					sb.append(" ");
				} else if (first.getPos() == KoreanPosJava.Adjective) {
					sb.append(" ");
				} else if (first.getPos() == KoreanPosJava.Verb) {
					sb.append(" ");
				} else if (first.getPos() == KoreanPosJava.Adverb) {
					sb.append(" ");
				}

				sb.append(second.getText());

			}
			if (newFile.exists()) {
				if (newFile.delete()) {
					System.out.println("success to delete File!");
				} else {
					System.out.println("fail to delete File!");
				}
			} else {
				System.out.println("file not exist");
			}

			// File nnFile = new File(
			// newFile.getParentFile().getAbsolutePath() + File.separator + "2" +
			// newFile.getName());

			// this.makeOutFile(nnFile, sb.toString());
			this.makeOutFile(newFile, sb.toString());

			/* 			 */
		}

		System.out.println("saved in result");

	}

	private ArrayList<String> getDiviedString(String str) {
		StringTokenizer tokens = new StringTokenizer(str);
		
		int i;
		ArrayList<String> list = new ArrayList<String>();
		while(tokens.hasMoreTokens()) {
			//System.out.println(tokens.nextToken());
			list.add(tokens.nextToken());
		}
		return list;
	}

	private ArrayList<String> extractToken(List<KoreanTokenJava> newTokensList) {
		ArrayList<String> keywords = new ArrayList<String>();
		for(KoreanTokenJava token : newTokensList) {
			if(token.getPos() == KoreanPosJava.Noun) {
				keywords.add(token.getText());
			}
		}
		return keywords;
	}

	private File makeOutFile(File newFile, String string) throws IOException {
		if (newFile.exists()) {
			newFile.delete();
		} else {
			File parent = null;
			if (!(parent = newFile.getParentFile()).exists()) {
				parent.mkdirs();
			}
		}
		if (newFile.exists()) {
			if (newFile.delete()) {
				// System.out.println(newFile.getName() + "을 삭제하였습니다.");
			} else {
				// System.out.println(newFile.getName() + "을 삭제하는데 실패하였습니다.");
			}
		} else {
			// System.out.println(newFile.getName() + "을 만들기 시작합니다.");
		}

		FileOutputStream fileOutputStream = new FileOutputStream(newFile);
		OutputStreamWriter OutputStreamWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
		BufferedWriter bf = new BufferedWriter(OutputStreamWriter);
		//
		// FileWriter bf = new FileWriter(newFile, false);
		bf.write(string);
		bf.flush();
		//
		bf.close();
		System.out.println(" " + newFile.getAbsolutePath());

		return newFile;

	}

	private List<KoreanTokenJava> insertTokenToDuplicatedToken(List<KoreanTokenJava> oldTokensList,
			List<KoreanTokenJava> nonContainedWord) {
		List<KoreanTokenJava> resultTokenList = new ArrayList<KoreanTokenJava>();
		ArrayList<KoreanTokenJava> duplicatedWord = new ArrayList<KoreanTokenJava>();

		// for (KoreanTokenJava word : nonContainedWord) {
		// System.out.println(word.getText());
		// }
		
		new KoreanTokenJava(dir, null, 0, 0, false, dir);
		
		int i = 0;
		// System.out.println("size: "+nonContainedWord.size());
		for (KoreanTokenJava word : oldTokensList) {
			if (i < nonContainedWord.size() && word.getPos() == KoreanPosJava.Noun) {
				if (duplicatedWord.contains(word)) {
					resultTokenList.add(word);
					continue;
				}
				duplicatedWord.add(word);

				KoreanTokenJava newWord = nonContainedWord.get(i);
				i++;
				// System.out.print("new Word: " + newWord.getText());
				// System.out.println(" -> old Word: " + word.getText());
				resultTokenList.add(newWord);
			} else {
				resultTokenList.add(word);
			}
		}
		// System.out.println("i: " + i);

		return resultTokenList;
	}

	private List<KoreanTokenJava> findNonContainNoun(List<KoreanTokenJava> oldTokensList,
			List<KoreanTokenJava> newTokensList) {
		List<KoreanTokenJava> foundNoun = new ArrayList<KoreanTokenJava>();
		// ArrayList<String> duplicatiedNoun = new ArrayList<String>();
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
				// System.out.println("Noun: " + word.getText());
			} else {
				// System.out.println("un-Noun: " + word.getText() + ", type: " +
				// word.getPos());
			}
		}

		for (KoreanTokenJava word : oldNounList) {
			if (!duplicatedNoun.contains(word.getText()) && !newNounList.contains(word)) {
				duplicatedNoun.add(word.getText());
				foundNoun.add(word);
				// System.out.println("this: \"" + word.getText()+"\"");
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

	private List<KoreanTokenJava> makeTokensFromOneLine2(File data) throws IOException {
		Seq<KoreanToken> oldTokens = null;
		String extractedLine = this.extractLineFromFile2(data);
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
				// System.out.println(newFile.getName() + "을 삭제하였습니다.");
			} else {
				// System.out.println(newFile.getName() + "을 삭제하는데 실패하였습니다.");
			}
		} else {
			// System.out.println(newFile.getName() + "을 만들기 시작합니다.");
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

	private String extractLineFromFile2(File data) throws IOException {
		String extractedLine = "";

		FileInputStream fileInputStream = new FileInputStream(data);
		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
		// InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,
		// "euc-kr");
		BufferedReader reader = new BufferedReader(inputStreamReader);

		String line = "";
		while ((line = reader.readLine()) != null) {
			extractedLine += (line + " ");
		}

		return extractedLine;
	}

	
	/* This need for Debug */
	private String extractLineFromFile(File data) throws IOException {
		String extractedLine = "";

		FileInputStream fileInputStream = new FileInputStream(data);
		 InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,
		 "UTF-8");
//		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "euc-kr");
		BufferedReader reader = new BufferedReader(inputStreamReader);
		// BufferedReader reader = new BufferedReader(new InputStreamReader(new
		// FileInputStream(file),"euc-kr"));

		String line = "";
		while ((line = reader.readLine()) != null) {
			extractedLine += (line + " ");
		}

		return extractedLine;
	}

	private Seq<KoreanTokenizer.KoreanToken> tokenization(String line) {
		CharSequence normalized = OpenKoreanTextProcessorJava.normalize(line);
		
		System.out.println(normalized.toString());
		
		String str = "고등영어과외 고등수학과외 중등영어과외 중등수학과외 기초과외 초등영어과외 초등수학과외 초5영어과외 초3 초4 초5 초6 중1 중2 중3 고1 고2 고3 내신 선행 족집게 이과 문과 수능 시험 일대일 과외쌤 소개 추천 찾기 중간고사 기말고사 아파트 마을 1등급 2등급 3등급 4등급 5등급 6등급 수능시험 시험범위 이공계 공부잘하는법 성적올리기 성적잘올리는법 잘하는법 책임감있는 과외선생님 선생님 알려주세요 소개해주세요 추천해주세요 소개 추천 찾기 초등학교 중학교 고등학교 학생은 공부 수포자 영포자 내신공부 6월모의고사 6월모평 9월모의고사 9월모평 내신공부 내신준비 수능대비 기말대비 중간대비 도와주세요 신뢰할 수 있는 언어영역 수리영역 외국어영역 사탐 과탐 사회 과학 생명과학 화학 물리 지구과학 세계지리 한국지리 윤리 근현대사 제2외국어 생활과윤리 사회문화 윤리와사상 동아시아사 법과정치 경제 역량 꾸준하게 잘가르치는 책임감있는 유명한 학생 수험생 재수생 예습 복습 선행학습 전문 일대일 소개 추천 찾기 어디서 받나요? 통합과학 통합사회 한국사 3월 6월 9월 11월 학평 모평 모의고사 수능 시험 평가 내신 입시 대입 7등급 8등급 구하기 연결 중개 대학생 알바 성적올리는법 전교1등 공부습관 몰입 집중 집중력 성적상승 성적하락 공부 수능대비 언어영역 외국어영역 수리영역 도와주세요 해주세요 잘하는법 공부방법 실업계 대학 인문계 이공계 수능범위 영수 교육청 수능대박 시험대박 어떻게해야하나요 공부하는습관 상위권 하위권 일대일 눈높이수업 기출문제 기출 내신문제 교과서 국어 사탐 과탐";
		ArrayList<String> keywordList = this.getDiviedString(str);
		
		return OpenKoreanTextProcessorJava.tokenize(normalized);
		// Seq<KoreanTokenizer.KoreanToken> tokens =
		// OpenKoreanTextProcessorJava.tokenize(normalized);
		// return OpenKoreanTextProcessorJava.tokensToJavaStringList(tokens);

	}
}
