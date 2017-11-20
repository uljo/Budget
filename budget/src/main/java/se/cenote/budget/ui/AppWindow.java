package se.cenote.budget.ui;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import se.cenote.budget.AppContext;
import se.cenote.budget.ui.views.chart.ChartView;
import se.cenote.budget.ui.views.table.BudgetView;

public class AppWindow extends Application{
	
	private String title = "Budget";
	private int width = 1000;
	private int height = 600;

	
	private StackPane stack;
	private GlassPane glassPane;
	
	public AppWindow() {}
	
	public static void show(){
		AppWindow.launch((String[])null);
	}

	@Override
	public void start(Stage stage) throws Exception {
		
		Parent parent = new MainFrame();
		
		stack = new StackPane();
		stack.getChildren().add(parent);
		
		Scene scene = new Scene(stack, width, height);
		scene.getStylesheets().add(getCssPath("budget.css"));

		stage.setScene(scene);
		
		stage.setTitle(title);
		
		showGlass();
		stage.show();
	}
	
	private void showGlass(){
		if(glassPane == null){
			glassPane = new GlassPane();
			glassPane.setOnMouseClicked( e -> removeGlass());
		}
		stack.getChildren().add(glassPane);
	}
	
	private void removeGlass() {
		FadeTransition transition = new FadeTransition();
		transition.setNode(glassPane);
		transition.setDuration(new Duration(1000));
		transition.setFromValue(1.0);
		transition.setToValue(0.0);
		transition.setOnFinished(e -> {
	    	glassPane.stopAnimation();
	    	stack.getChildren().remove(1); 
	    	glassPane = null;
	    	transition.stop();
	    });
	    
		transition.play();
	}

	@Override
	public void stop() throws Exception {
		AppContext.getInstance().getApp().stop();
		super.stop();
	}

	private String getCssPath(String cssFileName){
		ClassLoader classLoader = getClass().getClassLoader();
		File cssFile = new File(classLoader.getResource(cssFileName).getFile());
		String cssPath = cssFile.toURI().toString();
		System.out.println("[getCssPath] cssPath: " + cssPath);
		return cssPath;
	}
	
	/**
	 * 
	 * @author uffe
	 *
	 */
	class MainFrame extends BorderPane{

		private StackPane stack;
		
		private Button barsBtn;
		private Button tblBtn;
		private Button chartBtn;
		
		private BudgetView tableView;
		private ChartView chartView;
		
		private Label fileLbl;
		
		private GlyphFont fontAwesome;
		
		public MainFrame(){
			initComponents();
			layoutComponents();
			
			showTableView();
		}
		
		public void showChartView(){
			
			Platform.runLater(() -> {
				
				chartView.update();
				
				stack.getChildren().clear();
				stack.getChildren().add(chartView);

				tblBtn.setDisable(false);
				chartBtn.setDisable(true);
				System.out.println("[showChartView] Entered");
			});
		}
		
		public void showTableView(){
		
			Platform.runLater(() -> {
				
				tableView.update();
				
				stack.getChildren().clear();
				stack.getChildren().add(tableView);
				
				tblBtn.setDisable(true);
				chartBtn.setDisable(false);
				System.out.println("[showTableView] Entered");
			});
		}

		private void initComponents() {
			
			fontAwesome = GlyphFontRegistry.font("FontAwesome");
			
			stack = new StackPane();
			
			chartView = new ChartView();
			tableView = new BudgetView();
			
			barsBtn = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.BARS));
			barsBtn.setOnAction( e -> showGlass());
			
			Glyph tblGlyph = new Glyph("FontAwesome", FontAwesome.Glyph.TABLE);
			tblBtn = new Button("Table", tblGlyph);
			tblBtn.setOnAction( e -> showTableView());
			tblBtn.setDisable(true);
			
			chartBtn = new Button("Chart", new Glyph("FontAwesome", FontAwesome.Glyph.BAR_CHART));
			chartBtn.setOnAction( e -> showChartView());
			
			String storeName = AppContext.getInstance().getApp().getStoreFile();
			LocalDateTime time = AppContext.getInstance().getApp().getStoreTime(); 
			
