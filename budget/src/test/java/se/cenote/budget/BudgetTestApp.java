package se.cenote.budget;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.cenote.budget.dao.StoreManager;
import se.cenote.budget.dao.fs.StoreManagerImpl;
import se.cenote.budget.dao.fs.budget.ArsBudgetImpl;
import se.cenote.budget.dao.fs.budget.KontoBudgetImpl;
import se.cenote.budget.dao.fs.konto.KontoImpl;
import se.cenote.budget.model.budget.ArsBudget;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.budget.MonthDistribution;
import se.cenote.budget.model.konto.Konto;
import se.cenote.budget.model.konto.Konto.KontoTyp;

public class BudgetTestApp {

	public static void main(String[] args) {
		
		BudgetTestApp app = new BudgetTestApp();
		
		//app.testA();
		//app.testB();
		app.testC();
	}
	
	private void testC(){
		
		int year = 2017;
		ArsBudgetImpl arsBudget = new ArsBudgetImpl(year);
		
		System.out.println("1");
		ArsBudgetPrinter.print(arsBudget);
		
		Konto parent = arsBudget.getInKonton().get(0);
		KontoImpl konto = new KontoImpl(null, KontoTyp.IN, "Test 1", "");
		((KontoImpl)parent).addChild(konto);
		double[] amounts = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 0, 0};
		KontoBudgetImpl budget = new KontoBudgetImpl(konto, year, amounts);
		
		//MonthDistribution monthDist = new MonthDistribution(kontoName, amount, type, firstMonthNum);
		//budget.setMonthDistribution(monthDist);
		arsBudget.add(budget);
		
