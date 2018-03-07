package se.cenote.budget.dao.fs.budget;


import se.cenote.budget.dao.fs.StoreManagerImpl;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.budget.MonthDistribution;
import se.cenote.budget.model.konto.Konto;

public class KontoBudgetImpl implements KontoBudget{

	private Konto konto;
	private int year;
	
	/**
	 * Array holding all month amounts (zero-based) as well as total (index=12) and average amount (index = 13).
	 */
	private double[] monthAmounts;
	
	private MonthDistribution monthDistribution;
	
	public KontoBudgetImpl(Konto konto, int year, MonthDistribution monthDistribution) {
		this.konto = konto;
		this.year = year;
		
		this.monthDistribution = monthDistribution;
		
		this.monthAmounts = monthDistribution.asMonthAmounts();
	}
	
	public KontoBudgetImpl(Konto konto, int year, double[] monthAmounts) {
		
		if(monthAmounts.length != 14)
			throw new IllegalArgumentException("Array 'mounthAmounts' must have length 14!");
		
		this.konto = konto;
		this.year = year;
		
		this.monthAmounts = monthAmounts;
	}
	
	public KontoBudgetImpl(Konto konto, int year) {
		
		this.konto = konto;
		this.year = year;
		
		monthAmounts = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
	}

	@Override
	public Konto getKonto() {
		return konto;
	}

	@Override
	public int getYear() {
		return year;
	}
	
	@Override
	public MonthDistribution getMonthDistribution(){
		return monthDistribution;
	}
	
	public void setMonthDistribution(MonthDistribution monthDistribution){
		this.monthDistribution = monthDistribution;
		this.monthAmounts = monthDistribution.asMonthAmounts();
	}

	@Override
	public double getBelopp(int month) {
		if(month > 0 && month <= 12)
			return monthAmounts[month-1];
		else
			return 0;
	}
	
	@Override
	public double getTotal() {
		return monthAmounts[12];
	}
	
	@Override
	public double getAverage() {
		return monthAmounts[13];
	}
	
	@Override
	public double[] getBelopp(){
		return monthAmounts;
	}

	@Override
	public String toString() {
		return "KontoBudget(konto=" + (konto != null ? konto.getId() : null) + ", year=" + year + ", beloppByMonth="
				+ StoreManagerImpl.asList(monthAmounts) + ")";
	}
	
	
	public static double[] getDiff(KontoBudget budgetIn, KontoBudget budgetUt){
		double[] arr = new double[14];
		double sum = 0;
		for(int month = 1; month <= 12; month++){
			double netto = budgetIn.getBelopp(month) - budgetUt.getBelopp(month);
			arr[month-1] = netto;
			sum += netto;
		}
		arr[12] = sum;
		arr[13] = sum/12;
		return arr;
	}

}
