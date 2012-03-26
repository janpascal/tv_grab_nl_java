package org.vanbest.xmltv;

import java.util.Date;
import java.util.List;

public class Programme {
	class Title {
		String title;
		String lang;
	}
	class Actor {
		String name;
		String role;
	}
	class Credits {
		List<String> directors;
		List<Actor> actors;
		List<String> writers;
		List<String> adaptors;
		List<String> producers;
		List<String> composers;
		List<String> editors;
		List<String> presenters;
		List<String> commentators;
		List<String> guests;
	}
	public Channel channel; // required
	public Date startTime; // required
	public Date endTime;
    public Date pdcStart;
    public Date vpsStart;
    public String showview;
    public String videoplus;
    public String clumpidx;	
    
    public List<Title> titles; // at least one
    public List<Title> subtitles; 
    public List<Title> descriptions; 
    public Credits credits;
    public Date date; // copyright date, original date
    public List<Title> categories;
    Title language;
    Title origLanguage;
    
    
}