		System.out.println("2");
		ArsBudgetPrinter.print(arsBudget);
	}
	
	private void testA(){
		ArsBudgetBuilder builder = new ArsBudgetBuilder(LocalDate.now().getYear());
		ArsBudget arsBudget = builder.getArsBudget();
	}
	
	private void testB(){
		StoreManager mgr = new StoreManagerImpl(new File("src/test/resources/"));
		mgr.load();
		ArsBudget arsBudget = mgr.getArsBudget(2017);
		
		ArsBudgetPrinter.print(arsBudget);
	}
	
	static class ArsBudgetBuilder{
		
		private int year;
		private Map<String, Konto> kontonByNamn;
		private int id;
		
		private List<Konto> kontonIn;
		private List<Konto> kontonUt;
		
		private Map<Konto, KontoBudget> budgetByKonto;
		
		private double beloppCounter;
		
		public ArsBudgetBuilder(int year){
			this.year = year;
			kontonByNamn = new HashMap<>();
		}
		
		public ArsBudget getArsBudget(){

			build();
			return new ArsBudgetImpl(year, kontonIn, kontonUt, budgetByKonto);
		}
	
		private void build(){

			// Konton
			kontonIn = buildKontonIn();
			kontonUt = buildKontonUt();
			
			// Budget
			budgetByKonto = new HashMap<>();
			
			buildKontoBudget("Lön uffe", "Intäkter");
			buildKontoBudget("Lön Li", "Intäkter");
			
			buildKontoBudget("Ränta lån", "Hus Sigtuna");
			buildKontoBudget("Amortering lån", "Hus Sigtuna");
			
			buildKontoBudget("Ränta lån", "Brf Uppsala");
			buildKontoBudget("Månadsavgift", "Brf Uppsala");
			
			buildKontoBudget("ICA", "Livsmedel");
			
		}
		
		private List<Konto> buildKontonIn(){
			List<Konto> kontonIn = new ArrayList<>();
			KontoImpl kontoParent = buildKonton("Intäkter", KontoTyp.IN, null);
			kontonIn.add(kontoParent);
			
			buildKonton("Lön uffe", KontoTyp.IN, kontoParent);
			buildKonton("Lön Li", KontoTyp.IN, kontoParent);
			
			return kontonIn;
		}
		
		private List<Konto> buildKontonUt(){
			List<Konto> kontonUt = new ArrayList<>();
			KontoImpl kontoParent = buildKonton("Utgifter", KontoTyp.OUT, null);
			kontonUt.add(kontoParent);
			
			KontoImpl kontoGrupp = buildKonton("Hus Sigtuna", KontoTyp.OUT, kontoParent);
			buildKonton("Ränta lån", KontoTyp.OUT, kontoGrupp);
			buildKonton("Amortering lån", KontoTyp.OUT, kontoGrupp);
			
			kontoGrupp = buildKonton("Brf Uppsala", KontoTyp.OUT, kontoParent);
			buildKonton("Ränta lån", KontoTyp.OUT, kontoGrupp);
			buildKonton("Månadsavgift", KontoTyp.OUT, kontoGrupp);
			
			kontoGrupp = buildKonton("Livsmedel", KontoTyp.OUT, kontoParent);
			buildKonton("ICA", KontoTyp.OUT, kontoGrupp);
			
			return kontonUt;
		}
		
		private KontoImpl buildKonton(String namn, KontoTyp typ, KontoImpl kontoParent){
			
			String key = namn;
			
			KontoImpl kontoChild = new KontoImpl(++id + "", typ, namn, "");
			if(kontoParent != null){
				kontoParent.addChild(kontoChild);
				
				key = kontoParent.getNamn() + "-" + namn;
			}
			
			
			if(kontoParent != null)
				
			kontonByNamn.put(key, kontoChild);
			//System.out.println("[buildKonton] key: " + key);
			
			return kontoChild;
		}
		
		private void buildKontoBudget(String namn, String parentNamn){
			String key = parentNamn != null ? (parentNamn + "-" + namn) : namn;
			
			//System.out.println("[buildKontoBudget] key: " + key);
			
			Konto konto = kontonByNamn.get(key);
			double[] beloppByMonth = new double[12];
			for(int i = 0; i < 12; i++){
				beloppByMonth[i] = ++beloppCounter;
			}
			KontoBudgetImpl kontoBudget = new KontoBudgetImpl(konto, year, beloppByMonth);
			budgetByKonto.put(kontoBudget.getKonto(), kontoBudget);
		}
	}
	
	static class ArsBudgetPrinter{
		
		static void print(ArsBudget arsBudget){
			
			System.out.println("Årsbudget för " + arsBudget.getYear());
			
			System.out.println("\nIntäkter");
			List<Konto> inKonton = arsBudget.getInKonton();
			int i = 1;
			for(Konto konto : inKonton){
				printChild(i++, konto, arsBudget);
			}
			
			System.out.println("\nUtgifter");
			List<Konto> utKonton = arsBudget.getUtKonton();
			int j = 1;
			for(Konto konto : utKonton){
				printChild(j++, konto, arsBudget);
			}
		}
		
		static void printChild(int i, Konto konto, ArsBudget arsBudget){
			
			printKonto(i, konto, arsBudget);
			
			if(!konto.getChildren().isEmpty()){
				i += 1;
				for(Konto child : konto.getChildren()){
					printChild(i, child, arsBudget);
				}
			}
		}
		
		static void printKonto(int i, Konto konto, ArsBudget arsBudget){
			
			//System.out.print(i + ". " + konto.getNamn() + "\t");
			int level = konto.getLevel();
			int size = 25-(level*3);
			String format = "%s%-" + size + "s";
			System.out.printf(format, asSpace(level), konto.getNamn());
			
			KontoBudget kontoBudget = arsBudget.getBudget(konto);
			if(kontoBudget != null){
				for(int month = 1; month <= 12; month++){
					System.out.printf("%5.0f ", kontoBudget.getBelopp(month));
				}
				System.out.printf("%5.0f ", kontoBudget.getTotal());
				System.out.printf("%5.0f ", kontoBudget.getAverage());
			}
			System.out.printf("%n");
		}
		
		static String asSpace(int count){
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < count; i++){
				builder.append("   ");
			}
			return builder.toString();
		}
	}

}
