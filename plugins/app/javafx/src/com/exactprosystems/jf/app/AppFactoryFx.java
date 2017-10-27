package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.AbstractApplicationFactory;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.PluginInfo;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.PluginDescription;
import com.exactprosystems.jf.api.common.i18n.R;

import java.io.InputStream;

@PluginDescription(
        pluginName = "JAVAFX",
        description = R.JAVAFX_PLUGIN_DESCRIPTION,
        difference = R.JAVAFX_PLUGIN_DIFFERENCE
)
public class AppFactoryFx extends AbstractApplicationFactory
{

    @Override
    public InputStream getHelp()
    {
        return null;
    }

    @Override
    public IApplication createApplication() throws Exception
    {
        return null;
    }

    @Override
    public String getRemoteClassName()
    {
        return null;
    }

    @Override
    public PluginInfo getInfo()
    {
        return null;
    }

    @Override
    public String[] wellKnownParameters(ParametersKind kind)
    {
        return new String[0];
    }

    @Override
    public boolean canFillParameter(String parameterToFill)
    {
        return false;
    }

    @Override
    public String[] listForParameter(String parameterToFill)
    {
        return new String[0];
    }
}
