package se.cenote.budget.ui.views.chart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import se.cenote.budget.AppContext;
import se.cenote.budget.dao.fs.konto.KontoImpl;
import se.cenote.budget.model.budget.ArsBudget;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.konto.Konto;

public class PieChartPanel extends BorderPane{

	private PieChart pieChartIn;
	private PieChart pieChartUt;
	
	private StackPane stackIn;
	private StackPane stackUt;
	
	private Label captionIn;
	private Label captionUt;
	
	public PieChartPanel() {
		initComponents();
		layoutComponents();
	}
	
	public void update(){
		
		int year = AppContext.getInstance().getApp().getCurrYear();
		ArsBudget arsBudget = AppContext.getInstance().getApp().getArsBudget(year);
		
		ObservableList<PieChart.Data> dataIn = FXCollections.observableArrayList(getDataIn(arsBudget));
		ObservableList<PieChart.Data> dataUt = FXCollections.observableArrayList(getDataUt(arsBudget));
		
		Platform.runLater(() -> {
			pieChartIn.getData().clear();
			pieChartIn.getData().addAll(dataIn);
			registerMouseHandler(pieChartIn, captionIn);
			
			pieChartUt.getData().clear();
			pieChartUt.getData().addAll(dataUt);
			registerMouseHandler(pieChartUt, captionUt);
		});
	}
	
	private void registerMouseHandler(PieChart chart, Label caption){
		
		for (final PieChart.Data data : chart.getData()) {
			data.getNode().setOnMousePressed( e -> showLable(data, e, caption) );
			//data.getNode().setOnMouseExited( e -> hideLable(caption) );
		}
	}
	
	private void showLable(PieChart.Data data, MouseEvent e, Label caption){
		double x = e.getX();
    	double y = e.getY();
		caption.setTranslateX(x);
        caption.setTranslateY(y);
        String text = String.format("%,6.0f", data.getPieValue());
        caption.setText(text);
        System.out.println("[showLable] text: " + text + ", coord: " + x + ", " + y);
	}
	
	private void hideLable(Label caption){
		caption.setText("");
		System.out.println("[hideLable] text: " + caption.getText() + ", coord: ");
	}
	
	private List<PieChart.Data> getDataIn(ArsBudget arsBudget){
		Konto root = arsBudget != null ? (arsBudget.getInKonton().size() > 0 ? arsBudget.getInKonton().get(0) : null) : null;
		return getData(root, arsBudget);
	}
	
	private List<PieChart.Data> getDataUt(ArsBudget arsBudget){
		Konto root = arsBudget != null ? (arsBudget.getUtKonton().isEmpty() ? null : arsBudget.getUtKonton().get(0)) : null;
		return getData(root, arsBudget);
	}
	
	private List<PieChart.Data> getData(Konto root, ArsBudget arsBudget){
		
		List<PieChart.Data> list = new ArrayList<>();
		if(root != null){
			List<Konto> decendants = ((KontoImpl)root).getDecendents();
			for(Konto konto : decendants){
				
				if(konto.isInTyp() || konto.getLevel() == 2){
				
					KontoBudget kontoBudget = arsBudget.getBudget(konto);
					double amount = kontoBudget.getTotal();
					list.add(new PieChart.Data(konto.getNamn(), amount));
				}
			}
		}
		return list;
	}

	private void initComponents(){
		
		int year = AppContext.getInstance().getApp().getCurrYear();
		ArsBudget arsBudget = AppContext.getInstance().getApp().getArsBudget(year);
		
		ObservableList<PieChart.Data> dataIn = FXCollections.observableArrayList(getDataIn(arsBudget));
		pieChartIn = new PieChart(dataIn);
		pieChartIn.setTitle("Intäkter");
		
		ObservableList<PieChart.Data> pieChartDataUt = FXCollections.observableArrayList(getDataUt(arsBudget));
		pieChartUt = new PieChart(pieChartDataUt);
		pieChartUt.setTitle("Utgifter");
		
		captionIn = new Label("");
		captionIn.setTextFill(Color.DARKORANGE);
		captionIn.setStyle("-fx-font: 24 arial;");
		
		captionUt = new Label("");
		captionUt.setTextFill(Color.DARKORANGE);
		captionUt.setStyle("-fx-font: 24 arial;");
	}
	
	private void layoutComponents() {
		
		stackIn = new StackPane();
		stackIn.getChildren().addAll(pieChartIn, captionIn);
		//stackIn.setStyle("-fx-border-color: red");
		
		stackUt = new StackPane();
		stackUt.getChildren().addAll(pieChartUt, captionUt);
		//stackUt.setStyle("-fx-border-color: blue");
		
		HBox tilePane = new HBox();
		tilePane.getChildren().addAll(stackIn, stackUt);
		
		StackPane stack = new StackPane();
		stack.getChildren().addAll(tilePane);
		
		setCenter(stack);
	}

}
