package se.cenote.budget.model.budget;

import java.util.List;

import se.cenote.budget.model.konto.Konto;

public interface ArsBudget {

	public int getYear();
	public List<Konto> getInKonton();
	public List<Konto> getUtKonton();
	
	public KontoBudget getBudget(Konto konto);
	
	public KontoBudget getTotalBudgetIn();
	public KontoBudget getTotalBudgetUt();
	public KontoBudget getTotalBudgetNetto();
}
