package se.cenote.budget.model.budget;

import java.math.BigDecimal;


public class MonthDistribution{
	
	public enum DistributionType {MONTH_0, MONTH_1, MONTHS_2, MONTHS_3, MONTHS_4, MONTHS_6}
	
	private String kontoName;
	private BigDecimal amount;
	private DistributionType type;
	private int firstMonthNum;
	
	public MonthDistribution(String kontoName, BigDecimal amount, DistributionType type, int firstMonthNum) {
		this.kontoName = kontoName;
		this.amount = amount;
		this.type = type;
		this.firstMonthNum = firstMonthNum;
	}
	
	public String getKontoName(){
		return kontoName;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public DistributionType getTyp() {
		return type;
	}
	
	public int getTypeId(){
		return type.ordinal();
	}
	
	public boolean isEveryMonthType(){
		return DistributionType.MONTH_0.equals(type);
	}
	
	public boolean isSingleMonthType(){
		return DistributionType.MONTH_1.equals(type);
	}
	
	public boolean isSecondMonthType(){
		return DistributionType.MONTHS_2.equals(type);
	}
	
	public boolean isThirdMonthType(){
		return DistributionType.MONTHS_3.equals(type);
	}
	
	public boolean isFourthMonthType(){
		return DistributionType.MONTHS_4.equals(type);
	}
	
	public boolean isSixedMonthType(){
		return DistributionType.MONTHS_6.equals(type);
	}
	
	public int getFirstMonthNum() {
		return firstMonthNum;
	}
	
	public double[] asMonthAmounts(){
		
		double[] arr = new double[14];
		
		if(isEveryMonthType()){
			for(int j = 0; j < 12; j++){
				arr[j] = amount.doubleValue();
			}
		}
		else if(isSingleMonthType()){
			arr[getFirstMonthNum()-1] = amount.doubleValue();
		}
		else{
			int step = 0;
			
			if(isSecondMonthType()){
				step = 2;
			}
			else if(isThirdMonthType()){
				step = 3;
			}
			else if(isFourthMonthType()){
				step = 4;
			}
			else if(isSixedMonthType()){
				step = 6;
			}
			
			for(int j = getFirstMonthNum()-1; j < 12; j += step){
				arr[j] = amount.doubleValue();
			}
			
			System.out.println("[asMonthAmounts] getFirstMonthNum=" + getFirstMonthNum() + ", step=" + step);
		}
		
		for(int i = 0; i< 12; i++){
			arr[12] += arr[i];
		}
		arr[13] = arr[12]/12;
		
		return arr;
	}

	@Override
	public String toString() {
		return "MonthDistribution(kontoName=" + kontoName + ", amount=" + amount + ", type=" + type + ", firstMonthNum="
				+ firstMonthNum + ")";
	}
	
	
}