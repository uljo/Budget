package se.cenote.budget.ui.views.chart;

import java.text.DateFormatSymbols;
import java.time.LocalDate;

import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import se.cenote.budget.AppContext;
import se.cenote.budget.model.budget.ArsBudget;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.konto.Konto;

public class BarChartPanel extends StackPane{

	private BarChart<String,Number> chart;
	
	public BarChartPanel() {
		initComponents();
		layoutComponents();
	}
	
	public void update(){
		int year = AppContext.getInstance().getApp().getCurrYear();
		ArsBudget arsBudget = AppContext.getInstance().getApp().getArsBudget(year);
		
		if(arsBudget != null){
			Konto kontoIn = arsBudget.getInKonton().get(0);
	        KontoBudget kontoBudgetIn = arsBudget.getBudget(kontoIn);
	        XYChart.Series<String, Number> series1 = getSerie("Intäkter", kontoBudgetIn);
	        
	        Konto kontoUt = arsBudget.getUtKonton().get(0);
	        KontoBudget kontoBudgetUt = arsBudget.getBudget(kontoUt);
	        XYChart.Series<String, Number> series2 = getSerie("Utgifter", kontoBudgetUt);
	        
	        Platform.runLater(() -> {
	        	chart.getData().clear();
	            chart.getData().addAll(series1, series2);
			});
		}
	}

	private void initComponents() {
		final CategoryAxis xAxis = new CategoryAxis();
		xAxis.setLabel("Månad"); 
		
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Belopp");
        
        chart = new BarChart<String, Number>(xAxis, yAxis);
        //chart.setTitle("Årsbudget");
        
        int year = AppContext.getInstance().getApp().getCurrYear();
        ArsBudget arsBudget = AppContext.getInstance().getApp().getArsBudget(year);
        
        if(arsBudget != null){
	        Konto kontoIn = arsBudget.getInKonton().isEmpty() ? null : arsBudget.getInKonton().get(0);
	        if(kontoIn != null){
		        KontoBudget kontoBudgetIn = arsBudget.getBudget(kontoIn);
		        XYChart.Series<String, Number> series1 = kontoBudgetIn != null ? getSerie("Intäkter", kontoBudgetIn) : null;
		        if(series1 != null)
		        	chart.getData().add(series1);
	        }
	        
	        Konto kontoUt = arsBudget.getUtKonton().isEmpty() ? null : arsBudget.getUtKonton().get(0);
	        if(kontoUt != null){
	        	KontoBudget kontoBudgetUt = arsBudget.getBudget(kontoUt);
		        XYChart.Series<String, Number> series2 = kontoBudgetUt != null ? getSerie("Utgifter", kontoBudgetUt) : null;
		        
		        if(series2 != null)
		        	chart.getData().add(series2);
	        }
        }
	}
	
	private XYChart.Series<String, Number> getSerie(String name, KontoBudget kontoBudget){
		XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName(name);
        
        String[] months = new DateFormatSymbols().getShortMonths();
        for(int i = 1; i <= 12; i++){
        	double amount = kontoBudget.getBelopp(i);
        	serie.getData().add(new XYChart.Data<String, Number>(months[i-1], amount));
        }
        
        return serie;
	}
	
	private void layoutComponents(){
		getChildren().add(chart);
	}

}
