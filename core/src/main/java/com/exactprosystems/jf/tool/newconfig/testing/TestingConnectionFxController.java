/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.tool.newconfig.testing;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.sql.SqlConnection;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.newconfig.ConfigurationFx;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.*;

public class TestingConnectionFxController implements Initializable, ContainingParent
{
	private static final Logger logger = Logger.getLogger(TestingConnectionFxController.class);

	public TextField tfServer;
	public TextField tfDatabaseName;
	public TextField tfUser;
	public PasswordField pfPassword;
	public Label lblTest;
	public Label lblServer;

	private Parent parent;
	private ConfigurationFx model;
	private String name;
	private Map<String, TextField> map = new HashMap<>();

	public static final String DATABASE_NAME	= "DatabaseName";
	public static final String USER				= "User";
	public static final String SERVER_NAME		= "Server";

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert pfPassword != null : "fx:id=\"pfPassword\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		assert tfDatabaseName != null : "fx:id=\"tfDatabaseName\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		assert tfUser != null : "fx:id=\"tfUser\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		assert lblTest != null : "fx:id=\"lblTest\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		assert tfServer != null : "fx:id=\"tfServer\" was not injected: check your FXML file 'TestingConnectionFx.fxml'.";
		map.put(DATABASE_NAME, tfDatabaseName);
		map.put(USER, tfUser);
		map.put(SERVER_NAME, tfServer);
	}

	@Override
	public void setParent(Parent parent)
	{
		this.parent = parent;
	}

	//============================================================
	// events methods
	//============================================================
	public void testConnection(ActionEvent actionEvent)
	{
		Common.tryCatch(() -> testSqlConnection(this.name, tfServer.getText(), tfDatabaseName.getText(), tfUser.getText(), pfPassword.getText()), "Error on test connection");
	}

	public void init(ConfigurationFx model, String name, List<Settings.SettingsValue> values)
	{
		this.model = model;
		this.name = name;
		tfServer.setText("127.0.0.1");
		this.lblServer.setText(this.name);
		values.forEach(value -> Optional.ofNullable(map.get(value.getKey())).ifPresent(tf -> tf.setText(value.getValue())));
	}

	public void display()
	{
		Dialog dialog = new Alert(Alert.AlertType.INFORMATION);
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setContent(this.parent);
		dialog.setTitle(String.format(R.TESTING_CONNECTION_FX_CONTR_TEST.get(), this.name));
		dialog.setHeaderText(null);
		dialog.setGraphic(new Label());
		dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.show();
	}

	public void displayConnectionGood()
	{
		lblTest.getStyleClass().addAll(CssVariables.SQL_CONNECTION_VALID);
		lblTest.setText(R.TESTING_CONNECTION_FX_CONTR_CONNECTED.get());
	}

	public void displayConnectionBad(String message)
	{
		lblTest.getStyleClass().addAll(CssVariables.SQL_CONNECTION_INVALID);
		lblTest.setText(R.TESTING_CONNECTION_FX_CONTR_FAILED.get());
		Optional.ofNullable(message).ifPresent(msg -> 
		{
			if (lblTest.getTooltip() != null)
			{
				lblTest.getTooltip().setText(msg);
			}
			else
			{
				lblTest.setTooltip(new Tooltip(msg));
			}
		});
	}
	
	public void testSqlConnection(String sql, String server, String base, String user, String password) throws Exception
	{
		Common.tryCatch(() ->
		{
			Settings settings = this.model.getFactory().getSettings();

			settings.removeAll(Settings.GLOBAL_NS, Settings.SQL + sql);
			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.SERVER_NAME, server);
			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.USER, user);
			settings.setValue(Settings.GLOBAL_NS, Settings.SQL + sql, TestingConnectionFxController.DATABASE_NAME, base);
			settings.saveIfNeeded();
			SqlConnection connect = this.model.getDataBasesPool().connect(sql, server, base, user, password);
			if (connect != null && !connect.isClosed() && connect.getConnection().isValid(1))
			{
				displayConnectionGood();
			}
			else
			{
				displayConnectionBad(null);
			}
		}, R.TESTING_CONNECTION_FX_CONTR_ERROR.get());
	}

}
