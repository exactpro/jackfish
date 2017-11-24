////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.newconfig;

import com.exactprosystems.jf.api.service.ServiceStatus;
import com.exactprosystems.jf.common.MutableString;
import com.exactprosystems.jf.documents.config.*;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.skin.CustomTreeViewSkin;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.nodes.*;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigurationFxController implements Initializable, ContainingParent
{
	private ConfigurationFx					model;

	private ParametersTableView				tableView;
	private ConfigurationTreeView			treeView;
	private ConfigurationToolBar			menuBar;

	private EvaluatorTreeNode				evaluatorTreeNode;
	private FormatTreeNode					formatTreeNode;
	private MatrixTreeNode					matrixTreeNode;
	private LibraryTreeNode					libTreeNode;
	private VariablesTreeNode				varsTreeNode;
	private SqlTreeNode						sqlTreeNode;
	private GlobalHandlerNode				globalHandlerNode;
	private ClientTreeNode					clientTreeNode;
	private ServiceTreeNode					serviceTreeNode;
	private AppTreeNode						appTreeNode;
	private FileSystemTreeNode				fileSystemTreeNode;
	private ReportTreeNode					reportTreeNode;
	
	@Override
	public void setParent(Parent parent)
	{
		// don't use this. all content is built by code
	}

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
	}

	public void init(ConfigurationFx configuration, BorderPane pane, CompareEnum currentCompareEnum)
	{
		this.model 		= configuration;
		this.tableView 	= new ParametersTableView();
		this.treeView 	= new ConfigurationTreeView(this.tableView, this.model);
		this.menuBar 	= new ConfigurationToolBar(this.model, currentCompareEnum);

		pane.setTop(this.menuBar);

		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.getItems().addAll(this.treeView, this.tableView);
		splitPane.setDividerPositions(0.8);
		pane.setCenter(splitPane);

		initTreeView();
	}

	// ============================================================
	// display* methods
	// ============================================================
	public void displayEvaluator(List<String> imports) 
	{
		Common.tryCatch(() -> this.evaluatorTreeNode.display(imports), "Error on display evaluator");
	}

	public void displayFormat(String timeFormat, String dateFormat, String dateTimeFormat, List<String> additionFormats)
	{
		Common.tryCatch(() -> this.formatTreeNode.display(timeFormat, dateFormat, dateTimeFormat, additionFormats),"Error on display evaluator");
	}

	public void displayMatrix(List<String> matricesValue)
	{
		Common.tryCatch(() -> this.matrixTreeNode.display(matricesValue), "Error on display matrix");
	}

	public void displayLibrary(Map<String, Matrix> map)
	{
		Common.tryCatch(() -> this.libTreeNode.display(map), "Error on display libs");
	}

	public void displayVars(List<String> userVarsValue)
	{
		Common.tryCatch(() -> this.varsTreeNode.display(userVarsValue), "Error on display vars");
	}

	public void displaySql(List<SqlEntry> sqlEntries) 
	{
		Common.tryCatch(() -> this.sqlTreeNode.display(sqlEntries), "Error on display sql entries");
	}

	public void displayGlobalHandler(Map<HandlerKind, String> map)
	{
		Common.tryCatch(() -> this.globalHandlerNode.display(map), "Error on display sql entries");
	}

	public void displayClient(List<ClientEntry> clientEntries)
	{
		Common.tryCatch(() -> this.clientTreeNode.display(clientEntries,
				this.model.getClientDictionariesValue()
						.stream()
						.map(MutableString::get)
						.map(File::new)
						.collect(Collectors.toList())
		),"Error on display client entries");
	}

	public void displayService(List<ServiceEntry> serviceEntries, Map<String, ServiceStatus> statusMap)
	{
		Common.tryCatch(() -> this.serviceTreeNode.display(serviceEntries, statusMap),"Error on display service entries");
	}

	public void displayApp(List<AppEntry> appEntries)
	{
		Common.tryCatch(() -> this.appTreeNode.display(appEntries,
				this.model.getAppDictionariesValue()
							.stream()
							.map(MutableString::get)
							.map(File::new)
							.collect(Collectors.toList())
		), "Error on display apps entries");
	}

	public void displayReport(String reportsValue)
	{
		Common.tryCatch(() -> this.reportTreeNode.display(reportsValue), "Error on display report folder");
	}

	public void displayFileSystem(List<String> ignoreFiles)
	{
		Common.tryCatch(() -> this.fileSystemTreeNode.display(new File(".").listFiles(), ignoreFiles), "Error on display sql entries");
	}

	public void successfulSave()
	{
		DialogsHelper.showSuccess("Config successfully saved");
	}

	public void scrollToFile(File file)
	{
		byPass(this.treeView.getRoot(), file);
	}

	private void byPass(TreeItem<TreeNode> root, File file)
	{
		TreeNode value = root.getValue();
		if (value instanceof FileTreeNode)
		{
			File rootFile = ((FileTreeNode) value).getFile();

			if (ConfigurationFx.path(file).equals(ConfigurationFx.path(rootFile)))
			{
				((CustomTreeViewSkin<TreeNode>) this.treeView.getSkin()).scrollAndSelect(root);
				return;
			}
		}
		root.getChildren().forEach(item -> byPass(item, file));
	}

	private void initTreeView()
	{
		TreeItem<TreeNode> evaluatorTreeItem = new TreeItem<>();
		this.evaluatorTreeNode = new EvaluatorTreeNode(this.model, evaluatorTreeItem);
		evaluatorTreeItem.setValue(evaluatorTreeNode);
		
		TreeItem<TreeNode> formatTreeItem = new TreeItem<>();
		this.formatTreeNode = new FormatTreeNode(this.model, formatTreeItem);
		formatTreeItem.setValue(this.formatTreeNode);
		
		TreeItem<TreeNode> matrixTreeItem = new TreeItem<>();
		this.matrixTreeNode = new MatrixTreeNode(this.model, matrixTreeItem);
		matrixTreeItem.setValue(this.matrixTreeNode);

		TreeItem<TreeNode> libraryTreeItem = new TreeItem<>();
		this.libTreeNode = new LibraryTreeNode(this.model, libraryTreeItem);
		libraryTreeItem.setValue(this.libTreeNode);

		TreeItem<TreeNode> varsTreeItem = new TreeItem<>();
		this.varsTreeNode = new VariablesTreeNode(this.model, varsTreeItem);
		varsTreeItem.setValue(this.varsTreeNode);

		TreeItem<TreeNode> reportTreeItem = new TreeItem<>();
		this.reportTreeNode = new ReportTreeNode(this.model, reportTreeItem);
		reportTreeItem.setValue(reportTreeNode);

		TreeItem<TreeNode> globalHandlerTreeItem = new TreeItem<>();
		this.globalHandlerNode = new GlobalHandlerNode(model, globalHandlerTreeItem);
		globalHandlerTreeItem.setValue(this.globalHandlerNode);

		TreeItem<TreeNode> sqlTreeItem = new TreeItem<>();
		this.sqlTreeNode = new SqlTreeNode(this.model, sqlTreeItem);
		sqlTreeItem.setValue(sqlTreeNode);

		TreeItem<TreeNode> clientTreeItem = new TreeItem<>();
		this.clientTreeNode = new ClientTreeNode(this.model, clientTreeItem);
		clientTreeItem.setValue(clientTreeNode);

		TreeItem<TreeNode> serviceTreeItem = new TreeItem<>();
		this.serviceTreeNode = new ServiceTreeNode(this.model, serviceTreeItem);
		serviceTreeItem.setValue(serviceTreeNode);

		TreeItem<TreeNode> appTreeItem = new TreeItem<>();
		this.appTreeNode = new AppTreeNode(this.model, appTreeItem);
		appTreeItem.setValue(appTreeNode);

		TreeItem<TreeNode> separatorTreeItem = new TreeItem<>(new SeparatorTreeNode());
		
		TreeItem<TreeNode> fileSystemTreeItem = new TreeItem<>();
		this.fileSystemTreeNode = new FileSystemTreeNode(this.model, this.treeView.getRoot());
		fileSystemTreeItem.setValue(fileSystemTreeNode);

		this.treeView.getRoot().getChildren().addAll(
				Arrays.asList(
					evaluatorTreeItem, 
					formatTreeItem,
					matrixTreeItem,
					libraryTreeItem,
					varsTreeItem,
					reportTreeItem,
					globalHandlerTreeItem,
					sqlTreeItem,
					clientTreeItem,
					serviceTreeItem,
					appTreeItem,
					separatorTreeItem,
					fileSystemTreeItem)
				);
		
		this.treeView.getRoot().setExpanded(true);
	}

	public void updateParameters()
	{
		List<TablePair> parameters = this.treeView.getSelectionModel().getSelectedItem().getValue().getParameters();
		tableView.setItems(FXCollections.observableArrayList(Optional.ofNullable(parameters).orElse(Collections.emptyList())));
	}
}
