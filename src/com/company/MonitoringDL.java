package com.company;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;

public class MonitoringDL {
    //   File exlFile = new File("input.xls");
    String inputPath = System.getProperty("user.dir"); //set relative path to icon
    File exlFile = new File(inputPath + "\\input.xls");
    Workbook w;
    String insuranceResponse, intercity, kladrFrom, kladrTo, summa, priceFrom, priceTO = "";
    Double weight, volume, insurance;
    int count;
    String exactCityName;
    String fromExact,toExact;
    HttpPost request;
    StringEntity params;
    HttpResponse response;
    HttpEntity entity;
    InputStream instream;
    BufferedReader reader;
    StringBuilder sb;
    JsonObject mainObject;
    JsonParser parser;
    HttpClient httpClient = HttpClientBuilder.create().build();

    public static void main(String[] args) throws Exception {
        MonitoringDL http = new MonitoringDL();
        System.out.println("Testing 1 - Send Http GET request");
        http.sendGet();
    }


    private String getKladr(String address) throws Exception {
        String kladr = "";
        // HttpClient httpClient1 = HttpClientBuilder.create().build();
        request = new HttpPost("https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address");
        StringEntity params = new StringEntity("{\"count\":1,\"query\":\"" + address + "\"}", "utf-8");
        request.addHeader("content-type", "application/json");
        request.addHeader("Authorization", "Token 84beb76a98914195f374779f2f313d31efca3c5d");
        request.addHeader("X-Secret", "cb82deee2d367b967ba569b5fc11b9e21a8c4832");
        request.setEntity(params);

        response = httpClient.execute(request);
        entity = response.getEntity();
        instream = entity.getContent();
        reader = new BufferedReader(new InputStreamReader(instream));

        String responseAsString = EntityUtils.toString(response.getEntity());

        parser = new JsonParser();//response.toString()
        JsonArray mainObject;
        mainObject = parser.parse(responseAsString.toString()).getAsJsonObject().getAsJsonArray("suggestions");
        //System.out.println(mainObject.get(0).getAsJsonObject().getAsJsonObject("data").get("kladr_id").getAsString());
        kladr = mainObject.get(0).getAsJsonObject().getAsJsonObject("data").get("kladr_id").getAsString();



        try {
            exactCityName = mainObject.get(0).getAsJsonObject().getAsJsonObject("data").get("city").getAsString();
            exactCityName = exactCityName + " " + mainObject.get(0).getAsJsonObject().getAsJsonObject("data").get("region").getAsString();
        } catch (UnsupportedOperationException e) {
            try {
                System.out.println("null city");
                exactCityName = mainObject.get(0).getAsJsonObject().getAsJsonObject("data").get("settlement").getAsString();
                exactCityName = exactCityName + " " + mainObject.get(0).getAsJsonObject().getAsJsonObject("data").get("region").getAsString();
            } catch (Exception ee) {

                System.out.println("cant recognized");
            }
        }
        catch (IndexOutOfBoundsException eee) {
            try {
                System.out.println("! Не удалось распознать "+address+": допишите к названию тип нас. пункта или его регион");
                exactCityName = "Не удалось распознать "+address+": допишите к названию тип нас. пункта или его регион";
                // throw new Exception ();
            } catch (Exception ee) {
                System.out.println("shouldnt to be");
            }
        }






        return kladr;

    }


