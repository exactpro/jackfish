/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Reflection;
using System.Diagnostics;
using System.Windows.Forms;

namespace UIAdapter.Logger
{
    public enum LogLevel
    {
        All,
        Error,
        None
    }

    class StringWithParams
    {
        string msg;
        Object[] param;

        public StringWithParams(String msg)
            : this(msg, null)
        { }

        public StringWithParams(String msg, Object[] param)
        {
            this.msg = msg;
            this.param = param;
        }

        public override string ToString()
        {
            if (this.param == null)
            {
                return this.msg;
            }
            return string.Format(msg, param);
        }
    }

    public class Logger : IDisposable
    {
        private static readonly String filePath = "log.txt";
        private FileInfo file = new FileInfo(Path.Combine(Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location), filePath));
        private readonly FileStream fileStream;

        private readonly Action<StringWithParams> allLogger;
        private readonly Action<StringWithParams> errorLogger;

        public static LogLevel logLevel(String str)
        {
            switch (str.ToUpper())
            {
                case "ALL": return LogLevel.All;
                case "ERROR": return LogLevel.Error;
            }
            return LogLevel.None;
        }

        public Logger(LogLevel logLevel)
        {
            switch (logLevel)
            {
                case LogLevel.All:
                    allLogger = (str) => WriteToFile(CreateLine(str.ToString(), "All"));
                    errorLogger = (str) => WriteToFile(CreateLine(str.ToString(), "Error"));
                    break;

                case LogLevel.Error:
                    allLogger = (str) => { }; //nothing
                    errorLogger = (str) => WriteToFile(CreateLine(str.ToString(), "Error"));
                    break;

                case LogLevel.None:
                    allLogger = (str) => { };
                    errorLogger = (str) => { };
                    break;
            }
            this.fileStream = new FileStream(file.FullName, FileMode.Append, FileAccess.Write, FileShare.Write);
        }

        public void All(String message, params Object[] parameters)
        {
            allLogger(new StringWithParams(message, parameters));
        }

        public void All(String message, long time)
        {
            allLogger(new StringWithParams(message + " ; time : " + time));
        }

        public void All(String message)
        {
            allLogger(new StringWithParams(message));
        }

        public void Error(String message, params Object[] parameters)
        {
            errorLogger(new StringWithParams(message, parameters));
        }

        public void Error(String message)
        {
            errorLogger(new StringWithParams(message));
        }

        public void Error(String message, Exception exception)
        {
            StringBuilder builder = new StringBuilder();
            builder.AppendLine(message);
            builder.Append("\t" + exception.GetType().FullName).AppendLine(" : " + exception.Message);
            builder.AppendLine(exception.StackTrace);
            this.Error(builder.ToString());
        }

        private String CreateLine(String message, String level)
        {
            StackTrace trace = new StackTrace(true);
            //TODO think how to add method name
            StackFrame frame = trace.GetFrame(3);
            if (frame.GetMethod().Name.Contains("Logger.cs"))
            {
                frame = trace.GetFrame(4);
            }
            return String.Format("{0,-5}  {1} [ThreadId : {2}] {3,17}:{4,4} - {5}\r\n"
                , level.ToUpper()
                , DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss.fff")
                , System.Threading.Thread.CurrentThread.ManagedThreadId
                , new FileInfo(frame.GetFileName()).Name
                //, frame.GetMethod().Name
                , frame.GetFileLineNumber()
                , message);
        }

        private void WriteToFile(String line)
        {
            using (FileStream w = file.Open(FileMode.Append, FileAccess.Write, FileShare.Write))
            {
                byte[] bytes = new UTF8Encoding(true).GetBytes(line);
                w.Write(bytes, 0, bytes.Length);
                w.Flush();
            }
        }

        public void Dispose()
        {
            fileStream.Dispose();
            fileStream.Close();
        }
    }
}
