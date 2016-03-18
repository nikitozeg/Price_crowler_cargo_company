package com.company;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.thoughtworks.selenium.SeleniumException;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;

public class MonitoringPecom {
    Double length, width, height, weight, volume, insurancecost, maxSize;
    Boolean isInsurance, isPickUp, isDelivery;

    InputStream instream;
    BufferedReader reader;
    HttpResponse response;
    String responseAsString;
    JsonObject mainObject;

    private String transportingType;
    private String priceTotal;
    private String priceMT;
    private String insuranceResponse;
    private String cityFrom;
    private String cityTo;

    public static void main(String[] args) throws Exception {
        try {
            MonitoringPecom http = new MonitoringPecom();
            System.out.println("Testing 1 - Send Http GET request");
            http.sendGet();
        } catch (Exception e) {
            BufferedReader brr = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("ОШИБКА1 ");

             e.getMessage();
             e.printStackTrace();
             e.getStackTrace();
            Integer.parseInt(brr.readLine());
        }

    }

    public void printResponse() throws IOException {

        responseAsString = EntityUtils.toString(response.getEntity());


    }

    private void sendGet() throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();
        JsonParser parser = new JsonParser();
        String inputPath = System.getProperty("user.dir");
        String fromId, toId, priceFrom = "", priceTO = "", km = "", OblPerevozka = "";
        File crowlerResult = new File(inputPath + "\\outputPECOM.xls");
        WritableWorkbook writableWorkbook = Workbook.createWorkbook(crowlerResult);
        WritableSheet writableSheet = writableWorkbook.createSheet("Sheet2", 0);

        Label label0 = new Label(0, 0, "ОТ_входной");
        Label label1 = new Label(1, 0, "ДО_входной");
        Label label2 = new Label(2, 0, "ОТ_вычисленный");
        Label label3 = new Label(3, 0, "ДО_вычисленный");
        Label label4 = new Label(4, 0, "МТ");
        Label label5 = new Label(5, 0, "Забор");
        Label label6 = new Label(6, 0, "Отвоз");
        Label label7 = new Label(7, 0, "Километраж");
        Label label8 = new Label(8, 0, "Областная перевозка");
        Label label9 = new Label(9, 0, "ИТОГО");
        Label label10 = new Label(10, 0, "Вес");
        Label label11 = new Label(11, 0, "Объем");


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


        String to = "", from = "";

