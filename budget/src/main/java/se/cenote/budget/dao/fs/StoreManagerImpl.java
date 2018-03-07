package se.cenote.budget.dao.fs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.cenote.budget.dao.StoreManager;
import se.cenote.budget.dao.fs.budget.ArsBudgetImpl;
import se.cenote.budget.dao.fs.budget.KontoBudgetImpl;
import se.cenote.budget.dao.fs.konto.KontoImpl;
import se.cenote.budget.model.budget.ArsBudget;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.budget.MonthDistribution;
import se.cenote.budget.model.budget.MonthDistribution.DistributionType;
import se.cenote.budget.model.konto.Konto;
import se.cenote.budget.model.konto.Konto.KontoTyp;

public class StoreManagerImpl implements StoreManager {
	
	private static final String FILE_NAME = "budget.ser";
	
	private static final String SECTION_ACCOUNT = "[account-";
	private static final String SECTION_BUDGET = "[budget-";
	
	private static final String TYPE_IN = "IN";
	private static final String TYPE_OUT = "OUT";
	
	private File storeFile;
	
	private Map<Integer, ArsBudget> arsBudgetByYear;

	public StoreManagerImpl(File dir){
		storeFile = new File(dir, FILE_NAME);
		
		if(!storeFile.exists()){
			try{
				if(storeFile.createNewFile())
					System.out.println("Created new storage file: " + storeFile.getAbsolutePath());
			}
			catch(Exception e){
				throw new RuntimeException("Could not create file " + storeFile.getAbsolutePath());
			}
		}	
	}
	
	@Override
	public ArsBudget getArsBudget(int year){
		ArsBudget arsBudget = arsBudgetByYear.get(year);
		if(arsBudget == null){
			arsBudget = new ArsBudgetImpl(year);
			arsBudgetByYear.put(year, arsBudget);
		}
		return arsBudget;
	}
	
	@Override
	public Konto addKonto(String name, Konto parent){
		if(parent != null){
			String parentId = parent.getId();
			String id = parentId + "-" + (parent.getChildren().size() + 1);
			KontoTyp typ = parent.getTyp();
			KontoImpl konto = new KontoImpl(id, typ, name, "");
			((KontoImpl)parent).addChild(konto);
			return konto;
		}
		else
			return null;
	}
	

	@Override
	public KontoBudget addKontoBudget(Konto konto, MonthDistribution monthDistribution, int year){
		
		KontoBudget kontoBudget = new KontoBudgetImpl(konto, year, monthDistribution);
		
		ArsBudgetImpl arsBudget = (ArsBudgetImpl)arsBudgetByYear.get(year);
		if(arsBudget != null){
			arsBudget.add(kontoBudget);
		}
		return kontoBudget;
	}
	

	@Override
	public void update(Konto konto, int year, MonthDistribution monthDistribution) {
		ArsBudgetImpl arsBudget = (ArsBudgetImpl)arsBudgetByYear.get(year);
		if(arsBudget != null){
			KontoBudgetImpl kontoBudget = (KontoBudgetImpl)arsBudget.getBudget(konto);
			kontoBudget.setMonthDistribution(monthDistribution);
		}
	}
	
	@Override
	public void delete(Konto konto, int year){
		ArsBudgetImpl arsBudget = (ArsBudgetImpl)arsBudgetByYear.get(year);
		if(arsBudget != null){
			arsBudget.delete(konto);
		}
	}
	
	@Override
	public void moveKonto(Konto newParent, Konto konto) {
		((KontoImpl)newParent).addChild((KontoImpl)konto);
	}
	
	@Override
	public void load(){
		
		BudgetFileParser parser = new BudgetFileParser();
		
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(storeFile));
			
			String line = null;
			while((line = reader.readLine()) != null){
				
				parser.parse(line);
			}
			
