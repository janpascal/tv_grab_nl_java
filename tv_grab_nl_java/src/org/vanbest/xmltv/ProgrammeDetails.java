package org.vanbest.xmltv;

public class ProgrammeDetails {
	String db_id;
	String titel;
	String datum;
	String btijd;
	String etijd;
	String synop;
	String kijkwijzer;
	String genre;
	String presentatie;
	String acteursnamen_rolverdeling;
	String regisseur;
	String zender_id;
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
	public String getDatum() {
		return datum;
	}
	public void setDatum(String datum) {
		this.datum = datum;
	}
	public String getBtijd() {
		return btijd;
	}
	public void setBtijd(String btijd) {
		this.btijd = btijd;
	}
	public String getEtijd() {
		return etijd;
	}
	public void setEtijd(String etijd) {
		this.etijd = etijd;
	}
	public String getSynop() {
		return synop;
	}
	public void setSynop(String synop) {
		this.synop = synop;
	}
	public String getKijkwijzer() {
		return kijkwijzer;
	}
	public void setKijkwijzer(String kijkwijzer) {
		this.kijkwijzer = kijkwijzer;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getPresentatie() {
		return presentatie;
	}
	public void setPresentatie(String presentatie) {
		this.presentatie = presentatie;
	}
	public String getActeursnamen_rolverdeling() {
		return acteursnamen_rolverdeling;
	}
	public void setActeursnamen_rolverdeling(String acteursnamen_rolverdeling) {
		this.acteursnamen_rolverdeling = acteursnamen_rolverdeling;
	}
	public String getRegisseur() {
		return regisseur;
	}
	public void setRegisseur(String regisseur) {
		this.regisseur = regisseur;
	}
	public String getZender_id() {
		return zender_id;
	}
	public void setZender_id(String zender_id) {
		this.zender_id = zender_id;
	}
	@Override
	public String toString() {
		return "ProgrammeDetails [db_id=" + db_id + ", titel=" + titel
				+ ", datum=" + datum + ", btijd=" + btijd + ", etijd=" + etijd
				+ ", synop=" + synop + ", kijkwijzer=" + kijkwijzer
				+ ", genre=" + genre + ", presentatie=" + presentatie
				+ ", acteursnamen_rolverdeling=" + acteursnamen_rolverdeling
				+ ", regisseur=" + regisseur + ", zender_id=" + zender_id + "]";
	}
}
