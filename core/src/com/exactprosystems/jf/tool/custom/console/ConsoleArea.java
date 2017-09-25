package com.exactprosystems.jf.tool.custom.console;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.awt.MouseInfo;
import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ConsoleArea extends StyleClassedTextArea
{
    private ArrayList<Link> list;
    private int charIndex;

	private ContextMenu contextMenu;

    public ConsoleArea(Consumer<TreeItem<MatrixItem>> moveToMatrixItem)
    {
        this.list = new ArrayList<>();
        this.setMouseOverTextDelay(Duration.ofMillis(10));
        this.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> this.charIndex = e.getCharacterIndex());
        this.setOnMouseReleased(event ->
        {
            for (Link link : this.list)
            {
				if (this.charIndex > link.getStart() && this.charIndex < link.getEnd())
				{
					moveToMatrixItem.accept(link.getItem());
					break;
				}
			}
        });
		this.contextMenu = new ContextMenu();
		this.contextMenu.setAutoFix(true);
		this.contextMenu.setAutoHide(true);
		MenuItem itemClear = new MenuItem("Clear");
		itemClear.setOnAction(e -> this.clear());
		this.contextMenu.getItems().addAll(itemClear);
		this.setOnContextMenuRequested(e ->
		{
			this.contextMenu.show(this.getScene().getWindow(), MouseInfo.getPointerInfo().getLocation().getX(),MouseInfo.getPointerInfo().getLocation().getY());
		});
    }

    public void appendDefaultText(String text)
    {
        this.appendStyledText(text, false, CssVariables.CONSOLE_DEFAULT_TEXT);
    }

    public void appendDefaultTextOnNewLine(String text)
    {
        this.appendStyledText(text, true, CssVariables.CONSOLE_DEFAULT_TEXT);
    }

    public void appendErrorText(String text)
    {
        this.appendStyledText(text, false, CssVariables.CONSOLE_DEFAULT_TEXT);
    }

    public void appendErrorTextOnNewLine(String text)
    {
        this.appendStyledText(text, true, CssVariables.CONSOLE_ERROR_ITEM);
    }

    private void appendStyledText(String text, boolean newLine, String style)
    {
        int start = this.getLength();
        this.appendText(newLine ? text + "\n" : text);
        this.setStyleClass(start, this.getLength(), style);
    }

    public void appendMatrixItemLink(String text, TreeItem <MatrixItem> item)
    {
        int start = this.getLength();
        this.appendText(text + "\n");
        this.setStyleClass(start, this.getLength(), CssVariables.CONSOLE_PAUSED_ITEM);
        this.list.add(new Link(start, this.getLength(), item));
    }

    private class Link
    {
        int start;
        int end;
        TreeItem <MatrixItem> item;

        private Link(int start, int end, TreeItem <MatrixItem> item) {
            this.start = start;
            this.end = end;
            this.item = item;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public TreeItem<MatrixItem> getItem() {
            return item;
        }
    }
}
