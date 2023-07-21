
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import static java.lang.Thread.sleep;


public class Controlador {
    Logger log= Logger.getLogger(Controlador.class);
    public static WebDriver driver = null;
    WebDriverWait wait =null;
    private ArrayList<String[]> datos=new ArrayList<>();
    private CsvReader csvRead =new CsvReader();
    private Properties propiedades = new Properties();
    private Properties propiedades2 = new Properties();
    private long timeout;
    int contador=1;
    boolean bien=true;
    int numeroPujas=0;
    BigDecimal valor;
    BigDecimal valorMinimo;
    BigDecimal puja;
    String idPuja;
    DecimalFormat df;
    Boolean coincideValor=false;
    Long timeoutPuja;
    @BeforeTest
    public void setUp() throws IOException, InterruptedException {
        df = new DecimalFormat("0.0000");
        propiedades.load(new FileReader("C:\\Users\\crist\\Desktop\\Properties\\paltas.properties"));
        System.setProperty("webdriver.chrome.driver", propiedades.getProperty("driverPath"));
        timeout=Long.valueOf(propiedades.getProperty("timeout"));
        driver=new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        //datos=csvRead.read(propiedades.getProperty("urlCsv"));
        valor=new BigDecimal(propiedades.getProperty("valorInicial"));
        valorMinimo=new BigDecimal(propiedades.getProperty("valorMinimo"));
        driver.manage().window().maximize();
        String urlFitBankEntorno=propiedades.getProperty("url");
        driver.get(urlFitBankEntorno);
        puja =new BigDecimal(propiedades.getProperty("valorPuja"));
        //numeroPujas=Integer.parseInt(propiedades.getProperty("numeroPujas"));
        loginSercop();
    }
    @Test
    private void ReadDataTest() throws CsvValidationException, IOException, InterruptedException {
        try {
            while(valor.compareTo(valorMinimo)>=0 ) {
                coincideValor=false;
                openTransaction();
                checkAlert() ;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void openTransaction() throws IOException {

        try{
            try{
                driver.switchTo().alert().accept();
            }catch (Exception e){
               // log.info("No hay alerta");
            }
            By elements= By.xpath("//button[text()=' Pujar']");
            wait.until(ExpectedConditions.elementToBeClickable(elements));
            propiedades.load(new FileReader("C:\\Users\\crist\\Desktop\\Properties\\paltas.properties"));
            idPuja=propiedades.getProperty("id");
            Boolean datos=Boolean.valueOf(propiedades.getProperty("enviarDatos"));
            timeoutPuja=Long.valueOf(propiedades.getProperty("timeoutPuja"));
            valorMinimo=new BigDecimal(propiedades.getProperty("valorMinimo"));
            if(datos) {
                WebElement ganadora = driver.findElement(By.id("o-ganadora" + idPuja));
                String valorOferta = ganadora.getText().replace(".", "").replace(",", ".");
                valor = new BigDecimal(valorOferta);
                valor = valor.subtract(valor.multiply(puja).divide(new BigDecimal(100)));
                valor=valor.setScale(4,RoundingMode.DOWN);
                log.info("Inicio " + contador + "-" + valor + "-" + new Date());
                //log.info(valor );
                WebElement entorno = driver.findElement(By.id("valor-"+idPuja));
                entorno.click();
                entorno.sendKeys(valor.toString());
                WebDriverWait wait1=new WebDriverWait(driver,10);
                wait1.until(ExpectedConditions.alertIsPresent());
                String mensaje = driver.switchTo().alert().getText();
                log.info(mensaje);
                if (mensaje.contains("una cantidad")) {
                    coincideValor= true;
                }
                checkAlert();
                try {
                    checkAlert2();
                    String deb = driver.switchTo().alert().getText();
                    log.info(deb);
                    Alert alert = driver.switchTo().alert();
                    alert.accept();
                    /*if (deb.contains("debe ser")) {
                        pujar(deb, entorno);
                    }*/
                } catch (Exception e) {
                    //log.info("No existe error");
                }
                contador++;
                entorno.clear();
                while(!coincideValor){
                  String ultimoValor =ganadora.getText().replace(".", "").replace(",", ".");
                  BigDecimal ultimo=new BigDecimal(ultimoValor);

                  if(ultimo.compareTo(valor)==0){
                      coincideValor= true;
                  }
                }
            /*    ExpectedCondition<Boolean> elementTextEqualsString = arg0 -> ganadora.getText().replace(".", "").replace(",", ".").equals(valor.toString());
                wait.until(elementTextEqualsString);*/

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void pujar(String deb,WebElement entorno) throws InterruptedException {

        try{
            driver.switchTo().alert().accept();
        }catch (Exception e){
          // log.info("NO existe alerta puja");
        }
        entorno.clear();
        By elements= By.className("input-sm");
        wait.until(ExpectedConditions.elementToBeClickable(elements));
        String val=deb.substring(deb.indexOf("ser: "));
        val=val.replace("ser: ","");
        val=val.trim();
        valor=new BigDecimal(val);
        log.info("Valor equivocado "+valor+"-"+new Date());
        entorno.click();
        entorno.sendKeys(val);
        WebElement element= driver.findElement(By.xpath( "//button[text()=' Pujar']"));
        element.click();
        String mensaje=driver.switchTo().alert().getText();
        log.info(mensaje);
        checkAlert();
        valor=new BigDecimal(val);

    }
    private void pujarVacio(String valor,WebElement entorno){
        try{
            driver.switchTo().alert().accept();
        }catch (Exception e){
           // log.info("NO existe alerta puja");
        }
        entorno.clear();
        entorno.click();
        entorno.sendKeys(valor);
        //log.info("Valor Vacio "+valor);
        WebElement element= driver.findElement(By.xpath( "//button[text()=' Pujar']"));
        element.click();
        checkAlert();

    }
    @AfterTest
    public void exitDriver(){
       // driver.close();
        //driver.quit();
    }
    public void captureScreen(String cuenta) {
        String path;
        try {
            File source = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            path = propiedades.getProperty("screenshotsPath") + cuenta;
            FileUtils.copyFile(source, new File(path));
        }
        catch(IOException e) {
           // log.info("Failed to capture screenshot: " + e.getMessage());
        }
    }
    private void loginSercop(){
        try {
            By ru= By.id("ruc");
            wait.until(ExpectedConditions.elementToBeClickable(ru));
            WebElement ruc = driver.findElement(By.id("ruc"));
            ruc.click();
            ruc.sendKeys("1191788334001");
            WebElement user = driver.findElement(By.id("username"));
            user.click();
            user.sendKeys("PALTASEG");
            WebElement password = driver.findElement(By.id("password"));
            password.click();
            password.sendKeys("CIA*paltaseg2021");
            WebElement sub=driver.findElement(By.xpath("//button[@type='submit']"));
            sub.click();
           /* By elements= By.linkText("Ver");
            wait.until(ExpectedConditions.elementToBeClickable(elements));
            driver.findElement(By.linkText("Ver")).click();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void checkAlert() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, 1);
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            alert.accept();
        } catch (Exception e) {
            //exception handling
        }
    }
    public void checkAlert2() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeoutPuja);
            wait.until(ExpectedConditions.alertIsPresent());

        } catch (Exception e) {
            //exception handling
        }
    }
}

