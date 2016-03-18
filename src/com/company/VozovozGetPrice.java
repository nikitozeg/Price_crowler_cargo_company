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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;

public class VozovozGetPrice {
    String inputPath = System.getProperty("user.dir");
    File exlFile = new File(inputPath + "\\input.xls");
    Workbook w;
    String insuranceResponse, intercity, fromCityNameForVozovoz, toCityNameForVozovoz, costTotal;
    Double totalWeight, totalVolume, maxLen, maxWid, maxHei, maxWei, noGabWei, noGabVol;
    int amount;
    String shipping;
    String deliveryFrom;
    String deliveryTo;
    HttpPost request;
    HttpGet requestGet;
    HttpResponse response;
    HttpEntity entity;
    InputStream instream;
    JsonArray mainObjectArray;
    JsonParser parser;
    HttpClient httpClient = HttpClientBuilder.create().build();
    String idTo = "";
    String idFrom = "";

    public static void main(String[] args) throws Exception {
        VozovozGetPrice http = new VozovozGetPrice();
        System.out.println("Testing 1 - Send Http GET request");
        http.sendGet();
    }


    private String getExactCityName(String address, int k) throws Exception {
        String exactCityName = address;
        request = new HttpPost("https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address");
        System.out.println(address);
        StringEntity params = new StringEntity("{\"count\":2,\"query\":\"" + address + "\"}", "utf-8");
        request.addHeader("content-type", "application/json");
        request.addHeader("Authorization", "Token 84beb76a98914195f374779f2f313d31efca3c5d");
        request.addHeader("X-Secret", "cb82deee2d367b967ba569b5fc11b9e21a8c4832");
        request.setEntity(params);


        response = httpClient.execute(request);
        entity = response.getEntity();
        instream = entity.getContent();

        String responseAsString = EntityUtils.toString(response.getEntity());


        parser = new JsonParser();//response.toString()
        mainObjectArray = parser.parse(responseAsString.toString()).getAsJsonObject().getAsJsonArray("suggestions");
        //System.out.println(mainObject.get(0).getAsJsonObject().getAsJsonObject("data").get("kladr_id").getAsString());

        try {
            exactCityName = mainObjectArray.get(0).getAsJsonObject().getAsJsonObject("data").get("city").getAsString();
            exactCityName = exactCityName + " (" + mainObjectArray.get(0).getAsJsonObject().getAsJsonObject("data").get("region").getAsString() + ") ";
        } catch (UnsupportedOperationException e) {
            try {
                System.out.println("null city");
                exactCityName = mainObjectArray.get(0).getAsJsonObject().getAsJsonObject("data").get("settlement").getAsString();
                exactCityName = exactCityName + " (" + mainObjectArray.get(0).getAsJsonObject().getAsJsonObject("data").get("region").getAsString() + ")";
            } catch (Exception ee) {

                System.out.println("cant recognized");
            }
        } catch (IndexOutOfBoundsException eee) {
            try {
                System.out.println("! Не удалось распознать " + address + ": допишите к названию тип нас. пункта или его регион");
                exactCityName = "Не удалось распознать " + address + ": допишите к названию тип нас. пункта или его регион";
                // throw new Exception ();
            } catch (Exception ee) {
                System.out.println("shouldnt to be");
            }
        }
        //check settlment, if exist, write it
        try {
            exactCityName = mainObjectArray.get(0).getAsJsonObject().getAsJsonObject("data").get("settlement").getAsString();
        } catch (Exception e) {
        }

        return exactCityName;

    }

    //public String getConvertedParameters()


