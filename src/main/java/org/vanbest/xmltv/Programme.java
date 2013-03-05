package org.vanbest.xmltv;

/* TODO
 * Only partially implemented. Some fields are not implemented at all; some miss easy functions for adding;
 * some aren't written to xmltv format
 */
import java.io.Serializable;
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

	/*
	 * See separate java source file for the Icon class class Icon implements
	 * Serializable { URL url; int width; int height; }
	 */
	class Episode implements Serializable {
		String episode;
		String system; // onscreen or xmltv_ns
	}

	class Video implements Serializable {
		boolean present = true; // FIXME should be default null
		boolean colour = true; // FIXME should be default null
		String aspect; // eg. 16:9, 4:3
		String quality; // eg. 'HDTV', '800x600'.
	}

	class Audio implements Serializable {
		boolean present;
		String stereo; // 'mono','stereo','dolby','dolby digital','bilingual' or
						// 'surround'.
	}

	class PreviouslyShown implements Serializable {
		String start;
		String channel;
	}

	class Subtitle implements Serializable {
		String type; // teletext | onscreen | deaf-signed
		Title language;
	}

	class Rating implements Serializable {
		String system;
		String value;
		List<Icon> icons;
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
	public PreviouslyShown previouslyShown;
	/*
	 * premiere?, last-chance?, new?,
	 */
	public List<Subtitle> subtitles;
	public List<Rating> ratings;

	/*
	 * star-rating*, review*
	 */

	public void addTitle(String title) {
		addTitle(title, null);
	}

	public void addTitle(String title, String lang) {
		if (titles == null)
			titles = new ArrayList<Title>();
		titles.add(new Title(title, lang));
	}

	public void addSecondaryTitle(String title) {
		addSecondaryTitle(title, null);
	}

	public void addSecondaryTitle(String title, String lang) {
		if (secondaryTitles == null)
			secondaryTitles = new ArrayList<Title>();
		secondaryTitles.add(new Title(title, lang));
	}

	public void addDescription(String title) {
		addDescription(title, null);
	}

	public void addDescription(String title, String lang) {
		if (descriptions == null)
			descriptions = new ArrayList<Title>();
		descriptions.add(new Title(title, lang));
	}

	public void addEpisode(String episode, String system) {
		if (episodes == null)
			episodes = new ArrayList<Episode>();
		Episode e = new Episode();
		e.episode = episode;
		e.system = system;
		episodes.add(e);
	}
		
	public void addCategory(String category) {
		addCategory(category, null);
	}

	public void addCategory(String category, String lang) {
		if (categories == null)
			categories = new ArrayList<Title>();
		categories.add(new Title(category, lang));
	}

	public void addSubtitle(String type) {
		addSubtitle(type, null, null);
	}

	public void addSubtitle(String type, String language, String language_lang) {
		if (subtitles == null)
			subtitles = new ArrayList<Subtitle>();
		Subtitle s = new Subtitle();
		s.type = type;
		if (language != null) {
			s.language = new Title(language, language_lang);
		}
		subtitles.add(s);
	}

	public void addPresenter(String pres) {
		if (credits == null)
			credits = new Credits();
		if (credits.presenters == null) {
			credits.presenters = new ArrayList<String>();
		}
		credits.presenters.add(pres);
	}

	public void addDirector(String director) {
		if (credits == null)
			credits = new Credits();
		if (credits.directors == null)
			credits.directors = new ArrayList<String>();
		credits.directors.add(director);
	}

	public void addActor(String name) {
		addActor(name, null);
	}

	public void addActor(String name, String role) {
		if (credits == null)
			credits = new Credits();
		if (credits.actors == null)
			credits.actors = new ArrayList<Actor>();
		Actor actor = new Actor();
		actor.name = name;
		actor.role = role;
		credits.actors.add(actor);
	}

	public void setVideoAspect(String aspect) {
		if (video == null)
			video = new Video();
		video.aspect = aspect;
	}

	public void setVideoQuality(String quality) {
		if (video == null)
			video = new Video();
		video.quality = quality;
	}

	public void setVideoColour(boolean colour) {
		if (video == null)
			video = new Video();
		video.colour = colour;
	}

	public void setAudioStereo(String stereo) {
		if (audio == null)
			audio = new Audio();
		audio.stereo = stereo;
	}

	public void addUrl(String url) {
		if (urls == null)
			urls = new ArrayList<String>();
		urls.add(url);
	}

	// Convenience method, set "rerun" flag without any additional information
	public void setPreviouslyShown() {
		setPreviouslyShown(null, null);
	}

	public void setPreviouslyShown(String startTime, String channel) {
		if (previouslyShown == null)
			previouslyShown = new PreviouslyShown();
		previouslyShown.start = startTime;
		previouslyShown.channel = channel;
	}

	public boolean hasCategory(String category) {
		if (categories == null)
			return false;
		for (Title t : categories) {
			if (t.title.toLowerCase().equals(category))
				return true;
		}
		return false;
	}

	public void addRating(String system, String value) {
		if (ratings == null)
			ratings = new ArrayList<Rating>();
		Rating r = new Rating();
		r.system = system;
		r.value = value;
		ratings.add(r);
	}

	private void writeTitle(Title title, String tag, XMLStreamWriter writer)
			throws XMLStreamException {
		if (title == null)
			return;
		writer.writeStartElement(tag);
		if (title.lang != null)
			writer.writeAttribute("lang", title.lang);
		if (title.title != null)
			writer.writeCharacters(title.title);
		writer.writeEndElement();
	}

	private void writeTitleList(List<Title> titles, String tag,
			XMLStreamWriter writer) throws XMLStreamException {
		if (titles == null)
			return;
		for (Title title : titles) {
			writeTitle(title, tag, writer);
		}
	}

	private void writeString(String s, String tag, XMLStreamWriter writer)
			throws XMLStreamException {
		if (s == null)
			return;
		writer.writeStartElement(tag);
		writer.writeCharacters(s);
		writer.writeEndElement();
	}

	private void writeStringList(List<String> strings, String tag,
			XMLStreamWriter writer) throws XMLStreamException {
		if (strings == null)
			return;
		for (String s : strings) {
			writeString(s, tag, writer);
		}
	}

	private void writeActorList(List<Actor> actors, XMLStreamWriter writer)
			throws XMLStreamException {
		if (actors == null)
			return;
		for (Actor actor : actors) {
			writer.writeStartElement("actor");
			if (actor.role != null)
				writer.writeAttribute("role", actor.role);
			if (actor.name != null)
				writer.writeCharacters(actor.name);
			writer.writeEndElement();
		}
	}

	private void writeEpisodeList(List<Episode> episodes, XMLStreamWriter writer)
			throws XMLStreamException {
		if (episodes == null)
			return;
		for (Episode e: episodes) {
			writer.writeStartElement("episode");
			if (e.system != null)
				writer.writeAttribute("system", e.system);
			if (e.episode != null)
				writer.writeCharacters(e.episode);
			writer.writeEndElement();
		}
	}

	private void writeIconList(List<Icon> icons, XMLStreamWriter writer)
			throws XMLStreamException {
		if (icons == null)
			return;
		for (Icon i : icons) {
			i.serialize(writer);
		}
	}

	public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss Z");

		writer.writeStartElement("programme");
		if (startTime != null)
			writer.writeAttribute("start", df.format(startTime));
		if (endTime != null)
			writer.writeAttribute("stop", df.format(endTime));
		if (channel != null)
			writer.writeAttribute("channel", "" + channel);
		writeTitleList(titles, "title", writer);
		writeTitleList(secondaryTitles, "sub-title", writer);
		writeTitleList(descriptions, "desc", writer);
		if (credits != null) {
			writer.writeStartElement("credits");
			writeStringList(credits.directors, "director", writer);
			writeActorList(credits.actors, writer);
			writeStringList(credits.presenters, "presenter", writer);
			writer.writeEndElement();
		}
		writeTitleList(categories, "category", writer);
		writeIconList(icons, writer);
		writeStringList(urls, "url", writer);
		writeEpisodeList(episodes,writer);
		if (video != null) {
			writer.writeStartElement("video");
			if (!video.present) {
				writer.writeStartElement("present");
				writer.writeCharacters("no");
				writer.writeEndElement();
			}
			if (!video.colour) {
				writer.writeStartElement("colour");
				writer.writeCharacters("no");
				writer.writeEndElement();
			}
			if (video.aspect != null) {
				writer.writeStartElement("aspect");
				writer.writeCharacters(video.aspect);
				writer.writeEndElement();
			}
			if (video.quality != null) {
				writer.writeStartElement("quality");
				writer.writeCharacters(video.quality);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		if (audio != null) {
			writer.writeStartElement("audio");
			if (audio.stereo != null) {
				writer.writeStartElement("stereo");
				writer.writeCharacters(audio.stereo);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		if (previouslyShown != null) {
			writer.writeStartElement("previously-shown");
			if (previouslyShown.start != null)
				writer.writeAttribute("start", previouslyShown.start);
			if (previouslyShown.channel != null)
				writer.writeAttribute("channel", previouslyShown.channel);
			writer.writeEndElement();
		}
		if (subtitles != null) {
			for (Subtitle s : subtitles) {
				writer.writeStartElement("subtitles");
				if (s.type != null)
					writer.writeAttribute("type", s.type);
				if (s.language != null)
					writeTitle(s.language, "language", writer);
				writer.writeEndElement();
			}
		}
		if (ratings != null) {
			for (Rating r : ratings) {
				writer.writeStartElement("rating");
				if (r.system != null)
					writer.writeAttribute("system", r.system);
				if (r.value != null)
					writeString(r.value, "value", writer);
				writeIconList(icons, writer);
				writer.writeEndElement();
			}

		}
		writer.writeEndElement();
		writer.writeCharacters(System.getProperty("line.separator"));
	}

	public String getFirstTitle() {
		if (titles == null || titles.isEmpty())
			return null;
		return titles.get(0).title;
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("[Programme ").append(getFirstTitle());
		s.append("@ ").append(startTime.toString());
		s.append("(").append(channel).append(")]");
		return s.toString();
	}
}