			fileLbl = new Label("Store: " + storeName + " (senast sparad: " + time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ")");
		}

		private void layoutComponents() {
			setPadding(new Insets(10));
			
			Pane topInnerPane = buildTopPane();
			
			VBox topPane = new VBox();
			topPane.setSpacing(4);
			topPane.setPadding(new Insets(0, 5, 5, 5));
			topPane.getChildren().addAll(topInnerPane, new Separator());
			
			setTop(topPane);
			
			// Center pane
			setCenter(stack);
			
			// Bottom pane
			FlowPane infoPane = new FlowPane();
			infoPane.setHgap(6);
			
			//Glyph glyph = new Glyph("FontAwesome", FontAwesome.Glyph.FILE_TEXT_ALT);
			Glyph fileGlyph = fontAwesome.create(FontAwesome.Glyph.FILE_TEXT_ALT).size(18);
			
			infoPane.getChildren().addAll(fileGlyph, fileLbl);
			
			VBox bottomPane = new VBox();
			bottomPane.setSpacing(4);
			bottomPane.setPadding(new Insets(5, 5, 0, 5));
			bottomPane.getChildren().addAll(new Separator(), infoPane);
			setBottom(bottomPane);
		}
		
		private Pane buildTopPane(){
			
	        HBox topLeftBtnPane = new HBox();
	        topLeftBtnPane.setPrefWidth(100);
	        topLeftBtnPane.setSpacing(10);
	        topLeftBtnPane.getChildren().addAll(barsBtn);
	        
	        Text titleLbl = new Text("Årsbudget " + LocalDate.now().getYear());
	        titleLbl.setFont(Font.font(24));

	        HBox topBtnPane = new HBox();
	        topBtnPane.setPrefWidth(100);
	        topBtnPane.setSpacing(10);
	        topBtnPane.getChildren().addAll(tblBtn, chartBtn);

	        Region region1 = new Region();
	        HBox.setHgrow(region1, Priority.ALWAYS);

	        Region region2 = new Region();
	        HBox.setHgrow(region2, Priority.ALWAYS);

	        HBox hBox = new HBox(topLeftBtnPane, region1, titleLbl, region2, topBtnPane);
	        return hBox;
		}
	}
	
	class GlassPane extends BorderPane{
		
		private ImageView imageView;
		
		private AnimationTimer timer;
		private Random rand;
		private List<GlassNode> nodes;
		
		private Canvas canvas;
		
		public GlassPane(){
			initComponents();
			layoutComponents();
		}
		
		public void stopAnimation(){
			timer.stop();
		}
		
		private void initComponents(){

			imageView = new ImageView();
			
			rand = new Random();
			int index = rand.nextInt(3);
			String[] arr = {"images/logo.jpg", "images/budget-blue.jpg", "images/budgetdesign.jpg"};
			Image image = getImage(arr[index]);
			imageView.setImage(image);
			
			canvas = new Canvas(image.getWidth(), image.getHeight());
			nodes = new ArrayList<>();
			nodes.add(new GlassNode( 10, -10, 10, 1));
			
			timer = new GlassTimer();
	        timer.start();
		}
		
		private void layoutComponents() {
			
			StackPane stack = new StackPane();
			stack.getChildren().addAll(imageView, canvas);
			setCenter(stack);
			
			setStyle("-fx-background-color: white;");
		}
		
		private void update(){
			
			if(getWidth() > 0){
			
				GraphicsContext g = canvas.getGraphicsContext2D();
				
				g.clearRect(0, 0, getWidth(), getHeight());
				
				if(rand.nextBoolean()){
					for(int i = 0, num = rand.nextInt(10); i < num; i++){
						double x = rand.nextInt((int)getWidth());
						double size = 10 + rand.nextInt(8);
						double age = rand.nextInt(90);
						nodes.add(new GlassNode(x, -size, size, age));
					}
				}
				
				for(Iterator<GlassNode> itr = nodes.iterator(); itr.hasNext(); ){
					GlassNode node = itr.next();
					if(node.y > getHeight())
						itr.remove();
					else{
						node.y += 1 + (node.y/getWidth() * 10);
						
						double alpha = node.getAge()/90;
						g.setGlobalAlpha(alpha);
						g.setFill(Color.ORANGE);
						g.fillOval(node.x, node.y, node.size, node.size);
					}
				}
			}
		}
		
		private Image getImage(String path){
			Image image = null;
			try{
				ClassLoader classLoader = getClass().getClassLoader();
				URL url = classLoader.getResource(path);
				if(url != null){
					image = new Image(url.toURI().toString());
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return image;
		}
		
		class GlassNode{
			
			private int MAX_AGE = 90;
			
			double x;
			double y;
			double size;
			private double age;
			private boolean up;
			
			public GlassNode(double x, double y, double size, double age) {
				this.x = x;
				this.y = y;
				this.size = size;
				this.age = age;
			}
			
			public double getAge(){
				if(age >= MAX_AGE)
					up = false;
				else if(age <= 0)
					up = true;
				
				age += 1 * (up ? 1 : -1);
				return age;
			}
		}
		
		class GlassTimer extends AnimationTimer{
			
			private long limit = 100_000L;
			private long prevTime;

			@Override
			public void handle(long now) {
				
				long diff = now-prevTime;
				if(diff > limit){
					
					prevTime = now;
					
					update();
				}
			}
			
		}
		
	}

}
