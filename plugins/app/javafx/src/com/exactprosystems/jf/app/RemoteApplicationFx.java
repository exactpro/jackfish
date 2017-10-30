package com.exactprosystems.jf.app;

import com.exactprosystems.jf.api.app.*;
import org.apache.log4j.*;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

public class RemoteApplicationFx extends RemoteApplication
{
    private Logger logger = null;
    private Robot  currentRobot;

    private OperationExecutorFx operationExecutor;
    private PluginInfo          info;

    @Override
    protected void createLoggerDerived(String logName, String serverLogLevel, String serverLogPattern) throws Exception
    {
        try
        {
            this.logger = Logger.getLogger(RemoteApplicationFx.class);

            Layout layout = new PatternLayout(serverLogPattern);
            Appender appender = new FileAppender(layout, logName);
            this.logger.addAppender(appender);
            this.logger.setLevel(Level.toLevel(serverLogLevel, Level.ALL));

            MatcherFx.setLogger(this.logger);
        }
        catch (RemoteException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            logger.error(String.format("createLoggerDerived(%s, %s,%s)", logName, serverLogLevel, serverLogPattern));
            logger.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    protected void setPluginInfoDerived(PluginInfo info) throws Exception
    {

    }

    @Override
    protected int connectDerived(Map<String, String> args) throws Exception
    {
        return 0;
    }

    @Override
    protected int runDerived(Map <String, String> args) throws Exception
    {
        return 0;
    }

    @Override
    protected void stopDerived(boolean needKill) throws Exception
    {

    }

    @Override
    protected void refreshDerived() throws Exception
    {

    }

    @Override
    protected String getAlertTextDerived() throws Exception
    {
        return null;
    }

    @Override
    protected void navigateDerived(NavigateKind kind) throws Exception
    {

    }

    @Override
    protected void setAlertTextDerived(String text, PerformKind performKind) throws Exception
    {

    }

    @Override
    protected Collection<String> titlesDerived() throws Exception
    {
        return null;
    }

    @Override
    protected String switchToDerived(Map <String, String> criteria, boolean softCondition) throws Exception
    {
        return null;
    }

    @Override
    protected void switchToFrameDerived(Locator owner, Locator element) throws Exception
    {

    }

    @Override
    protected void resizeDerived(Resize resize, int height, int width) throws Exception
    {

    }

    @Override
    protected Collection <String> findAllDerived(Locator owner, Locator element) throws Exception
    {
        return null;
    }

    @Override
    protected ImageWrapper getImageDerived(Locator owner, Locator element) throws Exception
    {
        return null;
    }

    @Override
    protected Rectangle getRectangleDerived(Locator owner, Locator element) throws Exception
    {
        return null;
    }

    @Override
    protected OperationResult operateDerived(Locator owner, Locator element, Locator rows, Locator header, Operation operation) throws Exception
    {
        return null;
    }

    @Override
    protected CheckingLayoutResult checkLayoutDerived(Locator owner, Locator element, Spec spec) throws Exception
    {
        return null;
    }

    @Override
    protected void newInstanceDerived(Map <String, String> args) throws Exception
    {

    }

    @Override
    protected int closeAllDerived(Locator element, Collection <LocatorAndOperation> operations) throws Exception
    {
        return 0;
    }

    @Override
    protected String closeWindowDerived() throws Exception
    {
        return null;
    }

    @Override
    protected Document getTreeDerived(Locator owner) throws Exception
    {
        return null;
    }

    @Override
    protected void startNewDialogDerived() throws Exception
    {

    }

    @Override
    protected void moveWindowDerived(int x, int y) throws Exception
    {

    }

    @Override
    public Serializable getProperty(String name, Serializable prop) throws RemoteException
    {
        return null;
    }

    @Override
    public void setProperty(String name, Serializable prop) throws RemoteException
    {

    }
}
