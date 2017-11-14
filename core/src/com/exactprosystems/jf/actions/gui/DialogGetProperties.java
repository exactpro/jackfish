package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.app.FeatureNotSupportedException;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.awt.*;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
        group 				  		  = ActionGroups.GUI,
        suffix 						  = "APPPAR",
        constantGeneralDescription    = R.DIALOG_GET_PROPERTIES_GENERAL_DESC,
        additionFieldsAllowed 		  = true,
        constantAdditionalDescription = R.DIALOG_GET_PROPERTIES_ADDITIONAL_DESC,
        outputType              	  = Map.class,
        constantOutputDescription 	  = R.DIALOG_GET_PROPERTIES_OUTPUT_DESC,
        constantExamples 			  = R.DIALOG_GET_PROPERTIES_EXAMPLE
)
public class DialogGetProperties extends AbstractAction
{

    public final static String	connectionName			= "AppConnection";
    public final static String	dialogName				= "Dialog";
    public final static String	sizeName				= "Size";
    public final static String	positionName			= "Position";

    @ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_GET_PROPERTIES_APP_CONNECTION)
    protected AppConnection     connection;

    @ActionFieldAttribute(name = dialogName, mandatory = true, constantDescription = R.DIALOG_GET_PROPERTIES_DIALOG)
    protected String			dialog;

    @ActionFieldAttribute(name = sizeName, mandatory = false, constantDescription = R.DIALOG_GET_PROPERTIES_SIZE)
    protected String            size;

    @ActionFieldAttribute(name = positionName, mandatory = false, constantDescription = R.DIALOG_GET_PROPERTIES_POSITION)
    protected String            position;




    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
    {
        switch (fieldName)
        {
            case dialogName:
                return HelpKind.ChooseFromList;
            default:
                break;
        }
        return null;
    }

    @Override
    protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
    {
        switch (parameterToFill)
        {
            case dialogName:
                Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
                break;
            default:
                break;
        }
    }

    @Override
    protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
    {
        Map<String, Object> result = new HashMap<>();

        IGuiDictionary dictionary = this.connection.getDictionary();
        IApplication app = this.connection.getApplication();
        String id = connection.getId();

        IWindow window = dictionary.getWindow(this.dialog);
        Helper.throwExceptionIfDialogNull(window, this.dialog);
        IControl selfControl = window.getSelfControl();
        IRemoteApplication service = app.service();

        if (selfControl == null)
        {
            super.setError(message(id, window, IWindow.SectionKind.Self, null, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
            return;
        }

        for (Parameter parameter : parameters.select(TypeMandatory.NotMandatory))
        {
            try
            {
                if (parameter.getName().equals(sizeName))
                {
                    Dimension dialogSize = service.getDialogSize(selfControl.locator());
                    result.put(parameter.getName(), dialogSize);
                }
                if (parameter.getName().equals(positionName))
                {
                    Point dialogPosition = service.getDialogPosition(selfControl.locator());
                    result.put(parameter.getName(), dialogPosition);
                }
            }
            catch (RemoteException e)
            {
                String mes = message(id, window, IWindow.SectionKind.Self, selfControl, null, e.getCause().getClass().getSimpleName());

                if (e.getCause() instanceof FeatureNotSupportedException)
                {
                    super.setError(mes, ErrorKind.FEATURE_NOT_SUPPORTED);
                    return;
                }else
                {
                    super.setError(mes, ErrorKind.EXCEPTION);
                    return;
                }
            }
        }

        setResult(result);
    }
}
