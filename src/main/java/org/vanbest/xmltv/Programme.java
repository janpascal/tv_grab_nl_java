package org.vanbest.xmltv;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
	class Length {
		TimeUnit unit; 
		int count;
	}
	class Icon {
		URL url;
		int width;
		int height;
	}
	class Episode {
	    String episode;
	    String system; // onscreen or xmltv_ns
	}
	class Video {
		boolean present;
		boolean colour;
		String aspect; // eg. 16:9, 4:3
		String quality; // eg. 'HDTV', '800x600'.
	}
	class Audio {
		boolean present;
		String stereo; // 'mono','stereo','dolby','dolby digital','bilingual' or 'surround'. 
	}
	public Date startTime; // required
	public Date endTime;
    public Date pdcStart;
    public Date vpsStart;
    public String showview;
    public String videoplus;
	public Channel channel; // required
    public String clumpidx;	
    
    public List<Title> titles; // at least one
    public List<Title> subtitles; 
    public List<Title> descriptions; 
    public Credits credits;
    public Date date; // copyright date, original date
    public List<Title> categories;
    Title language;
    Title origLanguage;
    Length length;
    public List<Icon> icons;
    public List<URL> urls;
    public List<Title> countries;
    public List<Episode> episodes;
    public Video video;
    public Audio audio;
    /*
    previously-shown?, premiere?, last-chance?, new?,
    subtitles*, rating*, star-rating*, review* 
    */
    
    public void addTitle(String title) {
    	addTitle(title, null);
    }
    public void addTitle(String title, String lang) {
    	if(titles==null) titles = new ArrayList<Title>();
    	Title t = new Title();
    	t.title = title;
    	t.lang = lang;
    	titles.add(t);
    }
	public void addCategory(String category) {
    	addCategory(category, null);
    }
    public void addCategory(String category, String lang) {
    	if(categories==null) categories = new ArrayList<Title>();
    	Title t = new Title();
    	t.title = category;
    	t.lang = lang;
    	categories.add(t);
    }

    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss Z");

		writer.writeStartElement("programme");
		if(startTime != null) writer.writeAttribute("start", df.format(startTime));
		if(channel != null) writer.writeAttribute("channel", ""+channel.id);
		if(titles != null) {
			for(Title title: titles) {
				writer.writeStartElement("title");
				if (title.lang != null) writer.writeAttribute("lang", title.lang);
				if (title.title != null) writer.writeCharacters(title.title);
				writer.writeEndElement();
			}
		}
		if(categories != null) {
			for(Title category: categories) {
				writer.writeStartElement("category");
				if (category.lang != null) writer.writeAttribute("lang", category.lang);
				if (category.title != null) writer.writeCharacters(category.title);
				writer.writeEndElement();
			}
		}
/*		for(Icon i: icons) {
			i.serialize(writer);
		}
		for(String url: urls) {
			writer.writeStartElement("url");
			writer.writeCharacters(url);
			writer.writeEndElement();
		}
*/		
		writer.writeEndElement();
		writer.writeCharacters(System.getProperty("line.separator"));
	}
    
}
