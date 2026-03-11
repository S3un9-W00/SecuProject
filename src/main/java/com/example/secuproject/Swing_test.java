package com.example.secuproject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Swing_test {
    public static void main(String[] args) {

        //기본 창 띄우기
        JFrame jFrame = new JFrame("Swing test gui title");
        jFrame.setSize(500, 300);
        jFrame.setLocationRelativeTo(null);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);

        JPanel jPanel1 = new JPanel();
        JLabel jLabel1 = new JLabel("It is just text");
        JButton jButtonNext = new JButton("Next");
        JButton jButton1 = new JButton("Button");


        JPanel jPanel2 = new JPanel();
        JLabel jLabel2 = new JLabel("This is page no.2");
        JButton jButtonUndo = new JButton("go Back");


        jButton1.addActionListener(new ActionListener() {
            public int cnt = 0;
            @Override
            public void actionPerformed(ActionEvent e) {

                cnt++;

                jLabel1.setText("Button clicked!" + cnt);
                if(cnt == 67) {
                    jLabel1.setText("You got 676767676767676767   Your number : " + cnt);
                } else if(cnt == 56){
                    jLabel1.setText("You got My serial number!     You got : " + cnt);
                } else if(cnt == 523){
                    jLabel1.setText("You summoned LEGEND    LEGEND's num is : " + cnt);
                }
            }
        });

        jButtonNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.setContentPane(jPanel2);
                jFrame.revalidate();
                jFrame.repaint();
            }
        });

        jButtonUndo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.setContentPane(jPanel1);
                jFrame.revalidate();
                jFrame.repaint();
            }
        });

        jPanel1.add(jLabel1);
        jPanel1.add(jButton1);
        jPanel1.add(jButtonNext);

        jPanel2.add(jLabel2);
        jPanel2.add(jButtonUndo);

        jFrame.add(jPanel1);
        jFrame.pack();
    }
}
