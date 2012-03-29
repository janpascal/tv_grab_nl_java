package org.vanbest.xmltv;

/*
  Copyright (c) 2012 Jan-Pascal van Best <janpascal@vanbest.org>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  The full license text can be found in the LICENSE file.
*/

import java.util.Date;
import org.apache.commons.lang.StringEscapeUtils;

public class TvGidsProgramme {
	  public String getDb_id() {
		return db_id;
	}

	public void setDb_id(String db_id) {
		this.db_id = db_id;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getSoort() {
		return soort;
	}

	public void setSoort(String soort) {
		this.soort = soort;
	}

	public String getKijkwijzer() {
		return kijkwijzer;
	}

	public void setKijkwijzer(String kijkwijzer) {
		this.kijkwijzer = kijkwijzer;
	}

	public String getArtikel_id() {
		return artikel_id;
	}

	public void setArtikel_id(String artikel_id) {
		this.artikel_id = artikel_id;
	}

	public Date getDatum_start() {
		return datum_start;
	}

	public void setDatum_start(Date datum_start) {
		this.datum_start = datum_start;
	}

	public Date getDatum_end() {
		return datum_end;
	}

	public void setDatum_end(Date datum_end) {
		this.datum_end = datum_end;
	}

	public boolean isIs_highlight() {
		return is_highlight;
	}

	public void setIs_highlight(boolean is_highlight) {
		this.is_highlight = is_highlight;
	}

	public String getHighlight_afbeelding() {
		return highlight_afbeelding;
	}

	public void setHighlight_afbeelding(String highlight_afbeelding) {
		this.highlight_afbeelding = highlight_afbeelding;
	}

	public String getHighlight_content() {
		return highlight_content;
	}

	public void setHighlight_content(String highlight_content) {
		this.highlight_content = highlight_content;
	}

	  String db_id;
	  String titel;
	  String genre;
	  String soort;
	  String kijkwijzer;
	  String artikel_id;
	  Date datum_start;
	  Date datum_end;
	  boolean is_highlight;
	  String highlight_afbeelding;
	  String highlight_content;
	  TvGidsProgrammeDetails details = null;
	  Channel channel = null;

	  public void fixup(Config config) {
		 titel = titel==null?"":StringEscapeUtils.unescapeHtml(titel);
		 genre = genre==null?"":StringEscapeUtils.unescapeHtml(genre);
		 soort = soort==null?"":StringEscapeUtils.unescapeHtml(soort);
		 highlight_content = highlight_content==null?"":StringEscapeUtils.unescapeHtml(highlight_content);
		 genre = config.translateCategory(genre);
	  }

	  public String toString() {
		  StringBuffer s = new StringBuffer();
		  s.append("id: " + db_id + ";");
		  s.append("titel: " + titel + ";");
		  s.append("genre: " + genre + ";");
		  s.append("soort: " + soort + ";");
		  s.append("kijkwijzer: " + kijkwijzer+ ";");
		  s.append("artikel_id: " + artikel_id + ";");
		  s.append("datum_start: " + datum_start + ";");
		  s.append("datum_end: " + datum_end + ";");
		  if (details != null) s.append("details:" + details.toString() );
		  s.append("\n");
		  
		  return s.toString();
	  }
}
