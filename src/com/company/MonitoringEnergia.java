package com.company;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.JsonPath;
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
import java.util.List;

public class MonitoringEnergia {
    String inputPath = System.getProperty("user.dir");
    File exlFile = new File(inputPath + "\\input.xls");
    Workbook w;
    String insuranceResponse, intercity, exactCityFrom, exactCityTo;
    String noGabPrice;
    Double Len, Wid, Hei, wei, noGabWei, noGabVol;
    int amount;
    double costTotal;
    String otvozPrice;
    String zaborPrice;
    String MTPrice;
    HttpPost request;
    HttpGet requestGet;
    HttpResponse response;
    HttpEntity entity;
    InputStream instream;
    JsonArray mainObjectArray;
    JsonParser parser;
    HttpClient httpClient = HttpClientBuilder.create().build();
    int idTo;
    int idFrom;
    Cell cell;

    public static void main(String[] args) throws Exception {
        MonitoringEnergia http = new MonitoringEnergia();
        System.out.println("Testing 1 - Send Http GET request");
        http.sendGet();
    }

    private String getExactNameForNrg(String address) throws Exception {
        String exactCityName = address;
        request = new HttpPost("https://suggestions.dadata.ru/suggestions/api/4_1/rs/suggest/address");
        address="г "+address;
        System.out.println(address);
        StringEntity params = new StringEntity("{\"count\":2,\"query\":\"" + address + "\"}", "utf-8");
        request.addHeader("content-type", "application/json");
        request.addHeader("Authorization", "Token 84beb76a98914195f374779f2f313d31efca3c5d");
        request.addHeader("X-Secret", "cb82deee2d367b967ba569b5fc11b9e21a8c4832");
        request.addHeader("Accept", "application/json");
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
        } catch (UnsupportedOperationException e) {
            System.out.println("Город не распознан, укажите точнее, дописав область.");
            throw new Error();
        }
        return exactCityName;
    }


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
        Label label06 = new Label(6, 0, "ВЕС");
        Label label08 = new Label(8, 0, "");
        Label label09 = new Label(9, 0, "Забор");
        Label label010 = new Label(10, 0, "МТ");
        Label label011 = new Label(11, 0, "Отвоз");
        Label label012 = new Label(12, 0, "Негаб");
        Label label013 = new Label(13, 0, "ИТОГО");

        writableSheet.addCell(label00);
        writableSheet.addCell(label01);
        writableSheet.addCell(label02);
        writableSheet.addCell(label03);
        writableSheet.addCell(label04);
        writableSheet.addCell(label05);
        writableSheet.addCell(label06);
        //   writableSheet.addCell(label07);
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
                    //     totalWeight = 0.0;
                    //     totalVolume = 0.0;
                    insuranceResponse = "";
                    intercity = "";
                    exactCityFrom = "";
                    exactCityTo = "";


                    System.out.println(i + " ");


                    cell = sheet.getCell(3, i);
                    Len = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    if (Len < 0.1) Len = 0.1;

                    cell = sheet.getCell(4, i);
                    Wid = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    if (Wid < 0.1) Wid = 0.1;

                    cell = sheet.getCell(5, i);
                    Hei = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    if (Hei < 0.1) Hei = 0.1;

                    cell = sheet.getCell(6, i);
                    wei = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                    if (wei < 0.1) wei = 0.1;

                   /* cell = sheet.getCell(7, i);
                    amount = Integer.parseInt(cell.getContents());*/

               /*     cell = sheet.getCell(8, i); //obshii ves
                    if (cell.getContents().isEmpty()) {
                        totalWeight = amount * Double.parseDouble(sheet.getCell(6, i).getContents().replaceAll(",", "."));
                        totalWeight = new BigDecimal(totalWeight).setScale(1, RoundingMode.HALF_UP).doubleValue();
                        if (totalWeight < 0.1) totalWeight = 0.1;

                    } else {
                        totalWeight = new BigDecimal(cell.getContents().replaceAll(",", ".")).setScale(1, RoundingMode.HALF_UP).doubleValue();
                        if (totalWeight < 0.1) totalWeight = 0.1;
                    }
*/

                 /*   cell = sheet.getCell(9, i);
                    if (cell.getContents().isEmpty()) {
                        totalVolume = Double.parseDouble(sheet.getCell(3, i).getContents().replaceAll(",", ".")) * Double.parseDouble(sheet.getCell(4, i).getContents().replaceAll(",", ".")) * Double.parseDouble(sheet.getCell(5, i).getContents().replaceAll(",", ".")) * amount;
                        totalVolume = new BigDecimal(totalVolume).setScale(1, RoundingMode.HALF_UP).doubleValue();
                        if (totalVolume < 0.1) totalVolume = 0.1;

                    } else {
                        totalVolume = Double.parseDouble(cell.getContents().replaceAll(",", "."));
                        if (totalVolume < 0.1) totalVolume = 0.1;
                    }
*/
                    cell = sheet.getCell(1, i);
                    from = cell.getContents();
                    cell = sheet.getCell(2, i);
                    to = cell.getContents();
                    exactCityFrom = getExactNameForNrg(from);
                    exactCityTo = getExactNameForNrg(to);

                    noGabWei = 0.0;
                    noGabVol = 0.0;

                    //////////////////////////GET List of Cities
                    requestGet = new HttpGet("http://api2.nrg-tk.ru/v2/cities");
                    requestGet.addHeader("content-type", "application/javascript");

                    response = httpClient.execute(requestGet);
                    // entity = response.getEntity();

                    String responseAsString = EntityUtils.toString(response.getEntity());
                    // instream.close();

                    List<String> authors = JsonPath.read(responseAsString, "$.cityList[*].name");
                    List<Integer> id = JsonPath.read(responseAsString, "$.cityList[*].id");

                    if (authors.contains(exactCityFrom) && authors.contains(exactCityTo)) {
                        idFrom = id.get(authors.indexOf(exactCityFrom));
                        idTo = id.get(authors.indexOf(exactCityTo));
                    } else throw new Error();


                    //////////////////////////GET ID FROM
                    //String text = URLEncoder.encode(exactCityFrom, "UTF-8");
                    request = new HttpPost("http://api2.nrg-tk.ru/v2/price");
                    StringEntity params = new StringEntity("{\"cover\":0,\"idCurrency\":1,\"idCityFrom\":" + idFrom + ",\"idCityTo\":" + idTo + ",\"items\":[{\"weight\":" + wei + ",\"width\":" + Wid + ",\"length\":" + Len + ",\"height\":" + Hei + "}]}", "utf-8");
                  //  System.out.println(EntityUtils.toString(params));

                    request.addHeader("content-type", "application/json");
                    request.setEntity(params);
                    //    System.out.println(params);

                    response = httpClient.execute(request);
                    instream = response.getEntity().getContent();

                    responseAsString = EntityUtils.toString(response.getEntity());

                    instream.close();


                    JsonObject mainObject;
                    //  mainObject = parser.parse(responseAsString).getAsJsonObject();
                    try {
                        MTPrice = parser.parse(responseAsString).getAsJsonObject().getAsJsonArray("transfer").get(0).getAsJsonObject().get("price").getAsString();
                        zaborPrice = parser.parse(responseAsString).getAsJsonObject().getAsJsonObject("request").getAsJsonObject().get("price").getAsString();
                        otvozPrice = parser.parse(responseAsString).getAsJsonObject().getAsJsonObject("delivery").getAsJsonObject().get("price").getAsString();
                        try {
                            noGabPrice = parser.parse(responseAsString).getAsJsonObject().getAsJsonArray("transfer").get(0).getAsJsonObject().getAsJsonObject("oversize").get("price").getAsString();
                        } catch (Exception u) {
                            noGabPrice = "-";
                        }
                        //String author = JsonPath.using().read(responseAsString, "$.transfer[0].price");

                    } catch (Exception u) {
                        throw new Exception();
                    }


                    costTotal = Double.parseDouble(otvozPrice) + Double.parseDouble(zaborPrice) + Double.parseDouble(MTPrice) + Double.parseDouble(noGabPrice);


                    Label label0 = new Label(0, i, exactCityFrom);
                    Label label1 = new Label(1, i, exactCityTo);
                    Label label2 = new Label(2, i, Len.toString());
                    Label label3 = new Label(3, i, Wid.toString());
                    Label label4 = new Label(4, i, Hei.toString());
                    Label label5 = new Label(5, i, "");
                    //  Label label6 = new Label(6, i, totalWeight.toString());
                    //   Label label7 = new Label(7, i, totalVolume.toString());
                    Label label8 = new Label(6, i, wei.toString());
                    Label label9 = new Label(9, i, zaborPrice);
                    Label label10 = new Label(10, i, MTPrice);
                    Label label11 = new Label(11, i, otvozPrice);
                    Label label12 = new Label(12, i, noGabPrice);
                    Label label13 = new Label(13, i, String.valueOf(costTotal));


                    writableSheet.addCell(label0);
                    writableSheet.addCell(label1);
                    writableSheet.addCell(label2);
                    writableSheet.addCell(label3);
                    writableSheet.addCell(label4);
                    writableSheet.addCell(label5);
                    //    writableSheet.addCell(label6);
                    //   writableSheet.addCell(label7);
                    writableSheet.addCell(label8);
                    writableSheet.addCell(label9);
                    writableSheet.addCell(label10);
                    writableSheet.addCell(label11);
                    writableSheet.addCell(label12);
                    writableSheet.addCell(label13);
                    // System.out.println(costTotal);


                } catch (IndexOutOfBoundsException ar) {
                    Label label0 = new Label(0, i, from);
                    Label label1 = new Label(1, i, to);
                    Label label2 = new Label(2, i, Len.toString());
                    Label label3 = new Label(3, i, Wid.toString());
                    Label label4 = new Label(4, i, Hei.toString());
                    Label label6 = new Label(6, i, wei.toString());
                    Label label7 = new Label(9, i, "Нас. пункта не существует");
                    //    Label label7 = new Label(7, i, totalVolume.toString());
                    writableSheet.addCell(label0);
                    writableSheet.addCell(label1);
                    writableSheet.addCell(label2);
                    writableSheet.addCell(label3);
                    writableSheet.addCell(label4);
                    writableSheet.addCell(label6);
                    writableSheet.addCell(label7);
                    //  writableSheet.addCell(label7);
                    //ar.getMessage();
                    // ar.printStackTrace();
                    //ar.getStackTrace();

                } catch (Exception e) {
                    Label label0 = new Label(0, i, from);
                    Label label1 = new Label(1, i, to);
                    Label label2 = new Label(2, i, Len.toString());
                    Label label3 = new Label(3, i, Wid.toString());
                    Label label4 = new Label(4, i, Hei.toString());
                    Label label6 = new Label(6, i, wei.toString());
                    Label label7 = new Label(9, i, "Город не распознан, укажите точнее, дописав область.");
                    //  Label label7 = new Label(7, i, totalVolume.toString());
                    writableSheet.addCell(label0);
                    writableSheet.addCell(label1);
                    writableSheet.addCell(label2);
                    writableSheet.addCell(label3);
                    writableSheet.addCell(label4);
                    writableSheet.addCell(label6);
                    writableSheet.addCell(label7);
                    //    writableSheet.addCell(label7);
                    //  System.out.print("DoesntRecognized");
                    //  e.getMessage();
                    //   e.printStackTrace();
                    //    e.getStackTrace();

                }
                catch (Error e) {
                    Label label0 = new Label(0, i, from);
                    Label label1 = new Label(1, i, to);
                    Label label2 = new Label(2, i, Len.toString());
                    Label label3 = new Label(3, i, Wid.toString());
                    Label label4 = new Label(4, i, Hei.toString());
                    Label label6 = new Label(6, i, wei.toString());
                    Label label7 = new Label(9, i, "Данное направление не обслуживается");
                    //  Label label7 = new Label(7, i, totalVolume.toString());
                    writableSheet.addCell(label0);
                    writableSheet.addCell(label1);
                    writableSheet.addCell(label2);
                    writableSheet.addCell(label3);
                    writableSheet.addCell(label4);
                    writableSheet.addCell(label6);
                    writableSheet.addCell(label7);
                    //    writableSheet.addCell(label7);
                    //  System.out.print("DoesntRecognized");
                    //  e.getMessage();
                    //   e.printStackTrace();
                    //    e.getStackTrace();

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
