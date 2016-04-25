package com.exactprosystems.jf.tool.newconfig;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.Configuration.AppEntry;
import com.exactprosystems.jf.common.Configuration.ClientEntry;
import com.exactprosystems.jf.common.Configuration.ServiceEntry;
import com.exactprosystems.jf.common.Configuration.SqlEntry;
import com.exactprosystems.jf.sql.SqlConnection;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.newconfig.nodes.AppTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.ClientTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.EvaluatorTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.FileSystemTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.FormatTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.LibraryTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.MatrixTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.ReportTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.SeparatorTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.ServiceTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.SqlTreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.TreeNode;
import com.exactprosystems.jf.tool.newconfig.nodes.VariablesTreeNode;
import com.exactprosystems.jf.tool.newconfig.testing.TestingConnectionFxController;
import com.exactprosystems.jf.tool.settings.Theme;

public class ConfigurationNewFxController implements Initializable, ContainingParent
{
	private Parent						pane;
	private ConfigurationFxNew 			model;

	private ParametersTableView						tableView;
	private ConfigurationTreeView					treeView;
	private ConfigurationToolBar					menuBar;

	private EvaluatorTreeNode						evaluatorTreeNode;
	private FormatTreeNode							formatTreeNode;
	private MatrixTreeNode							matrixTreeNode;
	private LibraryTreeNode							libTreeNode;
	private VariablesTreeNode						varsTreeNode;
	private SqlTreeNode								sqlTreeNode;
	private ClientTreeNode							clientTreeNode;
	private ServiceTreeNode							serviceTreeNode;
	private AppTreeNode								appTreeNode;
	private FileSystemTreeNode						fileSystemTreeNode;
	private ReportTreeNode							reportTreeNode;
	private TestingConnectionFxController			testSqlController;

	
	@Override
	public void setParent(Parent parent)
	{
		this.pane = parent;
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
	}

	public void init(ConfigurationFxNew configuration)
	{
		this.model = configuration;
		this.tableView = new ParametersTableView();
		this.treeView = new ConfigurationTreeView(this.tableView, this.model);
		this.menuBar = new ConfigurationToolBar(this.model);
		
		initEvaluator();
		initFormat();
		initMatrix();
		initLibrary();
		initVars();
		initReport();
		initSql();
		initClient();
		initService();
		initApp();
		this.treeView.getRoot().getChildren().add(new TreeItem<>(new SeparatorTreeNode()));
		initFileSystem();
	}
	
	


	// ============================================================
	// display* methods
	// ============================================================
	public void displayEvaluator(String imports) // TODO model shouldn't show any messages, so it shouldn't do try-catch
	{
		Common.tryCatch(() -> this.evaluatorTreeNode.display(imports), "Error on display evaluator");
	}

	public void displayFormat(String timeFormat, String dateFormat, String dateTimeFormat, String additionFormats)
	{
		Common.tryCatch(() -> this.formatTreeNode.display(timeFormat, dateFormat, dateTimeFormat, additionFormats),
				"Error on display evaluator");
	}

	public void displayMatrix(List<File> matrixFolders)
	{
		Common.tryCatch(() -> this.matrixTreeNode.display(matrixFolders), "Error on display matrix");
	}

	public void displayLibrary(List<File> libraryFoders)
	{
		Common.tryCatch(() -> this.libTreeNode.display(libraryFoders), "Error on display libs");
	}

	public void displayVars(List<File> varsFiles)
	{
		Common.tryCatch(() -> this.varsTreeNode.display(varsFiles), "Error on display vars");
	}

	public void displaySql(List<SqlEntry> sqlEntries) 
	{
		Common.tryCatch(() -> this.sqlTreeNode.display(sqlEntries), "Error on display sql entries");
	}

	public void displayClient(List<ClientEntry> clientEntries)
	{
		Common.tryCatch(() -> this.clientTreeNode.display(clientEntries, null, null),
//		Common.tryCatch(() -> this.clientTreeNode.display(clientEntries, this.supportedClients, this.listClientDictionaries),
				"Error on display sql entries");
	}

	public void displayService(List<ServiceEntry> serviceEntries)
	{
		Common.tryCatch(() -> this.serviceTreeNode.display(serviceEntries, null, null),
//		Common.tryCatch(() -> this.serviceTreeNode.display(serviceEntries, this.supportedServices, this.startedServices),
				"Error on display sql entries");
	}

	public void displayApp(List<AppEntry> appEntries)
	{
		Common.tryCatch(() -> this.appTreeNode.display(appEntries, null, null), "Error on display sql entries");
//		Common.tryCatch(() -> this.appTreeNode.display(appEntries, this.supportedApps, this.listAppsDictionaries), "Error on display sql entries");
	}

	public void displayReport(File reportFolder)
	{
		Common.tryCatch(() -> this.reportTreeNode.display(reportFolder), "Error on display report folder");
	}

	public void displayFileSystem()
	{
//		List<File> ignoreFiles = new ArrayList<>(this.matrixFolders);
//		ignoreFiles.addAll(this.libraryFoders);
//		ignoreFiles.addAll(this.varsFiles);
//		ignoreFiles.addAll(this.listAppsDictionaries);
//		ignoreFiles.addAll(this.listClientDictionaries);
//		ignoreFiles.add(this.reportFolder);
//		Common.tryCatch(() -> this.fileSystemTreeNode.display(this.initialFile.listFiles(), ignoreFiles), "Error on display sql entries");

	}


	private void initReport()
	{
		TreeItem<TreeNode> reportTreeItem = new TreeItem<>();
		this.reportTreeNode = new ReportTreeNode(this.model, reportTreeItem);
		reportTreeItem.setValue(reportTreeNode);
		this.treeView.getRoot().getChildren().add(reportTreeItem);
	}

