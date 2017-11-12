package se.cenote.budget.ui.views.table.dlg;

import java.math.BigDecimal;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import se.cenote.budget.model.budget.MonthDistribution;
import se.cenote.budget.model.budget.MonthDistribution.DistributionType;

public class KontoBudgetPanel extends BorderPane{
	
	protected TextField nameFld;
	
	protected TextField amountFld;
	
	/**
	 * Available periods: 0 = Every, 1 = Single, 2 = Dual, 3 = Third, 4 = Fourth, 6 = Sixth
	 */
	protected ComboBox<DistributionTypeItem> periodCmb;
	
	/**
	 * First month for amount: 'N/A', 'Jan' - 'Dec'
	 */
	protected ComboBox<String> monthCmb;
	
	private MonthDistribution monthDistribution;
	
	private DecimalFormat fmt;
	private String thousandSep;

	public KontoBudgetPanel(MonthDistribution monthDistribution) {
		initComponents(monthDistribution);
		layoutComponents();
	}

	public MonthDistribution getMonthDistribution() {
		String name = nameFld.getText();
		BigDecimal amount = BigDecimal.ZERO;
		try{
			amount = new BigDecimal(amountFld.getText().replaceAll(thousandSep,"").replaceAll(",", "."));
		}
		catch(Exception e){
			System.out.println("[getMonthDistribution] ERROR - amount='" + amountFld.getText() + "' gives ex: " + e);
		}
		DistributionTypeItem typeItem = periodCmb.getSelectionModel().getSelectedItem();
		DistributionType type = typeItem.getType();
		int firstMonthNum = monthCmb.getSelectionModel().getSelectedIndex();
		return new MonthDistribution(name, amount, type, firstMonthNum);
	}
	
	boolean isDirty(){
		
		boolean typeChanged = !monthDistribution.getTyp().equals(periodCmb.getSelectionModel().getSelectedItem().getType());
		boolean monthChanged = monthDistribution.getFirstMonthNum() != monthCmb.getSelectionModel().getSelectedIndex();
		
		boolean dirty = typeChanged || monthChanged;
		System.out.println("[isDirty] dirty=" + dirty);
		return dirty;
	}
	
	private void changedPeriod(){
		int index = periodCmb.getSelectionModel().getSelectedIndex();
	    System.out.println("[changedPeriod] selected index: " + index);
	    
	    if(index != 0){
	    	monthCmb.getSelectionModel().select(1);
	    }
	    
	    monthCmb.setDisable(index == 0);
	}
	
	private void changedMonth(){
		String value = monthCmb.getSelectionModel().getSelectedItem();
	    System.out.println("[changedMonth] selected: " + value);
	}

	private void initComponents(MonthDistribution monthDistribution) {
		
		fmt = new DecimalFormat("#,##0");
		thousandSep = String.valueOf(fmt.getDecimalFormatSymbols().getGroupingSeparator());
		
		this.monthDistribution = monthDistribution;
		
		nameFld = new TextField();
		if(monthDistribution != null){
			nameFld.setText(monthDistribution.getKontoName());
			nameFld.setDisable(true);
		}
		else{
			nameFld.setPromptText("Kontonamn");
		}
		
		amountFld = new TextField();
		if(monthDistribution != null){
			amountFld.setText(fmt.format(monthDistribution.getAmount()));
		}
		else{
			amountFld.setPromptText("Kontobelopp");
		}
		
		List<DistributionTypeItem> periodItemList = new ArrayList<>();
		periodItemList.add(new DistributionTypeItem(DistributionType.MONTH_0, "Varje mån"));
		periodItemList.add(new DistributionTypeItem(DistributionType.MONTH_1, "Enskild mån"));
		periodItemList.add(new DistributionTypeItem(DistributionType.MONTHS_2, "Varannan mån"));
		periodItemList.add(new DistributionTypeItem(DistributionType.MONTHS_3, "3:e mån"));
		periodItemList.add(new DistributionTypeItem(DistributionType.MONTHS_4, "4:e mån"));
		periodItemList.add(new DistributionTypeItem(DistributionType.MONTHS_6, "6:e mån"));
		periodCmb = new ComboBox<>();
		periodCmb.getItems().addAll(periodItemList);
		if(monthDistribution != null){
			int index = monthDistribution.isEveryMonthType() ? 0
					: monthDistribution.isSingleMonthType() ? 1
							: monthDistribution.isSecondMonthType() ? 2 
									: monthDistribution.isThirdMonthType() ? 3 
											: monthDistribution.isFourthMonthType() ? 4 
													: 5;
			periodCmb.getSelectionModel().select(index);
		}
		else{
			periodCmb.getSelectionModel().selectFirst();
		}
		periodCmb.setOnAction((event) -> changedPeriod());
		periodCmb.setMaxWidth(Double.MAX_VALUE);
		periodCmb.setCellFactory(new Callback<ListView<DistributionTypeItem>,ListCell<DistributionTypeItem>>(){
            @Override
            public ListCell<DistributionTypeItem> call(ListView<DistributionTypeItem> l){
                return new ListCell<DistributionTypeItem>(){
                    @Override
                    protected void updateItem(DistributionTypeItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText("");
                        } 
                        else {
                            setText(item.getName());
                        }
                    }
                } ;
            }
        });
		
		
		monthCmb = new ComboBox<>();
		monthCmb.getItems().addAll(getMonthNameList());
		if(monthDistribution != null){
			int index = monthDistribution.getFirstMonthNum();
			monthCmb.getSelectionModel().select(index);
		}
		else{
			monthCmb.getSelectionModel().selectFirst();
		}
		monthCmb.setOnAction((event) -> changedMonth());
		monthCmb.setMaxWidth(Double.MAX_VALUE);
		monthCmb.setDisable(periodCmb.getSelectionModel().getSelectedIndex() == 0);
		
	}
	
	private static List<String> getMonthNameList(){
		String[] monthsArr = new DateFormatSymbols().getShortMonths();
		List<String> months = new ArrayList<>();
		months.add("N/A");
		for(String month : monthsArr){
			if(month.length() > 1){
				String item = month.substring(0, 1).toUpperCase() + month.substring(1);
				months.add(item);
			}
		}
		return months;
	}

	private void layoutComponents() {
		
		setPadding(new Insets(10));
		
		GridPane grid = new GridPane();
		grid.setHgap(5);
		grid.setVgap(8);
		grid.add(new Label("Namn:"), 0, 0);
		grid.add(nameFld, 1, 0);
		
		grid.add(new Label("Belopp:"), 0, 1);
		grid.add(amountFld, 1, 1);
		
		grid.add(new Label("Periodicitet:"), 0, 2);
		grid.add(periodCmb, 1, 2);
		
		grid.add(new Label("Första mån:"), 0, 3);
		grid.add(monthCmb, 1, 3);
		
		/*
		FlowPane btnPane = new FlowPane();
		btnPane.setAlignment(Pos.CENTER);
		btnPane.getChildren().addAll(saveBtn, cancelBtn);
		*/
		setCenter(grid);
	}


	class DistributionTypeItem{
		
		private DistributionType type;
		private String name;
		
		public DistributionTypeItem(DistributionType type, String name){
			this.type = type;
			this.name = name;
		}

		public DistributionType getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
		
	}

}
