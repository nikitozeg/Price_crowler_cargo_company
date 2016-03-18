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
import java.net.URLEncoder;

public class VozovozTariff {
    String inputPath = System.getProperty("user.dir");
    File exlFile = new File(inputPath + "\\input.xls");
    Workbook w;
    String insuranceResponse, intercity, fromCityNameForVozovoz, toCityNameForVozovoz, costTotal;
    Double totalWeight, totalVolume, maxLen, maxWid, maxHei, zabor, mt, otvoz, itogo;
    Boolean zaborEnabled, otvozEnabled;
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
        VozovozTariff http = new VozovozTariff();
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

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
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
        Label label02 = new Label(2, 0, "");
        Label label03 = new Label(3, 0, "Забор");
        Label label05 = new Label(4, 0, "Отвоз");
        Label label06 = new Label(5, 0, "НДС");
        Label label07 = new Label(7, 0, "");
        Label label08 = new Label(8, 0, "до 100");
        Label label09 = new Label(8, 1, "забор");
        Label label010 = new Label(9, 1, "мт");
        Label label011 = new Label(10, 1, "отвоз");
        Label label012 = new Label(11, 1, "ИТОГО");
        Label label013 = new Label(12, 0, "100-200");
        Label label014 = new Label(12, 1, "забор");
        Label label015 = new Label(13, 1, "мт");
        Label label016 = new Label(14, 1, "отвоз");
        Label label017 = new Label(15, 1, "ИТОГО");
        Label label018 = new Label(16, 0, "200-300");
        Label label019 = new Label(16, 1, "забор");
        Label label020 = new Label(17, 1, "мт");
        Label label021 = new Label(18, 1, "отвоз");
        Label label022 = new Label(19, 1, "ИТОГО");
        Label label023 = new Label(20, 0, "300-400");
        Label label024 = new Label(20, 1, "забор");
        Label label025 = new Label(21, 1, "мт");
        Label label026 = new Label(22, 1, "отвоз");
        Label label027 = new Label(23, 1, "ИТОГО");
        Label label028 = new Label(24, 0, "400-500");
        Label label029 = new Label(24, 1, "забор");
        Label label030 = new Label(25, 1, "мт");
        Label label031 = new Label(26, 1, "отвоз");
        Label label032 = new Label(27, 1, "ИТОГО");
        Label label033 = new Label(28, 0, "500-600");
        Label label034 = new Label(28, 1, "забор");
        Label label035 = new Label(29, 1, "мт");
        Label label036 = new Label(30, 1, "отвоз");
        Label label037 = new Label(31, 1, "ИТОГО");
        Label label038 = new Label(32, 0, "600-800");
        Label label039 = new Label(32, 1, "забор");
        Label label040 = new Label(33, 1, "мт");
        Label label041 = new Label(34, 1, "отвоз");
        Label label042 = new Label(35, 1, "ИТОГО");
        Label label043 = new Label(36, 0, "800-1000");
        Label label044 = new Label(36, 1, "забор");
        Label label045 = new Label(37, 1, "мт");
        Label label046 = new Label(38, 1, "отвоз");
        Label label047 = new Label(39, 1, "ИТОГО");
        Label label048 = new Label(40, 0, "1000-1200");
        Label label049 = new Label(40, 1, "забор");
        Label label050 = new Label(41, 1, "мт");
        Label label051 = new Label(42, 1, "отвоз");
        Label label052 = new Label(43, 1, "ИТОГО");
        Label label053 = new Label(44, 0, "1200-1500");
        Label label054 = new Label(44, 1, "забор");
        Label label055 = new Label(45, 1, "мт");
        Label label056 = new Label(46, 1, "отвоз");
        Label label057 = new Label(47, 1, "ИТОГО");

        writableSheet.addCell(label00);
        writableSheet.addCell(label01);
        writableSheet.addCell(label02);
        writableSheet.addCell(label03);
        writableSheet.addCell(label05);
        writableSheet.addCell(label06);
        writableSheet.addCell(label07);
        writableSheet.addCell(label08);
        writableSheet.addCell(label09);
        writableSheet.addCell(label010);
        writableSheet.addCell(label011);
        writableSheet.addCell(label012);
        writableSheet.addCell(label013);
        writableSheet.addCell(label014);
        writableSheet.addCell(label015);
        writableSheet.addCell(label016);
        writableSheet.addCell(label017);
        writableSheet.addCell(label018);
        writableSheet.addCell(label019);
        writableSheet.addCell(label020);
        writableSheet.addCell(label021);
        writableSheet.addCell(label022);
        writableSheet.addCell(label023);
        writableSheet.addCell(label024);
        writableSheet.addCell(label025);
        writableSheet.addCell(label026);
        writableSheet.addCell(label027);
        writableSheet.addCell(label028);
        writableSheet.addCell(label029);
        writableSheet.addCell(label030);
        writableSheet.addCell(label031);
        writableSheet.addCell(label032);
        writableSheet.addCell(label033);
        writableSheet.addCell(label034);
        writableSheet.addCell(label035);
        writableSheet.addCell(label036);
        writableSheet.addCell(label037);
        writableSheet.addCell(label038);
        writableSheet.addCell(label039);
        writableSheet.addCell(label040);
        writableSheet.addCell(label041);
        writableSheet.addCell(label042);
        writableSheet.addCell(label043);
        writableSheet.addCell(label044);
        writableSheet.addCell(label045);
        writableSheet.addCell(label046);
        writableSheet.addCell(label047);
        writableSheet.addCell(label048);
        writableSheet.addCell(label049);
        writableSheet.addCell(label050);
        writableSheet.addCell(label051);
        writableSheet.addCell(label052);
        writableSheet.addCell(label053);
        writableSheet.addCell(label054);
        writableSheet.addCell(label055);
        writableSheet.addCell(label056);
        writableSheet.addCell(label057);

