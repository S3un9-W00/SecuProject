package com.example.secuproject.backjoon.backjoon_bronze;

import java.util.Scanner;

public class problem02 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String a;
        String mid;

        int sum = 0;
        int first = sc.nextInt();
        while(true){
            mid = sc.next();

            if(mid.equals("+")){
                first += sc.nextInt();
                continue;
            }
            else if(mid.equals("-")){
                first -= sc.nextInt();
                continue;
            }
            else if(mid.equals("*")){
                first *= sc.nextInt();
                continue;
            }
            else if(mid.equals("/")){
                first /= sc.nextInt();
                continue;
            }

            else if (mid.equals("=")){
                break;
            }
        }


        System.out.println(first);
    }
}
