package com.jsonReading;

import java.util.ArrayList;

public class QAData {
	String main_category;
	String question;
	ArrayList<String> nbestanswers;
	String answer;
	String id;

	public QAData(String mainCategory, String question, String[] NBestAnswers, String answer, String id) {
		main_category = mainCategory;
		this.question = question;
		this.id = id;
		this.answer = answer;
		nbestanswers = new ArrayList<String>();
		for (String ans : NBestAnswers) {
			nbestanswers.add(ans);
		}
	}

	public String getMain_category() {
		return main_category;
	}

	public void setMain_category(String main_category) {
		this.main_category = main_category;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public ArrayList<String> getNbestanswers() {
		return nbestanswers;
	}

	public void setNbestanswers(ArrayList<String> nbestanswers) {
		this.nbestanswers = nbestanswers;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean equals(String id) {
		return this.id.equals(id);
	}

	@Override
	public String toString() {
		String ans =  "id = " + id + "\n" + "Main Category: " + main_category + "\n" + "Question: " + question + "\n"+"Answer: "
				+ answer + "\n" + "N best answers: " + "\n";
		for(String s: nbestanswers) {
			ans += s + "\n";
		}
		return ans;
	}

}