        try {
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setSuppressWarnings(true);
            File exlFilee = new File(inputPath + "\\inputPECOM.xls");
            Workbook ww = Workbook.getWorkbook(exlFilee, wbSettings);
            Sheet sheett = ww.getSheet(0);
            int enteredNumber = 2;
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Введите количество обрабатываемых строк:");
            try {
                enteredNumber = Integer.parseInt(br.readLine());
            } catch (NumberFormatException nfe) {
                System.err.println("Неверный формат");
                Thread.sleep(3000);
            }
            for (int i = 1; i < enteredNumber; i++) {//3650
                try {
                    System.out.print(i + ") ");
                    Cell cell = sheett.getCell(1, i);
                    from = cell.getContents().toString();

                    if (from.equalsIgnoreCase("москва")) from = "москва восток";
                    System.out.print(cell.getContents().toString() + "-->");

                    cell = sheett.getCell(2, i);
                    to = cell.getContents().toString();

                    if (to.equalsIgnoreCase("москва")) to = "москва восток";
                    System.out.println(cell.getContents().toString());


                    cell = sheett.getCell(3, i);
                    if (cell.getContents().toString().equalsIgnoreCase("да"))
                        isPickUp = true;
                    else isPickUp = false;


                    cell = sheett.getCell(4, i); //volume
                    if (cell.getContents().toString().equalsIgnoreCase("да"))
                        isDelivery = true;
                    else isDelivery = false;

                    cell = sheett.getCell(5, i); //ves
                    length = Double.parseDouble(cell.getContents().toString().replaceAll(",", "."));

                    cell = sheett.getCell(6, i); //ves
                    width = Double.parseDouble(cell.getContents().toString().replaceAll(",", "."));

                    cell = sheett.getCell(7, i); //ves
                    height = Double.parseDouble(cell.getContents().toString().replaceAll(",", "."));


                    maxSize = Math.max(length, width);
                    maxSize = Math.max(maxSize, height);


                    cell = sheett.getCell(10, i); //ves
                    weight = Double.parseDouble(cell.getContents().toString().replaceAll(",", "."));

                    cell = sheett.getCell(11, i); //volume
                    volume = Double.parseDouble(cell.getContents().toString().replaceAll(",", "."));


                    cell = sheett.getCell(32, i); //volume
                    //  System.out.print(cell.getContents().toString().replaceAll(",", "."));
                    insurancecost = Double.parseDouble(cell.getContents().toString().replaceAll(",", "."));

                    if (insurancecost.equals("0")) {
                        isInsurance = false;
                        insurancecost = 0.0;
                    } else {
                        isInsurance = true;
                    }

                } catch (SeleniumException e) {
                    System.out.print("DoesntRecognized");
                }

/////////////////////////////////////////////////////////////////////////////////////////

                HttpPost request = new HttpPost("https://kabinet.pecom.ru/api/v1/branches/findbytitle/");
                request.addHeader("Authorization", "Basic bmlraXRvemVnZzowMUQwQUFFOTJGQTRBNTRFQUI5RkU2NTJFQzBGNTFGQzY0QjFCNTI3AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                request.addHeader("Content-Type", "application/json;charset=utf-8");
                request.setEntity(new StringEntity("{title: \"" + from + "\"}", "UTF-8"));
                response = httpClient.execute(request);

                instream = response.getEntity().getContent();
                reader = new BufferedReader(new InputStreamReader(instream));
                printResponse();


                try {
                    mainObject = parser.parse(responseAsString).getAsJsonObject().getAsJsonArray("items").get(0).getAsJsonObject();

                    if (mainObject.get("cityId").isJsonNull()) {
                        fromId = mainObject.get("branchId").getAsString();
                        cityFrom = mainObject.get("branchTitle").getAsString();
                    } else {
                        fromId = mainObject.get("cityId").getAsString();
                        cityFrom = mainObject.get("cityTitle").getAsString();
                    }
                    //  System.out.println("fromId= " + fromId);
                } catch (Exception e) {
                    fromId = "0000000";
                }
                /////////////////////////////////////////////////////////////////////////////////////////
                request.setEntity(new StringEntity("{title: \"" + to + "\"}", "UTF-8"));
                response = httpClient.execute(request);
                instream = response.getEntity().getContent();
                reader = new BufferedReader(new InputStreamReader(instream));

                printResponse();
                try {
                    mainObject = parser.parse(responseAsString).getAsJsonObject().getAsJsonArray("items").get(0).getAsJsonObject();

                    if (mainObject.get("cityId").isJsonNull()) {
                        toId = mainObject.get("branchId").getAsString();
                        cityTo = mainObject.get("branchTitle").getAsString();
                    } else {
                        toId = mainObject.get("cityId").getAsString();
                        cityTo = mainObject.get("cityTitle").getAsString();
                    }
                    //  System.out.println("toId= " + toId);
                } catch (Exception e) {
                    toId = "0000000";
                }
/////////////////////////////////////////////////////////////////////////////////////////


                request = new HttpPost("https://kabinet.pecom.ru/api/v1/calculator/calculateprice/");
                request.addHeader("Content-Type", "application/json;charset=utf-8");
                request.addHeader("Authorization", "Basic bmlraXRvemVnZzowMUQwQUFFOTJGQTRBNTRFQUI5RkU2NTJFQzBGNTFGQzY0QjFCNTI3AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

                request.setEntity(new StringEntity("{\n" +
                        "   \"senderCityId\": " + fromId + ",\n" +
                        "   \"receiverCityId\": " + toId + ",\n" +
                        "   \"isOpenCarSender\": false,\n" +
                        "   \"senderDistanceType\": 0,\n" +
                        "   \"isDayByDay\": false, \n" +
                        "   \"isOpenCarReceiver\": false,\n" +
                        "   \"receiverDistanceType\": 0, \n" +
                        "   \"isHyperMarket\": false, \n" +
                        "   \"calcDate\": \"2015-10-06\",\n" +
                        "   \"isInsurance\": false,\n" +
                        "   \"isPickUp\": " + isPickUp + ", \n" +
                        "   \"isDelivery\": " + isDelivery + ", \n" +
                        "   \"Cargos\": [{ \n" +
                        "      \"length\": " + length + ", \n" +
                        "      \"width\": " + width + ",\n" +
                        "      \"height\": " + height + ",\n" +
                        "      \"volume\": " + volume + ",\n" +
                        "      \"maxSize\": " + maxSize + ",\n" +
                        "      \"isHP\": false, \n" +
                        "      \"sealingPositionsCount\": 0,\n" +
                        "      \"weight\": " + weight + ", \n" +
                        "      \"overSize\": false \n" +
                        "   }]\n" +
                        "}", "UTF-8"));


                response = httpClient.execute(request);

                instream = response.getEntity().getContent();
                reader = new BufferedReader(new InputStreamReader(instream));
                int counter = 0;
                printResponse();
                // System.out.println(responseAsString);
                try {
                    if (parser.parse(responseAsString).getAsJsonObject().getAsJsonPrimitive("hasError").toString().equalsIgnoreCase("true"))
                        priceMT = parser.parse(responseAsString).getAsJsonObject().getAsJsonPrimitive("errorMessage").toString();
                    JsonObject mainObject2 = parser.parse(responseAsString).getAsJsonObject().getAsJsonArray("transfers").get(0).getAsJsonObject();
                    transportingType = mainObject2.getAsJsonPrimitive("transportingType").getAsString();

                    if (transportingType.equalsIgnoreCase("1")) {
                        priceTotal = mainObject2.getAsJsonPrimitive("costTotal").getAsString();

                        JsonArray servicesObj = mainObject2.getAsJsonArray("services").getAsJsonArray();

                        for (int ii = 0; ii < 4; ii++) {

                            String temp = servicesObj.get(ii).getAsJsonObject().getAsJsonPrimitive("serviceType").getAsString();
                            switch (temp) {
                                case "Перевозка":
                                    if (servicesObj.get(ii).getAsJsonObject().getAsJsonPrimitive("info").getAsString().equalsIgnoreCase("Страхование:"))
                                        insuranceResponse = servicesObj.get(ii).getAsJsonObject().getAsJsonPrimitive("cost").getAsString();
                                    else
                                        priceMT = servicesObj.get(ii).getAsJsonObject().getAsJsonPrimitive("cost").getAsString();
                                    // System.out.println("= " + priceMT);
                                    break;
                                case "Забор":
                                    priceFrom = servicesObj.get(ii).getAsJsonObject().getAsJsonPrimitive("cost").getAsString();
                                    //  System.out.println("= " + priceFrom);
                                    break;
                                case "Доставка":
                                    counter = counter + 1;
                                    if (counter == 1)
                                        priceTO = servicesObj.get(ii).getAsJsonObject().getAsJsonPrimitive("cost").getAsString();
                                    else if (counter == 2)
                                        km = servicesObj.get(ii).getAsJsonObject().getAsJsonPrimitive("cost").getAsString();
                                    else if (counter == 3)
                                        OblPerevozka = servicesObj.get(ii).getAsJsonObject().getAsJsonPrimitive("cost").getAsString();

                                    //  System.out.println("= " + priceTO);

                                    break;

                            }
                        }

                    }
                } catch (Exception e) {

                }


                //WRITE to excel
                try {
                    label0 = new Label(0, i, from);
                    label1 = new Label(1, i, to);
                    label2 = new Label(2, i, cityFrom);
                    label3 = new Label(3, i, cityTo);
                    label4 = new Label(4, i, priceMT);
                    label5 = new Label(5, i, priceFrom);
                    label6 = new Label(6, i, priceTO);
                    label7 = new Label(7, i, km);
                    label8 = new Label(8, i, OblPerevozka);
                    label9 = new Label(9, i, priceTotal);
                    label10 = new Label(10, i, weight.toString());
                    label11 = new Label(11, i, volume.toString());


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


                } catch (Exception e) {
                    BufferedReader brr = new BufferedReader(new InputStreamReader(System.in));
                    System.out.print("ОШИБКА2 ");
                    e.getMessage();
                    e.printStackTrace();
                    e.getStackTrace();
                    Integer.parseInt(brr.readLine());
                }

            }

        } catch (Exception e) {
            BufferedReader brr = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("ОШИБКА3 ");
            e.getMessage();
            e.printStackTrace();
            e.getStackTrace();
            Integer.parseInt(brr.readLine());
        } finally {
            writableWorkbook.write();
            writableWorkbook.close();
        }


    }

}