	private void initFileSystem()
	{
		TreeItem<TreeNode> fileSystemTreeItem = new TreeItem<>();
		this.fileSystemTreeNode = new FileSystemTreeNode(this.model, this.treeView.getRoot());
		fileSystemTreeItem.setValue(fileSystemTreeNode);
	}

	private void initService()
	{
		TreeItem<TreeNode> serviceTreeItem = new TreeItem<>();
		this.serviceTreeNode = new ServiceTreeNode(this.model, serviceTreeItem);
		serviceTreeItem.setValue(serviceTreeNode);
		this.treeView.getRoot().getChildren().add(serviceTreeItem);
	}

	private void initClient()
	{
		TreeItem<TreeNode> clientTreeItem = new TreeItem<>();
		this.clientTreeNode = new ClientTreeNode(this.model, clientTreeItem);
		clientTreeItem.setValue(clientTreeNode);
		this.treeView.getRoot().getChildren().add(clientTreeItem);
	}

	private void initApp()
	{
		TreeItem<TreeNode> appTreeItem = new TreeItem<>();
		this.appTreeNode = new AppTreeNode(this.model, appTreeItem);
		appTreeItem.setValue(appTreeNode);
		this.treeView.getRoot().getChildren().add(appTreeItem);
	}

	private void initSql()
	{
		TreeItem<TreeNode> sqlTreeItem = new TreeItem<>();
		this.sqlTreeNode = new SqlTreeNode(this.model, sqlTreeItem);
		sqlTreeItem.setValue(sqlTreeNode);
		this.treeView.getRoot().getChildren().add(sqlTreeItem);
	}

	private void initVars()
	{
		TreeItem<TreeNode> varsTreeItem = new TreeItem<>();
		this.varsTreeNode = new VariablesTreeNode(this.model, varsTreeItem);
		varsTreeItem.setValue(this.varsTreeNode);
		this.treeView.getRoot().getChildren().add(varsTreeItem);
	}

	private void initLibrary()
	{
		TreeItem<TreeNode> libraryTreeItem = new TreeItem<>();
		this.libTreeNode = new LibraryTreeNode(this.model, libraryTreeItem);
		libraryTreeItem.setValue(this.libTreeNode);
		this.treeView.getRoot().getChildren().add(libraryTreeItem);
	}

	private void initMatrix()
	{
		TreeItem<TreeNode> matrixTreeItem = new TreeItem<>();
		this.matrixTreeNode = new MatrixTreeNode(this.model, matrixTreeItem);
		matrixTreeItem.setValue(this.matrixTreeNode);
		this.treeView.getRoot().getChildren().add(matrixTreeItem);
	}

	private void initFormat()
	{
		TreeItem<TreeNode> formatTreeItem = new TreeItem<>();
		this.formatTreeNode = new FormatTreeNode(this.model, formatTreeItem);
		formatTreeItem.setValue(this.formatTreeNode);
		this.treeView.getRoot().getChildren().add(formatTreeItem);
	}

	private void initEvaluator()
	{
		TreeItem<TreeNode> evaluatorTreeItem = new TreeItem<>();
		this.evaluatorTreeNode = new EvaluatorTreeNode(this.model, evaluatorTreeItem);
		evaluatorTreeItem.setValue(this.evaluatorTreeNode);
		this.treeView.getRoot().getChildren().add(evaluatorTreeItem);
		this.treeView.getRoot().setExpanded(true);
	}

	
	private BorderPane createMockView(ConfigurationFxNew config)
	{
		SplitPane splitPane = new SplitPane();

		VBox box = new VBox();
		Button undo = new Button("Undo");
		undo.setOnAction(e -> config.undo());
		Button redo = new Button("Redo");
		redo.setOnAction(e -> config.redo());
		box.getChildren().addAll(undo, redo);
		box.setSpacing(10);
		TabPane tabPane = new TabPane();
		tabPane.getTabs().addAll(new Tab("Tab 1"), new Tab("tab 2"));
		box.getChildren().add(tabPane);
		GridPane gridPane = new GridPane();
		gridPane.setMinWidth(20.0);
		ColumnConstraints c0 = new ColumnConstraints();
		c0.setHgrow(Priority.SOMETIMES);
		c0.setMaxWidth(30.0);
		c0.setMinWidth(30.0);
		c0.setPrefWidth(30.0);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setMinWidth(10.0);
		c1.setPrefWidth(100.0);
		c1.setHgrow(Priority.SOMETIMES);
		RowConstraints r0 = new RowConstraints();
		r0.setMinHeight(10.0);
		r0.setPrefHeight(30);
		r0.setVgrow(Priority.SOMETIMES);
		gridPane.getRowConstraints().add(r0);
		gridPane.getColumnConstraints().addAll(c0, c1);

		Label project = new Label("Project");
		project.setOnMouseClicked(event ->
		{
			double position = splitPane.getDividerPositions()[0];
			if (position < 0.1)
			{
				splitPane.setDividerPositions(0.5);
			}
			else
			{
				splitPane.setDividerPositions(0.0);
			}
		});
		gridPane.add(project, 0, 0);
		gridPane.setGridLinesVisible(true);
		project.setRotate(-90.0);
		GridPane.setValignment(project, VPos.TOP);
		GridPane.setMargin(project, new Insets(20, 0, 0, -15.0));

		splitPane.getItems().addAll(gridPane, box);
		BorderPane treePane = new BorderPane();
		treePane.setMinWidth(0.0);
		gridPane.add(treePane, 1, 0);
		return treePane;
	}

}
