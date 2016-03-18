package com.company;
/**
 * Created by n.ivanov on 25.03.2015.
 */

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.junit.After;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class browserEnergia {

    File exlFile = new File("C:\\Ruby21\\input.xls");
    Workbook w;

    String type, weight, volume, to, from;
    int enteredNumber = 2;


    public int SetParams(int row, Selenium selenium) throws Exception {


        int cost = 0;
        try {
            w = Workbook.getWorkbook(exlFile);
            Sheet sheet = w.getSheet(0);

            Cell cell = sheet.getCell(1, row);
            from = cell.getContents().toString();
            //   System.out.print(cell.getContents().toString());

            cell = sheet.getCell(2, row);
            to = cell.getContents().toString();
            //  System.out.print(cell.getContents().toString());

            cell = sheet.getCell(10, row); //ves
            weight = cell.getContents().toString().replaceAll(",", ".");
            selenium.type("id=weight", cell.getContents().toString().replaceAll(",", ".")); //setted ves

            cell = sheet.getCell(11, row); //volume

            volume = cell.getContents().toString().replaceAll(",", ".");
            selenium.type("id=volume", volume.toString()); //o


            //Thread.sleep(1000);
            try {
                selenium.select("id=cityFrom", "label=" + from);
                selenium.select("id=cityTo", "label=" + to);
            } catch (SeleniumException e) {
                //  e.printStackTrace();
                System.out.println("not found" + row);
                //    selenium.captureEntirePageScreen;shot("C:\\errorlogCrowler\\Select Cities is failed" + " " + System.currentTimeMillis() + ".png", "");
                throw new Exception("По направлению " + from + "-" + to + " перевозка невозможна ");

            }


            //  Thread.sleep(2000);
        } catch (BiffException e) {
            e.printStackTrace();
            selenium.captureEntirePageScreenshot("C:\\errorlogCrowler\\BiffException" + " " + System.currentTimeMillis() + ".png", "");
        } catch (SeleniumException e) {
            selenium.captureEntirePageScreenshot("C:\\errorlogCrowler\\SeleniumException on row " + row + " " + System.currentTimeMillis() + ".png", "");
            selenium.open("/calculator.html");
            Thread.sleep(3000);
        }
        return cost;
    }


//    public void browserReload() throws Exception {
//        selenium.close();
//        selenium.stop();
//        // Thread.sleep(5000);
//        setUp();
//        selenium.open("/calculator.html");
//        Thread.sleep(1000);
//
//    }


    public static void main(String[] args)  throws Exception

    {
        Selenium selenium;

        browserEnergia test = new browserEnergia();
        test.testVar9SpbMsk();
        //selenium = new DefaultSelenium("localhost", 4444, "*chrome", "http://nrg-tk.ru/client");
        //selenium.start();
    }


    public void testVar9SpbMsk() throws Exception {

        Selenium selenium;
        selenium = new DefaultSelenium("localhost", 4444, "*firefox C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe", "http://nrg-tk.ru/client");

        selenium.start();
        selenium.open("/calculator.html");
        Thread.sleep(1000);
        File crowlerResult = new File("C:\\Users\\n.ivanov\\output.xls");
        WritableWorkbook writableWorkbook = Workbook.createWorkbook(crowlerResult);

        WritableSheet writableSheet = writableWorkbook.createSheet("Sheet2", 0);
        String MT = "";
        try {
            System.out.println("start");

            String ves = "";
            int count = 0;

            Label label0 = new Label(0, 0, "Направление");
            Label label1 = new Label(1, 0, "Забор");
            Label label2 = new Label(2, 0, "Доставка");
            Label label3 = new Label(3, 0, "MT");
            Label label4 = new Label(4, 0, "ИТОГ");
            Label label5 = new Label(5, 0, "Вес");
            Label label6 = new Label(6, 0,"Обьем" );
            Label label7 = new Label(7, 0, "Тип");


            writableSheet.addCell(label0);
            writableSheet.addCell(label1);//откуда-куда
            writableSheet.addCell(label2); //забор
            writableSheet.addCell(label3); //доставка
            writableSheet.addCell(label4); //цена
            writableSheet.addCell(label5); //ves
            writableSheet.addCell(label6);
            writableSheet.addCell(label7);

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Введите количество обрабатываемых строк:");
            try {
                enteredNumber = Integer.parseInt(br.readLine());
            } catch (NumberFormatException nfe) {
                System.err.println("Неверный формат");
                Thread.sleep(3000);
            }

            for (int i = 1; i < enteredNumber; i++) {//18392  //4732
                try {
                   /* if (count == 50) {
                        browserReload(); /*Thread.sleep(6000);*/
                      /*  selenium.open("/calculator.html");  /* Thread.sleep(3000);*/
                  /*      count = 0;*/
                  /*  }
                    count++;*/
                    SetParams(i, selenium);

                    selenium.click("css=button.btn.btn-primary");
                    //selenium.waitForPageToLoad("20000");
                    for (int j = 1; j < 8; j++) {
                        try {
                            if ((selenium.getText("css=td").contains(to)) && (selenium.getText("css=td").contains(from)))
                                break;
                        } catch (SeleniumException e) {
                        }
                        Thread.sleep(1000);

                        if (j == 7) throw new Exception("не посчиталось!");
                    }

                    if (selenium.isTextPresent("Авиа") || selenium.isTextPresent("ЖД")) {
                        if (selenium.isTextPresent("Авто")) ;
                        else throw new Exception("Только АВИА/ЖД перевозка");
                        System.out.println("avia/ghd on " + i);
                        if (selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/thead/tr/th[2]").equals("Авто")) {
                            MT = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/tbody/tr/td[2]");
                            type = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/thead/tr/th[2]");
                        } else if (selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/thead/tr/th[3]").equals("Авто")) {
                            MT = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/tbody/tr/td[3]");
                            type = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/thead/tr/th[3]");
                        } else {
                            MT = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/tbody/tr/td[4]");
                            type = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/thead/tr/th[4]");
                        }
                    } else {
                        MT = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/tbody/tr/td[2]");
                        type = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/thead/tr/th[2]");
                    }


                    String ok = selenium.getText("css=td");
                    String zabor = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/tbody/tr[2]/td[2]");
                    String dostavka = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/tbody/tr[3]/td[2]");
                    String itogo = selenium.getText("//div[@id='all']/div/div[2]/aside/section/table/tbody/tr[4]/td[2]");
                    //  ves = selenium.getValue("id=weight");

                    System.out.println(i);
                    // System.out.print("  Забор " + zabor);
                    // System.out.println(" Доставка " + dostavka);

                    label0 = new Label(0, i, ok);
                    label1 = new Label(1, i, zabor);
                    label2 = new Label(2, i, dostavka);
                    label3 = new Label(3, i, MT);
                    label4 = new Label(4, i, itogo);
                    label5 = new Label(5, i, selenium.getValue("id=weight"));
                    label6 = new Label(6, i, selenium.getValue("id=volume"));
                    label7 = new Label(7, i, type);

                    writableSheet.addCell(label0);//откуда-куда
                    writableSheet.addCell(label1); //забор
                    writableSheet.addCell(label2); //доставка
                    writableSheet.addCell(label3); //цена
                    writableSheet.addCell(label4); //цена
                    writableSheet.addCell(label5); //ves
                    writableSheet.addCell(label6);
                    writableSheet.addCell(label7);
                } catch (SeleniumException e) {
                    selenium.captureEntirePageScreenshot("C:\\Users\\adm\\Dropbox\\errorlogCrowler\\чтото не нашлось  " + i + " " + System.currentTimeMillis() + ".png", "");
                    selenium.open("/calculator.html");
                    Thread.sleep(3000);
                } catch (Exception e) {//selenium.captureEntirePageScreenshot("C:\\Users\\adm\\Dropbox\\errorlogCrowler\\Exception " + System.currentTimeMillis() + ".png", "");
                    Label label8 = new Label(7, i, e.getMessage());
                    writableSheet.addCell(label8);
                    // browserReload();
                }
            }

            System.out.print("is done");
            Thread.sleep(2000);
            System.out.println("Cycled is over");
        } catch (SeleniumException e) {
            // selenium.captureEntirePageScreenshot("C:\\errorlogCrowler\\Some test FAILed " + System.currentTimeMillis() + ".png", "");
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        } finally {
            writableWorkbook.write();
            writableWorkbook.close();
            selenium.stop();
        }
    }


    @After
    public void tearDown() throws Exception {

    }
}
