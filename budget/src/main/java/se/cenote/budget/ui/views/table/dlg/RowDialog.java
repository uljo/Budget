package se.cenote.budget.ui.views.table.dlg;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar.ButtonData;
import se.cenote.budget.model.budget.MonthDistribution;

public class RowDialog extends Dialog<MonthDistribution>{
	
	private KontoBudgetPanel contentPanel;
	
	public RowDialog(MonthDistribution monthDistribution){
		
		setTitle("Konto Dialog");
		setHeaderText("Dialog för uppdatering av konto.");
		
		ButtonType saveButtonType = new ButtonType("Spara", ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
		
		// Enable/Disable login button depending on whether data has changed.
		Node saveButton = getDialogPane().lookupButton(saveButtonType);
		saveButton.setDisable(true);
	
		contentPanel = new KontoBudgetPanel(monthDistribution);
		
		// Do some validation (using the Java 8 lambda syntax).
		contentPanel.amountFld.textProperty().addListener((observable, oldValue, newValue) -> {
			saveButton.setDisable(newValue.trim().isEmpty());
		});
		
		contentPanel.periodCmb.valueProperty().addListener( (obs, nv, ov) -> {
			System.out.println("[hepp]");
			saveButton.setDisable(!contentPanel.isDirty());
		});
		
		contentPanel.monthCmb.valueProperty().addListener( (obs, nv, ov) -> {
			System.out.println("[happ]");
			saveButton.setDisable(!contentPanel.isDirty());
		});
		
	
		getDialogPane().setContent(contentPanel);
	
		// Request focus on the username field by default.
		Platform.runLater(() -> contentPanel.nameFld.requestFocus());
	
		// Convert the result to MonthDistribution instance when the save button is clicked.
		setResultConverter(dialogButton -> {
		    if (dialogButton == saveButtonType) {
		        return contentPanel.getMonthDistribution();
		    }
		    return null;
		});
	
	}
}