    public void sendGet() throws Exception {
        File crowlerResult = new File(inputPath + "\\output.xls");
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setSuppressWarnings(true);
        w = Workbook.getWorkbook(exlFile, wbSettings);
        Sheet sheet = w.getSheet(0);
        WritableWorkbook writableWorkbook = Workbook.createWorkbook(crowlerResult);
        WritableSheet writableSheet = writableWorkbook.createSheet("Sheet2", 0);

        Label label00 = new Label(0, 0, "От");
        Label label01 = new Label(1, 0, "До");
        Label label02 = new Label(2, 0, "Длина_макс");
        Label label03 = new Label(3, 0, "Ширина_макс");
        Label label04 = new Label(4, 0, "Высота_макс");
        Label label05 = new Label(5, 0, "");
        Label label06 = new Label(6, 0, "Общий ВЕС");
        Label label07 = new Label(7, 0, "Общий ОБЪЕМ");
        Label label08 = new Label(8, 0, "");
        Label label09 = new Label(9, 0, "Забор");
        Label label010 = new Label(10, 0, "МТ");
        Label label011 = new Label(11, 0, "Отвоз");
        Label label012 = new Label(12, 0, "");
        Label label013 = new Label(13, 0, "ИТОГО");

        writableSheet.addCell(label00);
        writableSheet.addCell(label01);
        writableSheet.addCell(label02);
        writableSheet.addCell(label03);
        writableSheet.addCell(label04);
        writableSheet.addCell(label05);
        writableSheet.addCell(label06);
        writableSheet.addCell(label07);
        writableSheet.addCell(label08);
        writableSheet.addCell(label09);
        writableSheet.addCell(label010);
        writableSheet.addCell(label011);
        writableSheet.addCell(label012);
        writableSheet.addCell(label013);


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
                    totalWeight = 0.0;
                    totalVolume = 0.0;
                    insuranceResponse = "";
                    intercity = "";
                    fromCityNameForVozovoz = "";
                    toCityNameForVozovoz = "";


                    System.out.println(i + " ");
                    Cell cell = sheet.getCell(1, i);
                    from = cell.getContents();
                    fromCityNameForVozovoz = getExactCityName(from, i);


                    cell = sheet.getCell(2, i);
                    to = cell.getContents();
                    toCityNameForVozovoz = getExactCityName(to, i);

                    cell = sheet.getCell(3, i);
                    maxLen = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    if (maxLen < 0.1) maxLen = 0.1;

                    cell = sheet.getCell(4, i);
                    maxWid = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    if (maxWid < 0.1) maxWid = 0.1;

                    cell = sheet.getCell(5, i);
                    maxHei = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    if (maxHei < 0.1) maxHei = 0.1;

                    cell = sheet.getCell(6, i);
                    maxWei = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    if (maxWei < 0.1) maxWei = 0.1;

                    cell = sheet.getCell(7, i);
                    amount = Integer.parseInt(cell.getContents());

                    cell = sheet.getCell(8, i); //obshii ves
                    if (cell.getContents().isEmpty()) {
                        totalWeight = amount * Double.parseDouble(sheet.getCell(6, i).getContents().replaceAll(",", "."));
                        //  System.out.println(totalWeight);
                        totalWeight = new BigDecimal(totalWeight).setScale(1, RoundingMode.HALF_UP).doubleValue();
                        if (totalWeight < 0.1) totalWeight = 0.1;

                    } else {
                        totalWeight = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                        if (totalWeight < 0.1) totalWeight = 0.1;
                    }


                    cell = sheet.getCell(9, i);
                    if (cell.getContents().isEmpty()) {
                        totalVolume = Double.parseDouble(sheet.getCell(3, i).getContents().replaceAll(",", ".")) * Double.parseDouble(sheet.getCell(4, i).getContents().replaceAll(",", ".")) * Double.parseDouble(sheet.getCell(5, i).getContents().replaceAll(",", ".")) * amount;
                        totalVolume = new BigDecimal(totalVolume).setScale(1, RoundingMode.HALF_UP).doubleValue();
                        if (totalVolume < 0.1) totalVolume = 0.1;

                    } else {
                        totalVolume = Double.parseDouble(cell.getContents().replaceAll(",", "."));
                        if (totalVolume < 0.1) totalVolume = 0.1;
                    }


                  /*  cell = sheet.getCell(10, i);
                    if (cell.getContents().isEmpty()) {
                        noGabWei = 0.0;
                    } else
                        noGabWei = Double.parseDouble(cell.getContents().replaceAll(",", "."));

                    cell = sheet.getCell(11, i);
                    if (cell.getContents().isEmpty()) {
                        noGabVol = 0.0;
                    } else
                        noGabVol = Double.parseDouble(cell.getContents().replaceAll(",", "."));
*/
                    noGabWei = 0.0;
                    noGabVol = 0.0;
                    //   cell = sheet.getCell(32, i); //insurance
                    //      insurance = Double.parseDouble(cell.getContents().replaceAll(",", "."));


                    //////////////////////////GET ID FROM
                    String text = URLEncoder.encode(fromCityNameForVozovoz, "UTF-8");
                    requestGet = new HttpGet("https://vozovoz.ru/api/v1/locations/autocomplete?query=" + text);
                    requestGet.addHeader("content-type", "application/javascript");

                    response = httpClient.execute(requestGet);
                    entity = response.getEntity();

                    String responseAsString = EntityUtils.toString(response.getEntity());

                    instream.close();

                    JsonArray mainObject;
                    mainObject = parser.parse(responseAsString).getAsJsonObject().getAsJsonArray("data");
                    try {
                        idFrom = mainObject.get(0).getAsJsonObject().get("id").getAsString();
                        if (fromCityNameForVozovoz.equalsIgnoreCase(from)) {
                            fromCityNameForVozovoz = fromCityNameForVozovoz + " (" + mainObject.get(0).getAsJsonObject().getAsJsonArray("regions").get(0).getAsJsonObject().get("name").getAsString() + ")";
                        }
                    } catch (IndexOutOfBoundsException u) {
                        if (fromCityNameForVozovoz.substring(0, 7).equalsIgnoreCase("Не удал"))
                            throw new Error();
                        else {
                            fromCityNameForVozovoz = "Не возим из " + fromCityNameForVozovoz;
                            throw new Error();
                        }
                    }

                    //////////////////////////GET ID TO
                    text = URLEncoder.encode(toCityNameForVozovoz, "UTF-8");
                    requestGet = new HttpGet("https://vozovoz.ru/api/v1/locations/autocomplete?query=" + text);
                    requestGet.addHeader("content-type", "application/javascript");

                    response = httpClient.execute(requestGet);
                    entity = response.getEntity();

                    responseAsString = EntityUtils.toString(response.getEntity());

                    instream.close();

                    mainObject = parser.parse(responseAsString).getAsJsonObject().getAsJsonArray("data");
                    try {
                        idTo = mainObject.get(0).getAsJsonObject().get("id").getAsString();
                        if (toCityNameForVozovoz.equalsIgnoreCase(to)) {
                            toCityNameForVozovoz = toCityNameForVozovoz + " (" + mainObject.get(0).getAsJsonObject().getAsJsonArray("regions").get(0).getAsJsonObject().get("name").getAsString() + ")";
                        }
                    } catch (IndexOutOfBoundsException u) {
                        if (toCityNameForVozovoz.substring(0, 7).equalsIgnoreCase("Не удал"))
                            throw new Error();
                        else {
                            toCityNameForVozovoz = "Не возим в " + toCityNameForVozovoz;
                            throw new Error();
                        }
                    }

                    /////////////Getprice Vozovoz.ru
                    request = new HttpPost("https://vozovoz.ru/api/v1/orders/price");

                    StringEntity params = new StringEntity("{\"status\":{},\"save\":false,\"services\":[{\"type\":\"shipping\",\"counteragents\":{\"consignee\":{\"type\":\"individual\"," +
                            "\"needCargoReceiptCode\":true,\"phoneNumbers\":[]},\"shipper\":{\"type\":\"individual\"," +
                            "\"needCargoReceiptCode\":false,\"phoneNumbers\":[]},\"payer\":{\"type\":\"individual\",\"phoneNumbers\":[]}}," +
                            "\"cargo\":{\"hasCorrespondence\":false,\"packages\":{\"bag1\":0,\"bag2\":0,\"box1\":0,\"box2\":0,\"box3\":0,\"box4\":0,\"safePackage\":0," +
                            "\"sealPackage\":0,\"bubbleFilmVolume\":0,\"extraPackageVolume\":0,\"hardPackageVolume\":0},\"total\":{\"all\":{\"quantity\":" + amount + ",\"volume\":" + totalVolume + "," +
                            "\"weight\":" + totalWeight + "},\"max\":{\"height\":" + maxHei + ",\"length\":" + maxLen + ",\"width\":" + maxWid + ",\"weight\":" + maxWei + "},\"noGab\":{\"volume\":" + 0 + ",\"weight\":" + 0 + "}}}},{\"type\":\"deliveryFrom\"," +
                            "\"from\":{\"id\":\"" + idFrom + "\",\"name\":\"Санкт-Петербург\",\"type\":\"г\",\"timezone\":3,\"address\":{\"address\":\"3\"," +
                            "\"dates\":{\"from\":\"2015-11-05T09:00:00.000\",\"to\":\"2015-11-05T13:00:00.000\"}}}},{\"type\":\"deliveryTo\",\"to\":{\"id\":\"" + idTo + "\"," +
                            "\"name\":\"Москва\",\"type\":\"г\",\"timezone\":3,\"address\":{\"address\":\"3\",\"dates\":{\"from\":\"2015-11-06T14:00:00.000\",\"to\":\"2015-11-06T18:00:00.000\"}}}}]}");


                    request.addHeader("content-type", "application/json");
                    request.setEntity(params);

                    response = httpClient.execute(request);
                    entity = response.getEntity();
                    instream = entity.getContent();

                    responseAsString = EntityUtils.toString(response.getEntity());

                    instream.close();
                    JsonObject mainObject1 = parser.parse(responseAsString).getAsJsonObject().getAsJsonObject("data");

                    try {

                        if (responseAsString.contains("code")) {
                            if (responseAsString.contains("needCargoReceiptCode") == false)
                                throw new Exception();
                        }

                        costTotal = mainObject1.getAsJsonObject("cost").getAsJsonPrimitive("total").toString();
                        int j;

                        for (j = 0; j <= 2; j++) {
                            try {
                                String temp = mainObject1.getAsJsonArray("services").get(j).getAsJsonObject().getAsJsonPrimitive("type").toString();
                                switch (temp) {
                                    case "\"shipping\"":
                                        try {
                                            shipping = mainObject1.getAsJsonArray("services").get(j).getAsJsonObject().getAsJsonObject("cost").getAsJsonPrimitive("total").toString();
                                            break;
                                        } catch (Exception e) {
                                            shipping = "-";
                                        }
                                    case "\"deliveryFrom\"":
                                        deliveryFrom = mainObject1.getAsJsonArray("services").get(j).getAsJsonObject().getAsJsonObject("cost").getAsJsonPrimitive("total").toString();
                                        break;
                                    case "\"deliveryTo\"":
                                        deliveryTo = mainObject1.getAsJsonArray("services").get(j).getAsJsonObject().getAsJsonObject("cost").getAsJsonPrimitive("total").toString();
                                        break;

                                }
                            } catch (Exception e) {

                            }

                        }

                    } catch (Exception e) {
                        Label label0 = new Label(0, i, fromCityNameForVozovoz);
                        Label label1 = new Label(1, i, toCityNameForVozovoz);
                        Label label12 = new Label(9, i, responseAsString);
                        writableSheet.addCell(label12);
                        writableSheet.addCell(label0);
                        writableSheet.addCell(label1);
                        System.out.println(responseAsString);
                        throw new Error();

                    }

                    Label label0 = new Label(0, i, fromCityNameForVozovoz);
                    Label label1 = new Label(1, i, toCityNameForVozovoz);
                    Label label2 = new Label(2, i, maxLen.toString());
                    Label label3 = new Label(3, i, maxWid.toString());
                    Label label4 = new Label(4, i, maxHei.toString());
                    Label label5 = new Label(6, i, totalWeight.toString());
                    Label label6 = new Label(7, i, totalVolume.toString());
                    Label label7 = new Label(8, i, "");
                    Label label8 = new Label(9, i, deliveryFrom);
                    Label label9 = new Label(10, i, shipping);
                    Label label10 = new Label(11, i, deliveryTo);
                    Label label11 = new Label(12, i, "");
                    Label label12 = new Label(13, i, costTotal);

                  /*  Label label00 = new Label(0, 0, "От");
                    Label label01 = new Label(1, 0, "До");
                    Label label02 = new Label(2, 0, "Длина_макс");
                    Label label03 = new Label(3, 0, "Ширина_макс");
                    Label label04 = new Label(4, 0, "Высота_макс");
                    Label label05 = new Label(5, 0, "");
                    Label label06 = new Label(6, 0, "Общий ВЕС");
                    Label label07 = new Label(7, 0, "Общий ОБЪЕМ");
                    Label label08 = new Label(8, 0, "");
                    Label label09 = new Label(9, 0, "Забор");
                    Label label010 = new Label(10, 0, "МТ");
                    Label label011 = new Label(11, 0, "Отвоз");
                    Label label012 = new Label(12, 0, "");
                    Label label013 = new Label(13, 0, "ИТОГО");
                    */

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
                    writableSheet.addCell(label10);
                    writableSheet.addCell(label11);
                    writableSheet.addCell(label12);
                    // System.out.println(costTotal);


                } catch (Exception e) {
                    System.out.print("DoesntRecognized");
                    e.getMessage();
                    e.printStackTrace();
                    e.getStackTrace();

                } catch (Error ar) {
                    Label label0 = new Label(0, i, fromCityNameForVozovoz);
                    Label label1 = new Label(1, i, toCityNameForVozovoz);

                    writableSheet.addCell(label0);
                    writableSheet.addCell(label1);
                    ar.getMessage();
                    ar.printStackTrace();
                    ar.getStackTrace();

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
