package org.vanbest.xmltv;

import java.io.Serializable;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Programme implements Serializable {
	class Title implements Serializable {
		String title;
		String lang;
	    public Title(String title, String lang) {
	    	this.title = title;
	    	this.lang = lang;
	    }
	}
	class Actor implements Serializable {
		String name;
		String role;
	}
	class Credits implements Serializable {
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
	class Length implements Serializable {
		TimeUnit unit; 
		int count;
	}
	class Icon implements Serializable {
		URL url;
		int width;
		int height;
	}
	class Episode implements Serializable {
	    String episode;
	    String system; // onscreen or xmltv_ns
	}
	class Video implements Serializable {
		boolean present;
		boolean colour;
		String aspect; // eg. 16:9, 4:3
		String quality; // eg. 'HDTV', '800x600'.
	}
	class Audio implements Serializable {
		boolean present;
		String stereo; // 'mono','stereo','dolby','dolby digital','bilingual' or 'surround'. 
	}
	class Subtitle implements Serializable {
		String type; // teletext | onscreen | deaf-signed
		Title language;
	}
	public Date startTime; // required
	public Date endTime;
    public Date pdcStart;
    public Date vpsStart;
    public String showview;
    public String videoplus;
	public String channel; // required xmltvid of the associated channel
    public String clumpidx;	
    
    public List<Title> titles; // at least one
    public List<Title> secondaryTitles; 
    public List<Title> descriptions; 
    public Credits credits;
    public Date date; // copyright date, original date
    public List<Title> categories;
    Title language;
    Title origLanguage;
    Length length;
    public List<Icon> icons;
    public List<String> urls;
    public List<Title> countries;
    public List<Episode> episodes;
    public Video video;
    public Audio audio;
    /*
    previously-shown?, premiere?, last-chance?, new?,
    */
    public List<Subtitle> subtitles; 
    /*rating*, star-rating*, review* 
    */

    public void addTitle(String title) {
    	addTitle(title, null);
    }
    public void addTitle(String title, String lang) {
    	if(titles==null) titles = new ArrayList<Title>();
    	titles.add(new Title(title,lang));
    }
	public void addSecondaryTitle(String title) {
		addSecondaryTitle(title,null);
	}
    public void addSecondaryTitle(String title, String lang) {
    	if(secondaryTitles==null) secondaryTitles = new ArrayList<Title>();
    	secondaryTitles.add(new Title(title,lang));
    }
    
	public void addCategory(String category) {
    	addCategory(category, null);
    }
    public void addCategory(String category, String lang) {
    	if(categories==null) categories = new ArrayList<Title>();
    	categories.add(new Title(category,lang));
    }
	public void addSubtitle(String type) {
    	addCategory(type, null);
    }
    public void addSubtitle(String type, String language, String language_lang) {
    	if(subtitles==null) subtitles = new ArrayList<Subtitle>();
    	Subtitle s = new Subtitle();
    	s.type = type;
    	if (language != null) {
    		s.language = new Title(language,language_lang);
    	}
    	subtitles.add(s);
    }
	public void addPresenter(String pres) {
		if (credits == null) credits = new Credits();
		if (credits.presenters==null) {
			credits.presenters=new ArrayList<String>();
		}
		credits.presenters.add(pres);
	}
	public void addUrl(String url) {
		if(urls==null) urls=new ArrayList<String>();
		urls.add(url);
	}

	private void writeTitle(Title title, String tag,
			XMLStreamWriter writer) throws XMLStreamException {
		if(title==null) return;
		writer.writeStartElement(tag);
		if (title.lang != null) writer.writeAttribute("lang", title.lang);
		if (title.title != null) writer.writeCharacters(title.title);
		writer.writeEndElement();
	}
	private void writeTitleList(List<Title> titles, String tag,
			XMLStreamWriter writer) throws XMLStreamException {
		if(titles==null) return;
		for(Title title: titles) {
			writeTitle(title,tag,writer);
		}
	}
	private void writeStringList(List<String> strings, String tag,
			XMLStreamWriter writer) throws XMLStreamException {
		if(strings==null) return;
		for(String s:strings) {
			writer.writeStartElement(tag);
			writer.writeCharacters(s);
			writer.writeEndElement();
		}
	}
    public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss Z");

		writer.writeStartElement("programme");
		if(startTime != null) writer.writeAttribute("start", df.format(startTime));
		if(endTime != null) writer.writeAttribute("stop", df.format(endTime));
		if(channel != null) writer.writeAttribute("channel", ""+channel);
		writeTitleList(titles,"title",writer);
		writeTitleList(secondaryTitles,"sub-title", writer); 
		if(credits != null) {
			writer.writeStartElement("credits");
			writeStringList(credits.presenters,"presenter",writer);
			writer.writeEndElement();
		}
		writeTitleList(categories, "category", writer);
		writeStringList(urls,"url",writer);
		if(subtitles != null) {
			for(Subtitle s: subtitles) {
				writer.writeStartElement("subtitles");
				if (s.type != null) writer.writeAttribute("type", s.type);
				if (s.language != null) writeTitle(s.language,"language",writer);
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
