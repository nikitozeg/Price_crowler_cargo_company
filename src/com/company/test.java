package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by n.ivanov on 17.02.2016.
 */
public class test {
    public static void main(String[] args) throws IOException, InterruptedException {
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Введите количество обрабатываемых строк:");
            try {
                double d= new BigDecimal(br.readLine()).setScale(1, RoundingMode.HALF_UP).doubleValue();
                System.out.println(d );
            } catch (NumberFormatException nfe) {
                System.err.println("Неверный формат");

            }
        }

    }
}
