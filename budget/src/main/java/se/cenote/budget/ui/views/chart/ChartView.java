package se.cenote.budget.ui.views.chart;

import java.time.LocalDate;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class ChartView extends BorderPane{

	private StackPane stack;
	
	private Button pieBtn;
	private Button barBtn;
	
	private PieChartPanel piePanel;
	private BarChartPanel barPanel;
	
	private ComboBox<String> typeCmb;
	
	public ChartView() {
		initComponents();
		layoutComponents();
	}
	
	public void update(){
		piePanel.update();
		barPanel.update();
	}
	
	private void updateChart(boolean showPie){
		stack.getChildren().clear();
		
		if(showPie){
			stack.getChildren().add(piePanel);
			pieBtn.setDisable(true);
			barBtn.setDisable(false);
		}
		else{
			stack.getChildren().add(barPanel);
			pieBtn.setDisable(false);
			barBtn.setDisable(true);
		}
	}

	private void initComponents() {
		
		piePanel = new PieChartPanel();
		barPanel = new BarChartPanel();
		
		pieBtn = new Button("Fördelning år", new Glyph("FontAwesome", FontAwesome.Glyph.PIE_CHART));
		pieBtn.setOnAction( e -> updateChart(true));
		pieBtn.setDisable(true);
		
		barBtn = new Button("Netto per månad", new Glyph("FontAwesome", FontAwesome.Glyph.BAR_CHART));
		barBtn.setOnAction( e -> updateChart(false));
		
		typeCmb = new ComboBox<>();
		typeCmb.getItems().addAll("Fördelning År", "Netto månad" );
		typeCmb.getSelectionModel().selectFirst();
		typeCmb.valueProperty().addListener( (obs, ov, nv) -> {
			selectChart();
		});
	}
	
	private void selectChart(){
		int index = typeCmb.getSelectionModel().getSelectedIndex();
		updateChart(index == 0);
	}

	private void layoutComponents() {
		
		Label header = new Label("Årsbudget " + LocalDate.now().getYear());
		header.setAlignment(Pos.CENTER);
		header.setFont(Font.font(24));
		//lbl.setStyle("-fx-border-color: red");
		
		VBox centerBox = new VBox();
		centerBox.setAlignment(Pos.CENTER);
		centerBox.setSpacing(4);
		centerBox.getChildren().addAll(header, typeCmb);

		HBox topPanel = new HBox();
		topPanel.setAlignment(Pos.CENTER);
		topPanel.getChildren().addAll(centerBox);
		
		setTop(topPanel);
		
		stack = new StackPane();
		stack.getChildren().add(piePanel);
		setCenter(stack);
	}

}
