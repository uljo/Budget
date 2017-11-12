package se.cenote.budget.dao.fs.budget;

import java.util.List;
import java.util.Map;

import se.cenote.budget.dao.fs.konto.KontoImpl;
import se.cenote.budget.model.budget.ArsBudget;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.konto.Konto;
import se.cenote.budget.model.konto.Konto.KontoTyp;

public class ArsBudgetImpl implements ArsBudget{
	
	private int year;
	
	private List<Konto> kontonIn;
	private List<Konto> kontonUt;
	
	private Map<Konto, KontoBudget> budgetByKonto;
	
	public ArsBudgetImpl(int year, List<Konto> kontonIn, List<Konto> kontonUt, Map<Konto, KontoBudget> budgetByKonto) {
		this.year = year;
		this.kontonIn = kontonIn;
		this.kontonUt = kontonUt;
		this.budgetByKonto = budgetByKonto;
	}

	@Override
	public int getYear() {
		return year;
	}

	@Override
	public List<Konto> getInKonton() {
		return kontonIn;
	}

	@Override
	public List<Konto> getUtKonton() {
		return kontonUt;
	}
	
	@Override
	public KontoBudget getTotalBudgetIn(){
		Konto kontoIn = kontonIn.get(0);
		return getBudget(kontoIn);
	}
	
	@Override
	public KontoBudget getTotalBudgetUt(){
		Konto kontoUt = kontonUt.get(0);
		return getBudget(kontoUt);
	}
	
	@Override
	public KontoBudget getTotalBudgetNetto(){
		KontoBudget budgetIn = getTotalBudgetIn();
		KontoBudget budgetUt = getTotalBudgetUt();
		double[] arr = KontoBudgetImpl.getDiff(budgetIn, budgetUt);
		return new KontoBudgetImpl(new KontoImpl("0", KontoTyp.NETTO, "Netto", ""), year, arr);
	}

	@Override
	public KontoBudget getBudget(Konto konto) {
		
		KontoBudget kontoBudget = budgetByKonto.get(konto);
		if(kontoBudget == null){
			kontoBudget = new KontoBudgetImpl(konto, year, getMonthArr(konto));
		}
		
		return kontoBudget;
	}
	
	public void add(KontoBudget budget){
		Konto konto = budget.getKonto();
	
		budgetByKonto.put(konto, budget);
	}
	
	public void delete(Konto konto){
		KontoImpl parent = (KontoImpl)konto.getParent();
		if(parent != null){
			parent.removeChild((KontoImpl)konto);
			budgetByKonto.remove(konto);
		}
	}
	
	private double[] getMonthArr(Konto konto){
		double[] arr = new double[14];
		buildBudget(konto, arr);
		
		for(int i = 0; i < 12; i++){
			arr[12] += arr[i];
		}
		arr[13] = arr[12]/12;
		
		//System.out.println("[getMonthArr] konto: " + konto.getNamn() + ", arr: " + Arrays.toString(arr));
		
		return arr;
	}
	
	private void buildBudget(Konto konto, double[] arr){
		
		if(!konto.isLeaf()){
			for(Konto child : konto.getChildren()){
				buildBudget(child, arr);
			}
		}
		else{
			KontoBudget kontoBudget = budgetByKonto.get(konto);
			for(int month = 1; month <= 12; month++){
				arr[month-1] += kontoBudget.getBelopp(month);
			}
		}
	}

}
