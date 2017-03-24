package www.william.com;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;

import com.csvreader.CsvReader;

import org.junit.Test;

public class testGitHub {
	private WebDriver driver;
	private String baseUrl;
	private boolean acceptNextAlert = true;
	private StringBuffer verificationErrors = new StringBuffer();
	
	@Before
	public void setUp() throws Exception {
	    driver = new FirefoxDriver();
	    baseUrl = "http://121.193.130.195:8080";
	    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
	    driver.manage().window().maximize();
	}

	@Test
	public void testGitHub()throws Exception {
	
		String record = "";
		File inFile = new File("D://inputgit.csv");
		BufferedReader reader = new BufferedReader(new FileReader(inFile));
		CsvReader creader = new CsvReader(reader,',');
		creader.skipRecord();
		while(creader.readRecord()){
			record = creader.getRawRecord();
			String[] infor = record.split("\\,");
			System.out.println(infor[0]);
			String passwd=infor[0].substring(infor[0].length()-6,infor[0].length());

			driver.get(baseUrl + "/");
		    driver.findElement(By.id("name")).clear();
		    driver.findElement(By.id("name")).sendKeys(infor[0]);
		    driver.findElement(By.id("pwd")).clear();
		    driver.findElement(By.id("pwd")).sendKeys(passwd);
		    driver.findElement(By.id("submit")).click();
		    
		    assertEquals("µÇÂ¼½á¹û", driver.getTitle());
		    assertEquals(infor[0], driver.findElement(By.xpath("//tbody[@id='table-main']/tr[2]/td[2]")).getText());
		    assertEquals(infor[2], driver.findElement(By.xpath("//tbody[@id='table-main']/tr[3]/td[2]")).getText());
		}
	}
	
	@After
	public void tearDown() throws Exception {
	    driver.quit();
	    String verificationErrorString = verificationErrors.toString();
	    if (!"".equals(verificationErrorString)) {
	      fail(verificationErrorString);
	    }
	  }
	private boolean isElementPresent(By by) {
	    try {
	      driver.findElement(by);
	      return true;
	    } catch (NoSuchElementException e) {
	      return false;
	    }
	  }

	  private boolean isAlertPresent() {
	    try {
	      driver.switchTo().alert();
	      return true;
	    } catch (NoAlertPresentException e) {
	      return false;
	    }
	  }

	  private String closeAlertAndGetItsText() {
	    try {
	      Alert alert = driver.switchTo().alert();
	      String alertText = alert.getText();
	      if (acceptNextAlert) {
	        alert.accept();
	      } else {
	        alert.dismiss();
	      }
	      return alertText;
	    } finally {
	      acceptNextAlert = true;
	    }
	  }
}
