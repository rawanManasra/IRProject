package com.jsonReading;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class ReadYahooDataBase {
	public List<QAData> DataBase;

	public ReadYahooDataBase() throws FileNotFoundException {
		final java.lang.reflect.Type QAType = new TypeToken<List<QAData>>() {
		}.getType();
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new FileReader("nfL6.json"));
		DataBase = gson.fromJson(reader, QAType); // contains the whole QA list
	}
}
