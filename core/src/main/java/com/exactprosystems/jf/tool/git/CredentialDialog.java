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
package com.exactprosystems.jf.tool.git;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class CredentialDialog
{
	private BiConsumer<String, String> consumer;

	public CredentialDialog(BiConsumer<String, String> consumer)
	{
		this.consumer = consumer;
	}

	public void display(String username, String password)
	{
		Dialog dialog = new Dialog();
		DialogsHelper.centreDialog(dialog);
		Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
		dialog.setResizable(true);

		GridPane gridPane = new GridPane();

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPercentWidth(35);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(65);

		gridPane.getColumnConstraints().addAll(c0, c1);

		TextField tfUsername = new TextField(username != null ? username : "");
		PasswordField pfPassword = new PasswordField();
		pfPassword.setText(password != null ? password : "");

		Label labelUserName = new Label(R.CREDENTIAL_DIALOG_USERNAME.get());
		gridPane.add(labelUserName, 0, 0);
		gridPane.add(tfUsername, 1, 0);
		Label labelPassword = new Label(R.CREDENTIAL_DIALOG_PASSWORD.get());
		gridPane.add(labelPassword, 0, 1);
		gridPane.add(pfPassword, 1, 1);

		Arrays.asList(tfUsername, pfPassword, labelPassword, labelUserName).stream().forEach(n -> GridPane.setMargin(n, new Insets(2, 0, 2, 0)));

		dialog.setTitle(R.CREDENTIAL_DIALOG_CREDENTIAL.get());
		dialog.setHeaderText(R.CREDENTIAL_DIALOG_STORE_CREDENTIAL.get());

		dialog.setResultConverter(param -> {
			this.consumer.accept(tfUsername.getText().isEmpty() ? null : tfUsername.getText(), pfPassword.getText());
			return null;
		});
		ButtonType buttonOk = new ButtonType(R.COMMON_OK.get(), ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().setAll(buttonOk);
		dialog.getDialogPane().setContent(gridPane);
		dialog.getDialogPane().lookupButton(buttonOk).disableProperty().bind(tfUsername.textProperty().isEmpty().or(pfPassword.textProperty().isEmpty()));
		dialog.showAndWait();
	}
}
