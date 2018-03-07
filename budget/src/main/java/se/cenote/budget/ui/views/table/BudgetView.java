package se.cenote.budget.ui.views.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import se.cenote.budget.AppContext;
import se.cenote.budget.model.budget.ArsBudget;
import se.cenote.budget.model.budget.MonthDistribution;
import se.cenote.budget.model.budget.KontoBudget;
import se.cenote.budget.model.konto.Konto;
import se.cenote.budget.ui.views.table.dlg.RowDialog;
import se.cenote.budget.ui.views.table.tbl.BudgetTable;
import se.cenote.budget.ui.views.table.tbl.Row;
import se.cenote.budget.ui.views.table.tbl.BudgetTable.RowDragListener;

public class BudgetView extends BorderPane{

	private int year;
	private BudgetTable tbl;

	public BudgetView() {
		initComponents();
		layoutComponents();
	}
	
	public void update(){
		ArsBudget budget = AppContext.getInstance().getApp().getArsBudget(year);
		
		List<Row> rows = new ArrayList<>();
		
		if(budget != null){
			rows.add(asRow(budget.getTotalBudgetIn()));
			rows.add(asRow(budget.getTotalBudgetUt()));
			rows.add(asRow(budget.getTotalBudgetNetto()));
			
			rows.add(new Row(this, null));
			
			Konto kontoIn = budget.getInKonton().get(0);
			addRow(kontoIn, budget, rows);
			
			rows.add(new Row(this, null));
			
			Konto kontoUt = budget.getUtKonton().get(0);
			addRow(kontoUt, budget, rows);
		}
		System.out.println("[update] year=" + year + ", rows.size: " + rows.size());
		
		tbl.update(rows);
	}
	
	private void addRow(Konto konto, ArsBudget arsBudget, List<Row> rows){
		KontoBudget kontoBudget = arsBudget.getBudget(konto);
		rows.add(asRow(kontoBudget));
		if(!konto.isLeaf()){
			for(Konto k : konto.getChildren()){
				addRow(k, arsBudget, rows);
			}
		}
	}
	
	private Row asRow(KontoBudget inBudget) {
		return new Row(this, inBudget);
	}
	
	private void updateBudget(){

		Row row = tbl.getSelectionModel().getSelectedItem();
		KontoBudget kontoBudget = row.getKontoBudget();
		if(kontoBudget != null){
			RowDialog dlg = new RowDialog(kontoBudget.getMonthDistribution());
			Optional<MonthDistribution> result = dlg.showAndWait();
			result.ifPresent(monthDistribution -> {
				
				System.out.println("[updateBudget] " + monthDistribution);
				
				Konto konto = kontoBudget.getKonto();
				if(konto != null){
					AppContext.getInstance().getApp().updateKontoBudget(konto, monthDistribution, year);
					update();
				}
			});
		}
	}
	
	private void addGroup(){
		Row row = tbl.getSelectionModel().getSelectedItem();
		Konto parentKonto = row.getKonto();
		
		Konto konto = AppContext.getInstance().getApp().addKonto("Grupp", parentKonto);
		
		//MonthDistribution monthDistribution = null;
		//AppContext.getInstance().getApp().addKontoBudget(konto, monthDistribution, year);
		update();
	}
	
	private void addKonto(){
		Row row = tbl.getSelectionModel().getSelectedItem();
		Konto parentKonto = row.getKonto();
		
		RowDialog dlg = new RowDialog(null);
		Optional<MonthDistribution> result = dlg.showAndWait();
		result.ifPresent(monthDistribution -> {
			
			System.out.println("[addKonto] " + monthDistribution);
			
			Konto konto = AppContext.getInstance().getApp().addKonto(monthDistribution.getKontoName(), parentKonto);
			if(konto != null){
				
				AppContext.getInstance().getApp().addKontoBudget(konto, monthDistribution, year);
				update();
			}
		});
	}
	
	private void deleteKonto(){
		Row row = tbl.getSelectionModel().getSelectedItem();
		
		Konto konto = row.getKonto();
		if(konto != null){
			
			Alert alert = buildAlert(konto);
			Optional<ButtonType> result = alert.showAndWait();
			
			if (result.get() == ButtonType.OK){
				System.out.println("[deleteKonto] removing konto: " + konto.getNamn());
				AppContext.getInstance().getApp().deleteKontoBudget(konto, year);
				update();
			} 
			else {
				System.out.println("[deleteKonto] decline delete request for konto: " + row.getKontoNamn());
			}
		}
	}
	
	private Alert buildAlert(Konto konto){
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Vänligen bekräfta");
		alert.setHeaderText("Du har begärt att konto " + konto.getNamn() + " ska tas bort.");
		alert.setContentText("Vill du verkligen ta bort konto " + konto.getNamn() + "?");
		return alert;
	}

	private void initComponents() {
		
		year = AppContext.getInstance().getApp().getCurrYear();
		
		RowDragListener lst = new RowDragListener() {
			@Override
			public void onDragDropped() {
				update();
			}
		};
		
		tbl = new BudgetTable(lst);
		
        tbl.addMenu(buildMenu());
	}

	private void layoutComponents() {
		setPadding(new Insets(10));
		
		setCenter(tbl);
	}
	
	private ContextMenu buildMenu(){
		ContextMenu menu = new ContextMenu();
		
		MenuItem groupAdd = new MenuItem("Ny grupp");
		groupAdd.setOnAction(e -> addGroup());
        menu.getItems().add(groupAdd);
		
        MenuItem itemAdd = new MenuItem("Nytt konto");
        itemAdd.setOnAction(e -> addKonto());
        menu.getItems().add(itemAdd);
        
        MenuItem itemUpdate = new MenuItem("Uppdatera budget");
        itemUpdate.setOnAction(e -> updateBudget());
        menu.getItems().add(itemUpdate);
        
        MenuItem itemDelete = new MenuItem("Ta bort konto");
        itemDelete.setOnAction(e -> deleteKonto());
        menu.getItems().add(itemDelete);
        
        return menu;
	}


}
