package se.cenote.budget.ui.views.table.spredsheet;

import java.util.Arrays;
import java.util.List;

import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import se.cenote.budget.AppContext;
import se.cenote.budget.model.budget.ArsBudget;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.konto.Konto;

public class BudgetSpreadsheetView extends BorderPane{
	
	private int year;
	
	private Text heading;
	private SpreadsheetView spreadSheet;

	public BudgetSpreadsheetView() {
		initComponents();
		layoutComponents();
	}

	private void initComponents() {
		
		this.year = AppContext.getInstance().getApp().getCurrYear();
		ArsBudget arsBudget = AppContext.getInstance().getApp().getArsBudget(year);
		
		heading = new Text("Månadsbudget: " + arsBudget.getYear());
		
		GridBase grid = buildGrid(arsBudget);

		spreadSheet = new SpreadsheetView(grid);
	}
	
	private void layoutComponents() {
		setPadding(new Insets(10, 0, 0, 0));
		
		FlowPane topPane = new FlowPane();
		topPane.setAlignment(Pos.BASELINE_CENTER);
		topPane.setPadding(new Insets(10));
		topPane.getChildren().add(heading);
		setTop(topPane);
		
		setCenter(spreadSheet);
	}
	
	private GridBase buildGrid(ArsBudget budget){

		int columnCount = 1 + 12 + 2; // Kontonamn + 12 månader + Total + Snitt
		int rowInCount = countRows(budget.getInKonton());
		int rowUtCount = countRows(budget.getUtKonton());
		int emptyRows = 3;
		int rowCount = rowInCount + rowUtCount + emptyRows;
		

		GridBase grid = new GridBase(rowCount, columnCount);

		ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
		int row = 0;
		
		// A) First part: Intäkter, Utgifter, Netto
		rows.add(asCells("Intäkter", budget.getTotalBudgetIn(), row++));
		rows.add(asCells("Utgifter", budget.getTotalBudgetUt(), row++));
		rows.add(asCells("Netto", budget.getTotalBudgetNetto(), row++));
		
		// B) Second part: Intäkter detalj
		rows.add(asCells("Intäkter", columnCount, row++));
		rows.addAll(asRows(true, budget, row));

		
		// C) Second part: Utgifter detalj
		rows.add(asCells("Utgifter", columnCount, row++));
		rows.addAll(asRows(false, budget, row));
		
		grid.setRows(rows);
		
		grid.spanColumn(15, 3, 0);
		//grid.spanColumn(15, 4, 0);
		
		//grid.spanColumn(15, 8, 0);
		grid.spanColumn(15, 7, 0);
		
		setHeaders(grid);
		
		return grid;
	}
	
	private ObservableList<ObservableList<SpreadsheetCell>> asRows(boolean isIn, ArsBudget arsBudget, int row){
		ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
		
		List<Konto> konton = isIn ? arsBudget.getInKonton() : arsBudget.getUtKonton();
		
		for(Konto kontoGrupp : konton){
			
			if(kontoGrupp.isLeaf()){
				KontoBudget kontoBudget = arsBudget.getBudget(kontoGrupp);
				rows.add(asCells(kontoGrupp, kontoBudget, row++, "   "));
			}
			else{
				// a) Add for each konto in grupp
				for(Konto konto : kontoGrupp.getChildren()){
					KontoBudget kontoBudget = arsBudget.getBudget(konto);
					rows.add(asCells(konto, kontoBudget, row++, "   "));
				}
				
				// b) Add total for grupp
				rows.add(asCells(kontoGrupp, arsBudget.getBudget(kontoGrupp), row++, ""));
			}
		}
		return rows;
	}
	
	private ObservableList<SpreadsheetCell> asCells(Konto konto, KontoBudget kontoBudget, int row, String prefix){
		return asCells(prefix + konto.getNamn(), kontoBudget, row);
	}
	
	private ObservableList<SpreadsheetCell> asCells(String kontoNamn, KontoBudget kontoBudget, int row){
		final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
		
		int column = 0;
		for(; column <= 12; column++){
			SpreadsheetCell cell = null;
			if(column > 0){
				cell = asCell(kontoBudget.getBelopp(column-1), row, column);
			}
			else{
				cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, kontoNamn);
			}
			
			list.add(cell);
		}
		
		list.add(asCell(kontoBudget.getTotal(), row, column++));
		list.add(asCell(kontoBudget.getAverage(), row, column));
		
		return list;
	}
	
	private static SpreadsheetCell asCell(double value, int row, int col){
		SpreadsheetCell cell = SpreadsheetCellType.DOUBLE.createCell(row, col, 1, 1, value);
		cell.getStyleClass().add("num_cell");
		cell.setFormat("#,##0");
		return cell;
	}
	
	private ObservableList<SpreadsheetCell> asCells(String kontoNamn, int cols, int row){
		final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
		
		for(int column = 0; column < cols; column++){
			SpreadsheetCell cell = null;
			if(column > 0){
				cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
			}
			else{
				cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, kontoNamn);
			}
			
			list.add(cell);
		}
		return list;
	}
	
	private ObservableList<SpreadsheetCell> asHeaderRow(String text, int span, int row){
		final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
		
		SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, text);
		list.add(cell);

		return list;
	}
	
	private static int countRows(List<Konto> kontoGrupper){
		int rowCount = 0;
		for(Konto kontoGrupp : kontoGrupper){
			rowCount += kontoGrupp.getChildren().size();
		}
		return rowCount;
	}
	
	private void setHeaders(GridBase grid){
		List<String> headers = Arrays.asList("Konto", "Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec", "Total", "Snitt");
		grid.getColumnHeaders().setAll(headers);
	}

}
