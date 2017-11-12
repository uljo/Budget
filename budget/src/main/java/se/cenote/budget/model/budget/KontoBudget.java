package se.cenote.budget.model.budget;

import se.cenote.budget.model.konto.Konto;

public interface KontoBudget {

	public Konto getKonto();
	
	public int getYear();
	
	public MonthDistribution getMonthDistribution();
	
	/**
	 * Return amount for specified month (January = 1, December = 12).
	 * @param month
	 * @return
	 */
	public double getBelopp(int month);
	
	/**
	 * Get total amount.
	 * @return
	 */
	public double getTotal();
	
	/**
	 * Get average monthly amount. 
	 * @return
	 */
	public double getAverage();
	
	/**
	 * Retrieve all amounths as an array.
	 * @return
	 */
	public double[] getBelopp();
}
