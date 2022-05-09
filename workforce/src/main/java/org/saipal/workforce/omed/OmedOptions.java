package org.saipal.workforce.omed;

public class OmedOptions {
	public static enum Privacy {
		NORMAL, CONFIDENTIAL
	}

	public static enum Priority {
		NORMAL, URGENT, HIGHLYURGENT
	}

	public static enum Language {
		Np, En
	}

	public static enum Orgtype {
		GOVORG, INDIVIDUAL, OTHER
	}

	public static enum LetterType {
		LETTER, MEMO, REPORT, FORM
	}

	public static enum contentType {
		ATT, TEXT, ATTID
	}

	public static enum Action {
		FWD, FWDP, NEW, FIN, APP
	}
}
