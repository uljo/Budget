package se.cenote.budget.dao;

import java.time.LocalDateTime;

import se.cenote.budget.model.budget.ArsBudget;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.budget.MonthDistribution;
import se.cenote.budget.model.konto.Konto;

public interface StoreManager {

	public ArsBudget getArsBudget(int year);
	
	public Konto addKonto(String name, Konto parent);

	public KontoBudget addKontoBudget(Konto konto, MonthDistribution monthDistribution, int year);

	public void update(Konto konto, int year, MonthDistribution monthDistribution);

	public void delete(Konto konto, int year);
	
	public void moveKonto(Konto newParent, Konto konto);

	public void load();

	public void store();
	
	public String getStoreName();
	
	public LocalDateTime getStoreTime();

}