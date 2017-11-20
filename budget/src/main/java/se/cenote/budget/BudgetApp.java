package se.cenote.budget;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;

import se.cenote.budget.dao.StoreManager;
import se.cenote.budget.dao.StoreManagerFactory;
import se.cenote.budget.model.budget.ArsBudget;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.budget.MonthDistribution;
import se.cenote.budget.model.konto.Konto;
import se.cenote.budget.ui.AppWindow;

public class BudgetApp {
	
	private int currYear;
	
	private StoreManager storeMgr;

	public static void main(String[] args) {
		AppContext.getInstance();
		AppWindow.show();
	}
	
	public BudgetApp() {
		Properties props = new Properties(); //TODO: read from file
		storeMgr = new StoreManagerFactory(props).getStoreManager();
		storeMgr.load();
		
		currYear = LocalDate.now().getYear();
	}
	
	public void stop(){
		storeMgr.store();
	}
	
	public ArsBudget getArsBudget(int year){
		return storeMgr.getArsBudget(year);
	}
	
	public Konto addKonto(String kontoNamn, Konto parent){
		return storeMgr.addKonto(kontoNamn, parent);
	}

	public KontoBudget addKontoBudget(Konto konto, MonthDistribution monthDistribution, int year) {
		
		return storeMgr.addKontoBudget(konto, monthDistribution, year);
	}
	
	public void updateKontoBudget(Konto konto, MonthDistribution monthDistribution, int year) {
		storeMgr.update(konto, year, monthDistribution);
	}
	
	public void deleteKontoBudget(Konto konto, int year){
		storeMgr.delete(konto, year);
	}

	public String getStoreFile() {
		return storeMgr.getStoreName();
	}
	
	public LocalDateTime getStoreTime() {
		return storeMgr.getStoreTime();
	}

	public void moveChild(Konto parent, Konto draggedKonto) {
		storeMgr.moveKonto(parent, draggedKonto);
	}



}
