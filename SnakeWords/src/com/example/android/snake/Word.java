package com.example.android.snake;

class Word{
	private String spelling;
	private String meaning;
	
	public Word(){
		spelling = "";
		meaning = "";
	}
	
	public Word(String spell, String mean){
		spelling = spell;
		meaning = mean;
	}
	
	public String getSpelling() {
		return spelling;
	}
	
	public void setSpelling(String spell){
		spelling = spell;
	}
	
	public String getMeaning() {
		return meaning;
	}
	
	public void setMeaning(String mean){
		meaning = mean;
	}
}
