package se.cenote.budget.model.konto;

import java.util.List;

public interface Konto {
	
	public enum KontoTyp {IN, OUT, NETTO}
	
	public String getId();
	
	public KontoTyp getTyp();
	public boolean isInTyp();
	public boolean isUtTyp();
	public boolean isNettoTyp();
	public boolean isSameTyp(Konto konto);
	
	public String getNamn();
	
	public String getBeskrivning();
	
	public int getLevel();
	public boolean isRoot();
	public boolean isLeaf();
	
	public Konto getParent();
	public List<Konto> getChildren();
	public List<Konto> getDecendents();
	public boolean isChild(Konto child);
}
