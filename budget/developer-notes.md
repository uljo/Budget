Developer notes
=================
Simple JavaFX application for displaying and manipulating budget month values for a year.

Budget data consists of different accounts of two main types: Inbound or Outbound.
 
Accounts can be ordered in groups, in an hierarchical tree structure with a parent node containing children nodes.

Each account has a month budget associated with it.

Functionality:

1. Add accounts

2. Remove accounts

3. Update accounts

4. Show and hide accounts belonging to a group (as an expandable tree-table)

5. Persist data on file


#ToDo
1) Popup-menu in table should be context dependent:

	a) Empty-row 	=> No menu
	b) Parent-row 	=> { Add Account, Delete }
	c) Leaf-row 	=> { Edit, Delete }

2) Make it possible to reorder table rows by drag-n-drop. 

3) Show percentages on Pie chart

4) Make it possible to change years

5) Internationalization



#Persistens
Data is persisted on file <code>{project}/src/test/resources/budget.ser</code> on following format:

Account-part:

	accountId$accountType$accountName

Budget-part:

	accountId$year$budgetType$monthNumber$amount

Example:

	[konto]
	1$IN$Intäkter
	1.1$IN$Lön Li
	1.2$IN$Lön uffe
	
	2$UT$Utgifter
	2.1$UT$Bostad Uppsala
	2.1.1$UT$Ränta lån
	
	[budget]
	1.1$2017$0$0$5000
	1.2$2017$0$0$33800

#AccountId
Stored with . as separators, but in application with '-'.

	1.2.3	1-2-3

#BudgetTypes
	0. Every month
	1. Single month
	2. Every other month
	3. Third month
	4. Fourth month
	6. Sixth month

#MonthNumber

	0 	= N/A

	1-12	= Jan-Dec

#Amount
Stored as number without decimals or separators.
	
