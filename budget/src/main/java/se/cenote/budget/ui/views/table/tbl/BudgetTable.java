package se.cenote.budget.ui.views.table.tbl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import se.cenote.budget.AppContext;
import se.cenote.budget.model.konto.Konto;

public class BudgetTable extends TableView<Row>{
	
	private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
	
	private RowDragListener listener;
	
	private ObservableList<Row> rowItems;
	
	private List<Row> allRows;
	private Map<Konto, Boolean> displayMap;
	
	public BudgetTable(RowDragListener listener) {
		
		this.listener = listener;
		
		rowItems = FXCollections.observableArrayList();
		setItems(rowItems);
		
		getColumns().addAll(buildStringColumns());
        getColumns().addAll(buildNumberColumns());
        
        setRowFactory(tv -> {
            TableRow<Row> tblRow = new TableRow<>();

            tblRow.setOnDragDetected(event -> {
                if (! tblRow.isEmpty()) {
                    Integer index = tblRow.getIndex();
                    Dragboard db = tblRow.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(tblRow.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.put(SERIALIZED_MIME_TYPE, index);
                    db.setContent(cc);
                    event.consume();
                }
            });

            tblRow.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                	int draggedIndex = (Integer)db.getContent(SERIALIZED_MIME_TYPE);
                    if (tblRow.getIndex() != draggedIndex) {
                    	Row draggedRow = getItems().get(draggedIndex);
                    	if(!tblRow.isEmpty()){
	                    	Row currRow = getItems().get(tblRow.getIndex());
	                    	if(currRow.isMoveAccepted(draggedRow)){
	                    		event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
	                        	event.consume();
	                    	}
                    	}
                    }
                }
            });

            tblRow.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    Row draggedRow = getItems().get(draggedIndex);
                    Konto draggedKonto = draggedRow.getKonto();

                    boolean updateTbl = false;
                    
                    if(!tblRow.isEmpty()){
                    	Row currRow = getItems().get(tblRow.getIndex());
	                    Konto currKonto = currRow.getKonto();
	                    
	                    System.out.println("[setOnDragDropped] Moving '" + draggedKonto.getNamn() + "' on top of '" + currKonto.getNamn() + "'.") ;
	                    
	                    if(currKonto != null && draggedKonto != null){
	                    	Konto parent = currKonto.getParent();
	                    	Konto draggedParent = draggedKonto.getParent();
	                    	if(!parent.equals(draggedParent)){
	                    		
	                    		AppContext.getInstance().getApp().moveChild(parent, draggedKonto);
	                    		updateTbl = true;
	                    		System.out.println("[setOnDragDropped] Moved " + draggedKonto + " to new parent: " + parent);
	                    	}
	                    	else{
	                    		System.out.println("[setOnDragDropped] Moved place between siblings." );
	                    	}
	                    }
                    }
                    
                    int dropIndex = tblRow.isEmpty() ?  getItems().size() : tblRow.getIndex();

                    getItems().remove(draggedIndex);
                    getItems().add(dropIndex, draggedRow);

                    event.setDropCompleted(true);
                    getSelectionModel().select(dropIndex);
                    event.consume();
                    
                    if(updateTbl && this.listener != null)
                    	this.listener.onDragDropped();
                    	//update();
                }
            });

            return tblRow ;
        });
	}
	
	public void update(List<Row> rows){

		allRows = rows;
		
		displayMap = new HashMap<>();
		for(int i = 0; i < allRows.size(); i++){ // first 4 rows are always visible!
			Row row = allRows.get(i);
			if(displayMap.containsKey(row.getKonto())){
				System.err.println("[update] Duplicate key in displayMap for row: " + row.getKonto());
			}
			displayMap.put(row.getKonto(), row.isChildrenVisible());
		}
		
		//displayMap = allRows.stream().collect(Collectors.toMap(o -> o.getKonto(), o -> o.isChildrenVisible()));
		
		Platform.runLater(() -> {
			rowItems.clear();
			rowItems.addAll(rows);
		});
	}
	
	public void addMenu(ContextMenu menu){
		
        addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getButton() == MouseButton.SECONDARY){
                	Row row = getSelectionModel().getSelectedItem();
                	int index = getSelectionModel().getSelectedIndex();
                	if(index > 3 && row != null){
                		menu.show(BudgetTable.this , event.getScreenX() , event.getScreenY());
                	}
                }
                else if(event.getClickCount() >= 2){
                	Row row = getSelectionModel().getSelectedItem();
                	if(row != null){
                		System.out.println("[handle] Caught double-click for row: " + row.getKontoNamn());
                		contractRow();
                	}
                }
            }
        });
	}
	
	private void contractRow(){
		
		Row targetRow = getSelectionModel().getSelectedItem();
		
		Konto konto = targetRow.getKonto();
		if(konto != null && !konto.isLeaf()){
			
			updateRowVisibility(targetRow);
			
			List<Row> list = getVisibleRows();

			Platform.runLater(() -> {
				rowItems.clear();
				rowItems.addAll(list);
			});
		}
	}

	private void updateRowVisibility(Row row){
		
		boolean childrenVisible = !row.isChildrenVisible();
		
		List<Konto> decendents = row.getKonto().getDecendents();
		
		for(Konto decendent : decendents){
			displayMap.put(decendent, childrenVisible);
		}
		row.setChildrenVisible(childrenVisible);
	}
	
	private List<Row> getVisibleRows(){
		List<Row> list = new ArrayList<>();
		for(int i = 0; i < allRows.size(); i++ /*Row row : allRows*/){
			Row row = allRows.get(i);
			boolean visible = displayMap.get(row.getKonto());
			if(visible){
				list.add(row);
			}
		}
		return list;
	}
	
	private List<TableColumn<Row, String>> buildStringColumns(){
		List<TableColumn<Row, String>> list = new ArrayList<>();
		
		TableColumn<Row, String> kontoCol = new TableColumn<>("Konto");
		kontoCol.setCellValueFactory(new PropertyValueFactory<Row, String>("kontoNamn"));
		kontoCol.setCellFactory(c -> new KontoCell());
		kontoCol.setSortable(false);
		list.add(kontoCol);
		
		return list;
	}

	private List<TableColumn<Row, Double>> buildNumberColumns(){

		String[] colNames = {"Jan", "Feb", "Mar", "Apr", "Maj", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dec", "Total", "Snitt"};
	
		List<TableColumn<Row, Double>> list = new ArrayList<>();
		for(int i = 0; i < colNames.length; i++){
			TableColumn<Row, Double> col = new TableColumn<>(colNames[i]);
			col.setSortable(false);
			
			String propName = null;
			if(i < 12)
				propName = "amount" + (i+1);
			else if(i == 12)
				propName = "sum";
			else if(i == 13)
				propName = "avg";
			
			col.setCellValueFactory(new PropertyValueFactory<Row, Double>(propName));
			col.setCellFactory(c -> new NumberCell());
			list.add(col);
		}
		return list;
	}
	
	public interface RowDragListener{
		public void onDragDropped();
	}
	
	public class KontoCell extends TableCell<Row, String>{
		
		public KontoCell(){
			//setStyle("-fx-alignment: CENTER-LEFT;");
		}
		
		@Override 
	    public void updateItem(String kontoNamn, boolean empty) {
	        super.updateItem(kontoNamn, empty);
	        
	        if (empty || kontoNamn == null) {
	            setText(null);
	        } 
	        else{
	            setText(kontoNamn);
	        }
	    }
	}

	/**
	 * 
	 * @author uffe
	 *
	 */
	public class NumberCell extends TableCell<Row, Double>{
		
		public NumberCell(){
			setStyle("-fx-alignment: CENTER-RIGHT;");
		}
		
		@Override 
	    public void updateItem(Double amount, boolean empty) {
	        super.updateItem(amount, empty);
	        
	        if (empty || amount == null) {
	            setText(null);
	        } 
	        else if(amount < 0){
	            setText("");
	        }
	        else if(amount >= 0){
	            setText(String.format("%,.0f", amount.doubleValue()));
	        }
	    }
	}

}
