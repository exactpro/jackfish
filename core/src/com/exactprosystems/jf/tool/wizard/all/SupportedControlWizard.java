package com.exactprosystems.jf.tool.wizard.all;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.common.IContext;
import com.exactprosystems.jf.api.wizard.WizardAttribute;
import com.exactprosystems.jf.api.wizard.WizardCategory;
import com.exactprosystems.jf.api.wizard.WizardCommand;
import com.exactprosystems.jf.api.wizard.WizardManager;
import com.exactprosystems.jf.documents.guidic.GuiDictionary;
import com.exactprosystems.jf.tool.wizard.AbstractWizard;
import com.exactprosystems.jf.tool.wizard.CommandBuilder;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.function.Supplier;

@WizardAttribute(
		name            	= "Support control wizard",
		pictureName         = "GherkinWizard.jpg",
		category            = WizardCategory.GUI_DICTIONARY,
		shortDescription    = "This wizard check dictionary on supported control",
		detailedDescription = "This wizard check dictionary on supported control",
		experimental 		= false,
		strongCriteries     = false,
		criteries           = { GuiDictionary.class, IApplicationFactory.class }
)
public class SupportedControlWizard extends AbstractWizard
{
	private IApplicationFactory appFactory;
	private GuiDictionary       dictionary;

	@Override
	public void init(IContext context, WizardManager wizardManager, Object... parameters)
	{
		super.init(context, wizardManager, parameters);

		this.appFactory = super.get(IApplicationFactory.class, parameters);
		this.dictionary = super.get(GuiDictionary.class, parameters);
	}

	@Override
	public boolean beforeRun()
	{
		//TODO add check
		return true;
	}

	@Override
	protected void initDialog(BorderPane borderPane)
	{
		ControlKind[] supportedValues = this.appFactory.supportedControlKinds();
		borderPane.setPrefSize(600.0, 600.0);
		TreeView<SimpleBean> treeView = createTreeView(supportedValues);

		borderPane.setCenter(treeView);
	}

	@Override
	protected Supplier<List<WizardCommand>> getCommands()
	{
		return () ->
				CommandBuilder.start()
				.build();
	}

	private TreeView<SimpleBean> createTreeView(ControlKind[] supportedValues)
	{
		TreeView<SimpleBean> treeView = new TreeView<>();
		treeView.setCellFactory(param -> new TreeCell<SimpleBean>(){
			@Override
			protected void updateItem(SimpleBean item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					HBox box = new HBox();
					box.setSpacing(4);
					box.getChildren().add(new Label(item.name));
					if (item.value != null)
					{
						ComboBox<ControlKind> comboBox = new ComboBox<>();
						comboBox.getItems().setAll(supportedValues);
						comboBox.getSelectionModel().select(ControlKind.Any);
						comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
							if (newValue != null)
							{
								item.value = newValue;
							}
						});
					}
					setGraphic(box);
				}
				else
				{
					setGraphic(null);
				}
			}
		});

		TreeItem<SimpleBean> root = new TreeItem<>(new SimpleBean(this.dictionary.getName()));
		treeView.setRoot(root);

		return treeView;
	}

	private class SimpleBean
	{
		String name;
		ControlKind value;

		public SimpleBean(String name)
		{
			this.name = name;
		}

		public SimpleBean(String name, ControlKind value)
		{
			this.name = name;
			this.value = value;
		}
	}
}
