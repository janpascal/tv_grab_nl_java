package org.vanbest.xmltv;

import java.util.Date;

public class Programme {
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

	public boolean isIs_hightlight() {
		return is_hightlight;
	}

	public void setIs_hightlight(boolean is_hightlight) {
		this.is_hightlight = is_hightlight;
	}

	public String getHighlight_afbeelding() {
		return highlight_afbeelding;
	}

	public void setHighlight_afbeelding(String highlight_afbeelding) {
		this.highlight_afbeelding = highlight_afbeelding;
	}

	String db_id;
	  String titel;
	  String genre;
	  String soort;
	  String kijkwijzer;
	  String artikel_id;
	  Date datum_start;
	  Date datum_end;
	  boolean is_hightlight;
	  String highlight_afbeelding;
	  ProgrammeDetails details = null;
	  
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
