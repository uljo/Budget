package se.cenote.budget.dao.fs.budget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
	
	private static final String TEXT_REVENUE = "Intäkter";
	private static final String TEXT_COSTS = "Utgifter";
	
	public ArsBudgetImpl(int year, List<Konto> kontonIn, List<Konto> kontonUt, Map<Konto, KontoBudget> budgetByKonto) {
		this.year = year;
		this.kontonIn = kontonIn;
		this.kontonUt = kontonUt;
		this.budgetByKonto = budgetByKonto;
	}
	
	public ArsBudgetImpl(int year) {
		
		this.year = year;
		
		
		this.kontonIn = new ArrayList<>();
		Konto kontoIn = new KontoImpl("In", KontoTyp.IN, TEXT_REVENUE, "");
		kontonIn.add(kontoIn);
		
		this.kontonUt = new ArrayList<>();
		Konto kontoUt = new KontoImpl("Ut", KontoTyp.OUT, TEXT_COSTS, "");
		kontonUt.add(kontoUt);

		
		this.budgetByKonto = new HashMap<Konto, KontoBudget>();
		/*
		KontoBudget budgetIn = new KontoBudgetImpl(kontoIn, year);
		budgetByKonto.put(kontoIn, budgetIn);
		
		KontoBudget budgetUt = new KontoBudgetImpl(kontoUt, year);
		budgetByKonto.put(kontoUt, budgetUt);
		*/
	}

	@Override
	public int getYear() {
		return year;
	}

	@Override
	public List<Konto> getInKonton() {
		return kontonIn != null ? kontonIn : Collections.emptyList();
	}

	@Override
	public List<Konto> getUtKonton() {
		return kontonUt != null ? kontonUt : Collections.emptyList();
	}
	
	@Override
	public KontoBudget getTotalBudgetIn(){
		Konto kontoIn = getInKonton().get(0);
		return getBudget(kontoIn);
	}
	
	@Override
	public KontoBudget getTotalBudgetUt(){
		Konto kontoUt = getUtKonton().get(0);
		return getBudget(kontoUt);
	}
	
	@Override
	public KontoBudget getTotalBudgetNetto(){
		KontoBudget budgetIn = getTotalBudgetIn();
		KontoBudget budgetUt = getTotalBudgetUt();
		double[] arr = KontoBudgetImpl.getDiff(budgetIn, budgetUt);
		
		System.out.println("[getTotalBudgetNetto] arr: " + asList(arr));
		
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
			if(kontoBudget != null){
				for(int month = 1; month <= 12; month++){
					arr[month-1] += kontoBudget.getBelopp(month);
				}
			}
		}
	}
	
	private static List<Double> asList(double[] arr){
		List<Double> list = new ArrayList<>();
		for(double v : arr){
			list.add(v);
		}
		return list;
	}

}
