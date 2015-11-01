package com.exactprosystems.jf.tool.settings;

import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class ShortcutRow extends GridPane
{
	public interface EditShortcut
	{
		void edit(String key, String newShortcut);
		String nameOtherShortcut(String value, String currentKey);
	}

	private TextField textField;
	private Button resetButton;
	private String key;
	private String[] previousValue = new String[] {Common.empty};

	public ShortcutRow(String key, EditShortcut editShortcut)
	{
		this.key = key;
		this.setId(this.key);
		Label label = new Label(key);
		label.setTooltip(new Tooltip(label.getText()));
		this.textField = new TextField(Common.empty);
		this.resetButton = new Button("Reset");
		listeners(editShortcut);

		RowConstraints rowConstraints = new RowConstraints();
		rowConstraints.setValignment(VPos.CENTER);
		this.getRowConstraints().addAll(rowConstraints);

		ColumnConstraints c0 = new ColumnConstraints();
		c0.setPercentWidth(25);
		c0.setMaxWidth(150);
		c0.setHalignment(HPos.CENTER);

		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(50);
		c1.setFillWidth(true);
		c1.setHalignment(HPos.CENTER);

		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(25);
		c2.setHalignment(HPos.CENTER);

		this.getColumnConstraints().addAll(c0, c1, c2);

		this.add(label, 0, 0);
		this.add(this.textField, 1, 0);
		this.add(this.resetButton, 2, 0);
	}

	public void setShortcut(String shortcut)
	{
		this.textField.setText(shortcut);
	}

	private void listeners(final EditShortcut editShortcut)
	{
		this.resetButton.setOnAction(actionEvent -> {
			textField.setText(Common.empty);
			editShortcut.edit(key, Common.empty);
		});

		this.textField.setOnKeyPressed(keyEvent -> {
			if (validHotKey(keyEvent.getCode()))
			{
				KeyCombination keyCombination = createShortCut(keyEvent);
				previousValue[0] = keyCombination.getName();
			}
		});

		this.textField.setOnKeyReleased(keyEvent -> {
			String value = previousValue[0];
			if (!value.equals(Common.empty))
			{
				textField.setText(value);
				editShortcut.edit(key, value);
				String otherShortcut = editShortcut.nameOtherShortcut(value, key);
				if (otherShortcut != null)
				{
					if (!textField.getStyleClass().contains(CssVariables.INCORRECT_FIELD))
					{
						textField.getStyleClass().add(CssVariables.INCORRECT_FIELD);
					}
					if (textField.getTooltip() == null)
					{
						textField.setTooltip(new Tooltip("This shortcut used " + otherShortcut));
					}
					else
					{
						textField.getTooltip().setText("This shortcut used " + otherShortcut);
					}
				}
				else
				{
					textField.getStyleClass().remove(CssVariables.INCORRECT_FIELD);
					textField.setTooltip(null);
				}
			}
		});
	}

	static boolean validHotKey(KeyCode code)
	{
		switch (code)
		{
			case Q: case W: case E: case R: case T: case Y: case U: case I: case O: case P:
			case A: case S: case D: case F: case G: case H: case J: case K: case L:
			case Z: case X: case C: case V: case B: case N: case M:

			case DIGIT1: case DIGIT2: case DIGIT3:
			case DIGIT4: case DIGIT5: case DIGIT6:
			case DIGIT7: case DIGIT8: case DIGIT9:
			case DIGIT0:

			case INSERT: case ENTER: case ESCAPE: case SPACE:

			case UP: case DOWN: case LEFT: case RIGHT:

			case F1 : case F2:  case F3:
			case F4 : case F5:  case F6:
			case F7	: case F8:  case F9:
			case F10: case F11: case F12:

			case MINUS: case PLUS: case EQUALS: case DELETE:
			return true;

			default:
				return false;
		}
	}

	static KeyCombination createShortCut(KeyEvent keyEvent)
	{
		String combination = "";
		if (keyEvent.isShiftDown())
		{
			combination+= KeyCodeCombination.SHIFT_DOWN + "+";
		}
		if (keyEvent.isAltDown())
		{
			combination+=KeyCodeCombination.ALT_DOWN + "+";
		}
		if (keyEvent.isControlDown())
		{
			combination+=KeyCodeCombination.CONTROL_DOWN + "+";
		}
		combination+=keyEvent.getCode();
		return KeyCodeCombination.valueOf(combination);
	}
}
