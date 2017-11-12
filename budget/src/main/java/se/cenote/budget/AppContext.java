package se.cenote.budget;

public class AppContext {

	private static AppContext INSTANCE = new AppContext();
	
	private BudgetApp app;
	
	private AppContext() {
		app = new BudgetApp();
	}

	public static AppContext getInstance(){
		return INSTANCE;
	}
	
	public BudgetApp getApp(){
		return app;
	}
}
