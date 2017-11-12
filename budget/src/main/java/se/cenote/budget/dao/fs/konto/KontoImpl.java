package se.cenote.budget.dao.fs.konto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.cenote.budget.model.konto.Konto;

public class KontoImpl implements Konto{
	
	private String id;
	private KontoTyp typ;
	private String namn;
	private String desc;
	
	private Konto parent;
	private List<Konto> children;
	
	public KontoImpl(String id, KontoTyp typ, String namn, String desc){
		this(id, typ, namn, desc, null, null);
	}
	
	public KontoImpl(String id, KontoTyp typ, String namn, String desc, Konto parent, List<Konto> children){
		this.id = id;
		this.typ = typ;
		this.namn = namn;
		this.desc = desc;
		
		this.parent = parent;
		this.children = children;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	@Override
	public KontoTyp getTyp() {
		return typ;
	}
	
	public boolean isInTyp() {
		return KontoTyp.IN.equals(typ);
	}
	
	public boolean isUtTyp() {
		return KontoTyp.UT.equals(typ);
	}
	
	public boolean isNettoTyp() {
		return KontoTyp.NETTO.equals(typ);
	}
	
	public boolean isSameTyp(Konto other){
		return getTyp().equals(other.getTyp());
	}

	@Override
	public String getNamn() {
		return namn;
	}

	@Override
	public String getBeskrivning() {
		return desc;
	}
	
	@Override
	public int getLevel(){
		int count = 1;
		Konto ancestor = parent;
		while(ancestor != null){
			count++;
			ancestor = ancestor.getParent();
		}
		return count;
	}

	@Override
	public boolean isRoot() {
		return parent == null;
	}

	@Override
	public boolean isLeaf() {
		return children == null || children.isEmpty();
	}

	@Override
	public Konto getParent() {
		return parent;
	}

	@Override
	public List<Konto> getChildren() {
		return children != null ? children : Collections.emptyList();
	}
	
	@Override
	public List<Konto> getDecendents(){
		List<Konto> decendents = new ArrayList<>();
		if(!isLeaf()){
			for(Konto child : getChildren()){
				decendents.add(child);
				decendents.addAll(child.getDecendents());
			}
		}
		return decendents;
	}
	
	public boolean isChild(Konto konto){
		return getChildren().contains(konto);
	}
	
	@Override
	public String toString() {
		return "Konto(id=" + id + ", typ=" + typ + ", namn=" + namn + ", parent=" + (parent != null ? parent.getNamn() : null) + ", children.count="
				+ (children != null ? children.size() : 0) + ")";
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KontoImpl other = (KontoImpl) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void addChild(KontoImpl konto){
		if(children == null)
			children = new ArrayList<>();
		children.add(konto);
		
		if(konto.getParent() != null){
			konto.getParent().getChildren().remove(konto);
		}
		konto.setParent(this);
	}

	void setParent(Konto parent) {
		this.parent = parent;
	}

	public void removeChild(KontoImpl konto) {
		children.remove(konto);
		konto.setParent(null);
	}


}