    public void sendGet() throws Exception {
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setSuppressWarnings(true);
        // File crowlerResult = new File("C:\\Users\\usr\\Desktop\\MonitoringDL\\output.xls");
        File crowlerResult = new File(inputPath + "\\output.xls");
        // File crowlerResult = new File("C:\\Users\\n.ivanov\\Dropbox\\AutoMonitoringDL\\output.xls");
        w = Workbook.getWorkbook(exlFile, wbSettings);
        Sheet sheet = w.getSheet(0);
        WritableWorkbook writableWorkbook = Workbook.createWorkbook(crowlerResult);
        WritableSheet writableSheet = writableWorkbook.createSheet("Sheet2", 0);

        Label label01 = new Label(0, 0, "От_входные");
        Label label02 = new Label(1, 0, "До_входные");
        Label label03 = new Label(2, 0, "От_вычисленные");
        Label label04 = new Label(3, 0, "До_вычисленные");
        Label label05 = new Label(4, 0, "Вес");
        Label label06 = new Label(5, 0, "Объем");
        Label label07 = new Label(6, 0, "Забор");
        Label label08 = new Label(7, 0, "МТ");
        Label label09 = new Label(8, 0, "Отвоз");
        Label label10 = new Label(9, 0, "Страховка");
        Label label11= new Label(10, 0, "ИТОГО");

        writableSheet.addCell(label01);
        writableSheet.addCell(label02);
        writableSheet.addCell(label03);
        writableSheet.addCell(label04);
        writableSheet.addCell(label05);
        writableSheet.addCell(label06);
        writableSheet.addCell(label07);
        writableSheet.addCell(label08);
        writableSheet.addCell(label09);
        writableSheet.addCell(label10);
        writableSheet.addCell(label11);

        parser = new JsonParser();

        try {
            int enteredNumber = 0;
            String to = "";
            String from = "";

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Введите количество обрабатываемых строк:");
            try {
                enteredNumber = Integer.parseInt(br.readLine());
            } catch (NumberFormatException nfe) {
                System.err.println("Неверный формат");
                Thread.sleep(3000);
            }


            for (int i = 1; i < enteredNumber; i++) {
                try {
                    weight = 0.0;
                    volume = 0.0;
                    insurance = 0.0;
                    insuranceResponse = "";
                    intercity = "";
                    kladrFrom = "";
                    kladrTo = "";


                    System.out.print(i + " ");
                    Cell cell = sheet.getCell(1, i);
                    from = "г "+cell.getContents();

                    System.out.print(from + " ");
                    //  System.out.print(getKladr(from));
                    kladrFrom = getKladr(from) + "000000000000";
                    fromExact=exactCityName;

                    cell = sheet.getCell(2, i);
                    to = "г "+cell.getContents();
                    System.out.println(to);
                    kladrTo = getKladr(to) + "000000000000";
                    toExact=exactCityName;

                    cell = sheet.getCell(10, i); //ves
                    weight = Double.parseDouble(cell.getContents().replaceAll(",", "."));

                    cell = sheet.getCell(11, i); //volume
                    volume = Double.parseDouble(cell.getContents().replaceAll(",", "."));

                    try {
                        cell = sheet.getCell(32, i); //insurance
                        insurance = Double.parseDouble(cell.getContents().replaceAll(",", "."));

                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        insurance = 0.0;
                    }


                    request = new HttpPost("https://api.dellin.ru/v1/public/calculator.json");
                    params = new StringEntity("{\"appKey\":\"8E6F26C2-043D-11E5-8F8A-00505683A6D3\",    \"derivalPoint\":\"" + kladrFrom + "\",\"derivalDoor\":true,\"arrivalPoint\":\"" + kladrTo + "\"," +
                            "\"arrivalDoor\":true,\"sizedVolume\":\"" + volume + "\",\"sizedWeight\":\"" + weight + "\",\"statedValue\":\"" + insurance + "\"}");


                    request.addHeader("content-type", "application/javascript");
                    request.setEntity(params);

                    response = httpClient.execute(request);
                    //   System.out.println(response);

                    entity = response.getEntity();
                    instream = entity.getContent();

                    reader = new BufferedReader(new InputStreamReader(instream));
                    sb = new StringBuilder();

                    String line = null;
                    try {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            instream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    String ss = sb.toString();
                    //System.out.println("RESPONSE: " + ss);
                    instream.close();

                    // Thread.sleep(90000);

                    mainObject = parser.parse(ss).getAsJsonObject();
                    summa = mainObject.getAsJsonPrimitive("price").getAsString();

                    mainObject = parser.parse(ss).getAsJsonObject().getAsJsonObject("derival");
                    priceFrom = mainObject.getAsJsonPrimitive("price").getAsString();

                    try {
                        mainObject = parser.parse(ss).getAsJsonObject().getAsJsonObject("intercity");
                        intercity = mainObject.getAsJsonPrimitive("price").getAsString();
                    } catch (Exception e) {
                        intercity = "-";
                    }

                    try {
                        mainObject = parser.parse(ss).getAsJsonObject();
                        insuranceResponse = mainObject.getAsJsonPrimitive("insurance").getAsString();
                    } catch (Exception e) {
                        insuranceResponse = "-";
                    }

                    mainObject = parser.parse(ss).getAsJsonObject().getAsJsonObject("arrival");

                    priceTO = mainObject.get("price").getAsString();

                    Label label0 = new Label(0, i, from);
                    Label label1 = new Label(1, i, to);
                    Label label2 = new Label(2, i, fromExact);
                    Label label3 = new Label(3, i, toExact);
                    Label label4 = new Label(4, i, weight.toString());
                    Label label5 = new Label(5, i, volume.toString());
                    Label label6 = new Label(6, i, priceFrom);
                    Label label7 = new Label(7, i, intercity);
                    Label label8 = new Label(8, i, priceTO);
                    Label label9 = new Label(9, i, insuranceResponse);
                    Label label010 = new Label(10, i, summa);

                    writableSheet.addCell(label0);
                    writableSheet.addCell(label1);
                    writableSheet.addCell(label2);
                    writableSheet.addCell(label3);
                    writableSheet.addCell(label4);
                    writableSheet.addCell(label5);
                    writableSheet.addCell(label6);
                    writableSheet.addCell(label7);
                    writableSheet.addCell(label8);
                    writableSheet.addCell(label9);
                    writableSheet.addCell(label010);

                    //Запись рез-тов в таблицу ДЛ+VOZ
                    try {
                        if (count == 10) {
                            System.out.println(i);
                            count = 0;
                        } else count++;

                        //return;
                    } catch (Exception e) {
                        System.out.print("exc");
                    }


                } catch (Exception e) {
                    System.out.print("DoesntRecognized");
                    e.getMessage();
                    e.printStackTrace();
                    e.getStackTrace();

                }
            }

        } catch (Exception e) {
            System.out.print("exc2");
        } finally {
            writableWorkbook.write();
            writableWorkbook.close();
        }


    }

}