        writableSheet.mergeCells(8, 0, 11, 0);
        writableSheet.mergeCells(12, 0, 15, 0);
        writableSheet.mergeCells(16, 0, 19, 0);
        writableSheet.mergeCells(20, 0, 23, 0);
        writableSheet.mergeCells(24, 0, 27, 0);
        writableSheet.mergeCells(28, 0, 31, 0);
        writableSheet.mergeCells(32, 0, 35, 0);
        writableSheet.mergeCells(36, 0, 39, 0);
        writableSheet.mergeCells(40, 0, 43, 0);
        writableSheet.mergeCells(44, 0, 47, 0);

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
                    zaborEnabled = !(cell.getContents().isEmpty());

                    cell = sheet.getCell(4, i);
                    otvozEnabled = !(cell.getContents().isEmpty());

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

                    totalWeight = 99.0;

                    Label label0 = new Label(0, i + 1, fromCityNameForVozovoz);
                    Label label1 = new Label(1, i + 1, toCityNameForVozovoz);

                    writableSheet.addCell(label0);
                    writableSheet.addCell(label1);
                    if (zaborEnabled) {
                        Label label2 = new Label(3, i + 1, "+");
                        writableSheet.addCell(label2);
                    }

                    if (otvozEnabled) {
                        Label label3 = new Label(4, i + 1, "+");
                        writableSheet.addCell(label3);
                    }


                    /////////////Getprice Vozovoz.ru
                    for (int k = 0; k < 40; k = k + 4) {
                        if (k == 4) totalWeight = 100.0;

                        maxHei = 0.1;
                        maxLen = 0.1;
                        maxWid = 0.1;
                        amount = 1;
                        totalVolume = 0.1;

                        request = new HttpPost("https://vozovoz.ru/api/v1/orders/price");

                        StringEntity params = new StringEntity("{\"status\":{},\"save\":false,\"services\":[{\"type\":\"shipping\",\"counteragents\":{\"consignee\":{\"type\":\"individual\"," +
                                "\"needCargoReceiptCode\":true,\"phoneNumbers\":[]},\"shipper\":{\"type\":\"individual\"," +
                                "\"needCargoReceiptCode\":false,\"phoneNumbers\":[]},\"payer\":{\"type\":\"individual\",\"phoneNumbers\":[]}}," +
                                "\"cargo\":{\"hasCorrespondence\":false,\"packages\":{\"bag1\":0,\"bag2\":0,\"box1\":0,\"box2\":0,\"box3\":0,\"box4\":0,\"safePackage\":0," +
                                "\"sealPackage\":0,\"bubbleFilmVolume\":0,\"extraPackageVolume\":0,\"hardPackageVolume\":0},\"total\":{\"all\":{\"quantity\":" + amount + ",\"volume\":" + totalVolume + "," +
                                "\"weight\":" + totalWeight + "},\"max\":{\"height\":" + maxHei + ",\"length\":" + maxLen + ",\"width\":" + maxWid + ",\"weight\":" + totalWeight + "},\"noGab\":{\"volume\":" + 0 + ",\"weight\":" + 0 + "}}}},{\"type\":\"deliveryFrom\"," +
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
                                                shipping = "0";
                                            }
                                        case "\"deliveryFrom\"":
                                            if (zaborEnabled)
                                                deliveryFrom = mainObject1.getAsJsonArray("services").get(j).getAsJsonObject().getAsJsonObject("cost").getAsJsonPrimitive("total").toString();
                                            else deliveryFrom = "0";
                                            break;
                                        case "\"deliveryTo\"":
                                            if (otvozEnabled)
                                                deliveryTo = mainObject1.getAsJsonArray("services").get(j).getAsJsonObject().getAsJsonObject("cost").getAsJsonPrimitive("total").toString();
                                            else deliveryTo = "0";
                                            break;

                                    }
                                } catch (Exception e) {

                                }

                            }

                        } catch (Exception e) {
                            Label label12 = new Label(9, i + 1, responseAsString);
                            writableSheet.addCell(label12);
                            System.out.println(responseAsString);
                            throw new Error();
                        }

                        if (k == 0) totalWeight = totalWeight + 1;
                        zabor = Double.valueOf(deliveryFrom) / totalWeight;
                        mt = Double.valueOf(shipping) / totalWeight;
                        otvoz = Double.valueOf(deliveryTo) / totalWeight;

                        Label label8 = new Label(8 + k, i + 1, String.valueOf(round(zabor, 2)));
                        Label label9 = new Label(9 + k, i + 1, String.valueOf(round(mt, 2)));
                        Label label10 = new Label(10 + k, i + 1, String.valueOf(round(otvoz, 2)));
                        Label label11 = new Label(11 + k, i + 1, String.valueOf(round(zabor + mt + otvoz, 2)));

                        if (!(zabor == 0.0)) {
                            writableSheet.addCell(label8);
                        }
                        writableSheet.addCell(label9);
                        if (!(otvoz == 0.0)) {
                            writableSheet.addCell(label10);
                        }
                        writableSheet.addCell(label11);
                        // System.out.println(costTotal);
                        if (k >= 24) {
                            totalWeight = totalWeight + 200.0;
                        } else
                            totalWeight = totalWeight + 100.0;
                    }

                } catch (Exception e) {
                    System.out.print("DoesntRecognized");
                    e.getMessage();
                    e.printStackTrace();
                    e.getStackTrace();

                } catch (Error ar) {
                    Label label0 = new Label(0, i + 1, fromCityNameForVozovoz);
                    Label label1 = new Label(1, i + 1, toCityNameForVozovoz);

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
