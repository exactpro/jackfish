////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app.js;

import com.exactprosystems.jf.app.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class JSInjection
{
	private final String css = "rgb(173, 216, 230)";
	private final String style = "backgroundColor";

	public static void main(String[] args) throws Exception
	{
		Browser browser = Browser.CHROME;
		WebDriver driver = browser.createDriver("");
		driver.get("http://yandex.ru");
		WebElement element = driver.findElement(By.xpath(".//button[@class='button suggest2-form__button button_theme_websearch button_size_m i-bem button_js_inited']"));
		System.out.println("style : " + element.getAttribute("style"));
	}

	public void startHighLight(WebDriver driver, WebElement element)
	{
		((JavascriptExecutor) driver).executeScript(
				"var element = arguments[0];" +
				"var oldStyle = undefined;" +
				"if (element != undefined) {" +
				"	oldStyle = element.style."+style+";"+
				"	element.style."+style+" = '" + css + "'"+
				"}"
				,element);
	}

	public void stopHighLight(WebDriver driver, WebElement element)
	{
		((JavascriptExecutor) driver).executeScript(
				"var element = arguments[0];" +
				"if (element != undefined) {" +
				"	element.style."+style+" = ''"+
				"}"
				,element);
	}

	public abstract void injectJSHighlight	(WebDriver driver);
	public abstract void injectJSLocation	(WebDriver driver);

	public final void stopInject(WebDriver driver)
	{
		((JavascriptExecutor) driver).executeScript("document.onmouseout = function(e){}");
	}

	protected String getLocation(String x, String y)
	{
		return
		"document.myMouseLocation = [];"+
		"document.onmousedown = function(e) {"+
		"	document.myMouseLocation[0] = e." + x + ";" +
		"	document.myMouseLocation[1] = e." + y + ";" +
		"}";
	}

	protected String getHighlight(String to, String out)
	{
		return
		"document.onmouseout = function(e) {" +
		"	var css = '"+css+"'; " +
		"	if (e."+to+" != null) { " +
		"		e."+to+".style."+style+" = css;"+
		"	}"+
		"	if (e."+out+" != null) {"+
		"		var b = e."+out+".style."+style+";"+
		"		var index = b.indexOf(css);"+
		"		if (index != -1) {"+
		"			e."+out+".style."+style+" = b.substring(0,index).concat(b.substring(index + css.length));"+
		"		}"+
		"	}"+
		"}";
	}
}
