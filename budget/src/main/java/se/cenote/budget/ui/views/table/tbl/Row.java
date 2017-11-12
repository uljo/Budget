package se.cenote.budget.ui.views.table.tbl;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.konto.Konto;
import se.cenote.budget.ui.views.table.BudgetView;

/**
 * 
 * @author uljo
 *
 */
public class Row{
	
	/**
	 * 
	 */
	private final BudgetView budgetView;

	//private Konto konto;
	private KontoBudget kontoBudget;
	
	private boolean visibleChildren;
	
	private final SimpleStringProperty kontoNamnProp;
	
	private final SimpleDoubleProperty amount1Prop;
	private final SimpleDoubleProperty amount2Prop;
	private final SimpleDoubleProperty amount3Prop;
	private final SimpleDoubleProperty amount4Prop;
	private final SimpleDoubleProperty amount5Prop;
	private final SimpleDoubleProperty amount6Prop;
	private final SimpleDoubleProperty amount7Prop;
	private final SimpleDoubleProperty amount8Prop;
	private final SimpleDoubleProperty amount9Prop;
	private final SimpleDoubleProperty amount10Prop;
	private final SimpleDoubleProperty amount11Prop;
	private final SimpleDoubleProperty amount12Prop;
	
	private final SimpleDoubleProperty amountSumProp;
	private final SimpleDoubleProperty amountAverageProp;
	
	public Row(BudgetView budgetView, KontoBudget kontoBudget){
		
		this.budgetView = budgetView;
		Konto konto = null;
		double[] arr = null;
		if(kontoBudget != null){
			arr = kontoBudget.getBelopp();
			konto = kontoBudget.getKonto();
		}
		else
			arr = getArrEmpty();
		
		visibleChildren = true;
		
		this.kontoBudget = kontoBudget;
		
		String name = updateDisplayName(konto, visibleChildren);
		this.kontoNamnProp = new SimpleStringProperty(name);
		
		this.amount1Prop = new SimpleDoubleProperty(arr[0]);
		this.amount2Prop = new SimpleDoubleProperty(arr[1]);
		this.amount3Prop = new SimpleDoubleProperty(arr[2]);
		this.amount4Prop = new SimpleDoubleProperty(arr[3]);
		this.amount5Prop = new SimpleDoubleProperty(arr[4]);
		this.amount6Prop = new SimpleDoubleProperty(arr[5]);
		this.amount7Prop = new SimpleDoubleProperty(arr[6]);
		this.amount8Prop = new SimpleDoubleProperty(arr[7]);
		this.amount9Prop = new SimpleDoubleProperty(arr[8]);
		this.amount10Prop = new SimpleDoubleProperty(arr[9]);
		this.amount11Prop = new SimpleDoubleProperty(arr[10]);
		this.amount12Prop = new SimpleDoubleProperty(arr[11]);
		
		double sum = arr.length > 12 ? arr[12] : 0;
		double avg = arr.length > 13 ? arr[13] : 0;
		
		this.amountSumProp = new SimpleDoubleProperty(sum);
		this.amountAverageProp = new SimpleDoubleProperty(avg);
	}
	
	public boolean isMoveAccepted(Row draggedRow) {
		boolean result = false;
		
		Konto draggedKonto = draggedRow != null ? draggedRow.getKonto() : null;
		Konto konto = getKonto();
		
		if( konto != null && draggedKonto != null && konto.isSameTyp(draggedKonto)){
			/*
			if(konto.isLeaf()){
				Konto parent = konto.getParent();
				result = !parent.isChild(draggedKonto);
			}
			else{
				result = !konto.isChild(draggedKonto);
			}
			*/
			result = true;
		}
		return result;
	}

	public Konto getKonto(){
		return kontoBudget != null ? kontoBudget.getKonto() : null;
	}
	
	public KontoBudget getKontoBudget(){
		return kontoBudget;
	}
	
	public void setChildrenVisible(boolean value){
		
		System.out.println("Row [setChildrenVisible] value=" + value);
		
		if(value != visibleChildren){
			String name = updateDisplayName(getKonto(), value);
			System.out.println("Row [setChildrenVisible] name=" + name);
			kontoNamnProp.set(name);
		}
		visibleChildren = value;
	}
	
	public boolean isChildrenVisible(){
		return visibleChildren;
	}
	
	public String getKontoNamn(){
		
		return kontoNamnProp.get();
	}
	
	public double getAmount1(){
		return amount1Prop.get();
	}
	
	public double getAmount2(){
		return amount2Prop.get();
	}
	
	public double getAmount3(){
		return amount3Prop.get();
	}
	
	public double getAmount4(){
		return amount4Prop.get();
	}
	
	public double getAmount5(){
		return amount5Prop.get();
	}
	
	public double getAmount6(){
		return amount6Prop.get();
	}
	
	public double getAmount7(){
		return amount7Prop.get();
	}
	
	public double getAmount8(){
		return amount8Prop.get();
	}
	
	public double getAmount9(){
		return amount9Prop.get();
	}
	
	public double getAmount10(){
		return amount10Prop.get();
	}
	
	public double getAmount11(){
		return amount11Prop.get();
	}
	
	public double getAmount12(){
		return amount12Prop.get();
	}
	
	public double getSum(){
		return amountSumProp.get();
	}
	
	public double getAvg(){
		return amountAverageProp.get();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getOuterType().hashCode();
		result = prime * result + ((kontoBudget.getKonto() == null) ? 0 : kontoBudget.getKonto().hashCode());
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
		Row other = (Row) obj;
		if (!getOuterType().equals(other.getOuterType()))
			return false;
		if (getKonto() == null) {
			if (other.getKonto() != null)
				return false;
		} else if (!getKonto().equals(other.getKonto()))
			return false;
		return true;
	}

	private BudgetView getOuterType() {
		return this.budgetView;
	}
	
	private String updateDisplayName(Konto konto, boolean isVisible){
		String name = "";
		if(konto != null){
			String prefix = getSpaces(konto.getLevel());
			
			String icon = "";
			if(!konto.isLeaf()){
				icon = isVisible ? "-" : "+";
			}
			name = prefix + icon + konto.getNamn();
		}
		return name;
	}
	
	private String getSpaces(int level) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < level; i++)
			builder.append("  ");
		return builder.toString();
	}
	
	private double[] getArrEmpty(){
		double[] arr = new double[14];
		for(int i = 0; i < arr.length; i++){
			arr[i] = -1;
		}
		return arr;
	}
	
}