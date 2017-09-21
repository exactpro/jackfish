package com.exactprosystems.jf.tool.custom.console;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.geometry.VPos;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.ReadOnlyStyledDocument;
import org.fxmisc.richtext.model.StyledText;
import org.fxmisc.richtext.model.TextOps;
import org.reactfx.util.Either;

import java.util.function.Consumer;
import java.util.function.Function;

public class ConsoleArea extends GenericStyledArea<Void, Either<StyledText<TextStyle>, MatrixItemLink<TextStyle>>, TextStyle>
{
    private static TextOps<StyledText<TextStyle>, TextStyle> STYLED_TEXT_OPS = StyledText.textOps();
    private static MatrixItemLinkOps<TextStyle> MATRIXITEMLINK_OPS = new MatrixItemLinkOps<>();
    private static TextOps<Either<StyledText<TextStyle>, MatrixItemLink<TextStyle>>, TextStyle> EITHER_OPS = STYLED_TEXT_OPS._or(MATRIXITEMLINK_OPS);

    public ConsoleArea(Consumer<TreeItem<MatrixItem>> moveToMatixItem) {
        super(
                null,
                (t, p) -> {},
                TextStyle.EMPTY,
                EITHER_OPS,
                e -> e.unify(styledText -> createStyledTextNode(t ->
                        {
                            t.setText(styledText.getText());
                            t.setStyle(styledText
                                    .getStyle()
                                    .toCss());
                        }),
                        matrixItemLink ->
                                createStyledTextNode(t -> {
                                    if (matrixItemLink.isReal()) {
                                        t.setText(matrixItemLink.getDisplayedText());
                                        t.getStyleClass().add(CssVariables.CONSOLE_PAUSED_ITEM);
                                        t.setOnMouseClicked(ae -> moveToMatixItem.accept(matrixItemLink.getItem()));
                                    }
                                })
                )
        );
    }

    public void appendDefaultText(String text)
    {
        super.appendText(text + "\n");
    }

    public void appendErrorText(String text)
    {
        super.appendText(text + "\n");
    }

    public void appendMatrixItemLink(String displayedText, TreeItem <MatrixItem> item) {
        replaceWithLink(getLength(), getLength(), displayedText + "\n", item);
    }

    public void replaceWithLink(int start, int end, String displayedText, TreeItem <MatrixItem> item) {
        replace(start, end, ReadOnlyStyledDocument.fromSegment(
                Either.right(new MatrixItemLink<>(displayedText, displayedText, TextStyle.EMPTY, item)),
                null,
                TextStyle.EMPTY,
                EITHER_OPS
        ));
    }

    public static Text createStyledTextNode(Consumer<Text> applySegment) {
        Text t = new Text();
        t.setTextOrigin(VPos.TOP);
        applySegment.accept(t);

        t.impl_selectionFillProperty().bind(t.fillProperty());
        return t;
    }
}