			System.out.println("[load] loaded " + parser.getKontonById().keySet().size() + " konton from file: " + storeFile.getAbsolutePath());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			if(reader != null){
				try {
					reader.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		//Map<String, Konto> kontonById = parser.getKontonById();
		arsBudgetByYear = new HashMap<>();
		
		for(int year : parser.getYears()){
			Map<Konto, KontoBudget> budgetByKonto = parser.getBudgetById(year);
			
			List<Konto> kontonIn = new ArrayList<Konto>();
			kontonIn.add(parser.getKontoIn());
			
			List<Konto> kontonUt = new ArrayList<Konto>();
			kontonUt.add(parser.getKontoUt());
			
			ArsBudget arsBudget = new ArsBudgetImpl(year, kontonIn, kontonUt, budgetByKonto);
			arsBudgetByYear.put(arsBudget.getYear(), arsBudget);
		}
		System.out.println("arsBudgetByYear.size=" + arsBudgetByYear.size());
	}
	
	
	@Override
	public void store(){
		BudgetFileWriter writer = new BudgetFileWriter();
		writer.write(storeFile, arsBudgetByYear);
		System.out.println("[store] Saved " + arsBudgetByYear.size() + " konton to file: " + storeFile);
	}
	
	@Override
	public String getStoreName(){
		return storeFile.getAbsolutePath();
	}
	
	public LocalDateTime getStoreTime(){
		return Instant.ofEpochMilli(storeFile.lastModified()).atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	
	private static String asId(String[] idArr){
		String key = "";
		for(int i = 0; i < idArr.length; i++){
			key += idArr[i];
			
			if(i < idArr.length-1){
				key += "-";
			}
		}
		return key;
	}
	
	private static String asParentId(String[] idArr){
		String key = null;
		
		if(idArr.length > 1){
			int i = 0;
			key = idArr[i++];
			while(i < idArr.length-1){
				key += "-" + idArr[i++];
			}
		}
		return key;
	}
	
	public static String asList(double[] arr){
		StringBuilder builder = new StringBuilder("[");
		for(int i = 0; i < arr.length; i++){
			builder.append(Double.toString(arr[i]));
			
			if(i < arr.length-1)
				builder.append(", ");
		}
		builder.append("]");
		return builder.toString();
	}
	
	/**
	 * Class for persisting Konton and Budget on files.
	 * <p>
	 * <code>
	 * [account]</br>
	 * KontoId$KontoType$Name</br>
	 * </br>
	 * [budget]</br>
	 * KontoId$BudgetType$MonthNumber$Amount</br>
	 * 
	 * --</br>
	 * BudgetType : 0 = Every month, 1 = One single month, 2 = Varannan, 3 = Third months, 4 = Fourths months, 6 = Sixth months</br>
	 * MonthNumber : 1-12 indicating first month for the amount. Only valid for types 1-6. If budgetTpe = 0 then MonthNumber = 0</br>
	 * </code>
	 * @author uffe
	 *
	 */
	class BudgetFileWriter{
		
		private DecimalFormat fmt = new DecimalFormat("###0");
		
		public BudgetFileWriter(){}
		
		public void write(File file, Map<Integer, ArsBudget> map){
			PrintWriter writer = null;
			
			try{
				writer = new PrintWriter(new FileWriter(file));
				
				for(ArsBudget arsBudget : map.values()){
				
					// Konton
					writer.println(SECTION_ACCOUNT + Integer.toString(arsBudget.getYear()) + "]");
					
					// In
					Konto konto = arsBudget.getInKonton().get(0);
					writeKonton(konto, writer);
					
					writer.println();
					// Out
					konto = arsBudget.getUtKonton().get(0);
					writeKonton(konto, writer);
					
					// Budget
					writer.println("\n" + SECTION_BUDGET + Integer.toString(arsBudget.getYear()) + "]");
					
					konto = arsBudget.getInKonton().get(0);
					writeBudgets(konto, arsBudget, writer);
					
					konto = arsBudget.getUtKonton().get(0);
					writeBudgets(konto, arsBudget, writer);
				}
				
			}
			catch(Exception e){
				e.printStackTrace();
			}
			finally{
				writer.close();
			}
		}
		
		private void writeKonton(Konto root, PrintWriter writer){
			String typ = root.isInTyp() ? TYPE_IN : TYPE_OUT;
			String id = root.getId();
			writer.println(id + "$" + typ + "$" + root.getNamn());
			
			if(!root.isLeaf()){
				writeChildren(id, root.getChildren(), writer);
			}
		}
		
		private void writeChildren(String id, List<Konto> children, PrintWriter writer){
			int i = 1;
			for(Konto child : children){
				String currId = id + "." + i++;
				String typ = child.isInTyp() ? TYPE_IN : TYPE_OUT;
				writer.println(currId + "$" + typ + "$" + child.getNamn());
				
				if(!child.isLeaf())
					writeChildren(currId, child.getChildren(), writer);
			}
		}
		
		private void writeBudgets(Konto root, ArsBudget arsBudget, PrintWriter writer){
			String id = root.getId();
			
			writeBudgetChildren(id, root.getChildren(), arsBudget, writer);
		}
		
		private void writeBudgetChildren(String id, List<Konto> children, ArsBudget arsBudget, PrintWriter writer){
			int i = 1;
			for(Konto konto : children){
				String currId = id + "." + i++;
				
				if(konto.isLeaf()){
					KontoBudget kontoBudget = arsBudget.getBudget(konto);
					writer.println(asText(currId, kontoBudget));
				}
				
				if(!konto.isLeaf()){
					writeBudgetChildren(currId, konto.getChildren(), arsBudget, writer);
				}
			}
		}
		
		/**
		 * Format: kontoId$year$budgetType$monthNumber$amount
		 * 
		 * @param currId
		 * @param kontoBudget
		 * @return
		 */
		private String asText(String currId, KontoBudget kontoBudget){
			
			StringBuilder builder = new StringBuilder();
			
			MonthDistribution dist = kontoBudget.getMonthDistribution();
			
			builder.append(asDotId(currId));
			builder.append("$");
			
			builder.append(Integer.toString(dist.getTypeId()));
			builder.append("$");
			
			builder.append(dist.getTypeId() == 0 ? "0" : Integer.toString(dist.getFirstMonthNum()));
			builder.append("$");
			
			builder.append(fmt.format(dist.getAmount()));
			
			return builder.toString();
		}
		
		
		private String asDotId(String text){
			return text != null ? text.replaceAll("-", ".") : text;
		}
	}

	/**
	 * 
	 * @author uljo
	 *
	 */
	class BudgetFileParser{

		boolean kontoMode = false;
		int year;
		
		private Konto kontoIn;
		private Konto kontoUt;
		private Map<String, Konto> kontonById;
		private Map<Integer, Map<Konto, KontoBudget>> budgetByYearById;
		
		public BudgetFileParser(){
			kontonById = new HashMap<>();
			budgetByYearById = new HashMap<>();
		}
		
		public Map<String, Konto> getKontonById(){
			return kontonById;
		}
		
		public Konto getKontoIn(){
			return kontoIn;
		}
		
		public Konto getKontoUt(){
			return kontoUt;
		}
		
		public List<Integer> getYears(){
			return new ArrayList<Integer>(budgetByYearById.keySet());
		}
		
		public Map<Konto, KontoBudget> getBudgetById(int year){
			return budgetByYearById.get(year);
		}
		
		public Map<Integer, Map<Konto, KontoBudget>> getBudgetByYearById(){
			return budgetByYearById;
		}
		
		void parse(String line){

			//System.out.println("[load] line: " + line);
			if(line.length() > 0){
				if(line.startsWith(SECTION_ACCOUNT)){
					String yearPart = line.substring(SECTION_ACCOUNT.length(), line.length()-1);
					year = Integer.parseInt(yearPart);
					kontoMode = true;
				}
				else if(line.startsWith(SECTION_BUDGET)){
					String yearPart = line.substring(SECTION_BUDGET.length(), line.length()-1);
					year = Integer.parseInt(yearPart);
					kontoMode = false;
				}
				else if(kontoMode){
					parseKonto(line);
				}
				else{
					parseKontoBudget(line);
				}
			}
		}
		
		/**
		 * 
		 * Id $ Typ $ Namn<br>
		 * 1$I$Intäkter<br>
		 * 1.1$I$Lön Li
		 * @param line
		 */
		private void parseKonto(String line){
			String[] parts = line.split("\\$");
			String[] idParts = parts[0].split("\\.");
			KontoTyp typ = KontoTyp.valueOf(parts[1]);
			String namn = parts[2];
			
			String id = asId(idParts);
			KontoImpl konto = new KontoImpl(id, typ, namn, null);
			kontonById.put(konto.getId(), konto);

			String parentId = asParentId(idParts);
			Konto parent = kontonById.get(parentId);
			if(parent != null)
				((KontoImpl)parent).addChild(konto);
			else if(konto.isInTyp())
				kontoIn = konto;
			else if(konto.isUtTyp())
				kontoUt = konto;
		}
		
		/**
		 * Format: kontoId$budgetType$monthNumber$amount
		 * 
		 * @param line
		 */
		private void parseKontoBudget(String line){
			
			String[] parts = line.split("\\$");
			
			String[] idParts = parts[0].split("\\.");
			String kontoId = asId(idParts);
			
			int typeId = Integer.parseInt(parts[1]);
			DistributionType type = DistributionType.values()[typeId];
			
			int firstMonth = Integer.parseInt(parts[2]);
			
			BigDecimal amount = new BigDecimal(parts[3].replace(",", "."));
			
			Konto konto = kontonById.get(kontoId);
			if(konto == null)
				throw new RuntimeException("Parser error - Unknown kontoId: " + kontoId);
			

			MonthDistribution dist = new MonthDistribution(konto.getNamn(), amount, type, firstMonth);
			
			KontoBudgetImpl kontoBudget = new KontoBudgetImpl(konto, year, dist);
			
			// Save in memory
			Map<Konto, KontoBudget> map = budgetByYearById.get(year);
			if(map == null){
				map = new HashMap<>();
				budgetByYearById.put(year, map);
			}
			map.put(kontoBudget.getKonto(), kontoBudget);
			//System.out.println("[load] kontoBudget: " + kontoBudget);
			//System.out.println("idParts: " + Arrays.asList(idParts) + ", arr: " + asList(arr));
		}
	}

}
