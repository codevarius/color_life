package core;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static final int WORLD_WIDTH = 1000;
    public static final int WORLD_HEIGHT = 1000;
    public static final int POP_CAPACITY = 5250; //init population size on start
    public static HashMap<String,Rectangle> rectMap;


    public static void main(String[] args) {

        generateTileCollisionMatrix();

        Thread era = new Thread(() -> {
            Population pop = new Population();
            pop.init();
            while(true){
                if(!Population.bornAdamBox.isEmpty()){
                    //System.out.println(Population.bornAdamBox);
                    Population.adamBox.add(Population.bornAdamBox.pop());
                }
                pop.iterate();

            }
        });

        era.start();
    }

    private static void generateTileCollisionMatrix() {
        rectMap = new HashMap<>();
        int tile_width = Math.round(WORLD_WIDTH/10);
        int tile_height = Math.round(WORLD_HEIGHT/10);

        //bot collision tiled rect map
        for (int i = 0; i < WORLD_WIDTH/tile_width; i++){
            for (int j = 0; j < WORLD_HEIGHT/tile_height; j++){
                StringBuffer id = new StringBuffer();
                id.append(i);
                id.append(":");
                id.append(j);
                rectMap.put(id.toString(),new Rectangle(i*tile_width, j*tile_height, tile_width, tile_height));
            }
        }

        //System.out.println(rectMap);
    }
}

class Population{
    public static ArrayList<Adam> adamBox = new ArrayList<>();
    public static ArrayDeque<Adam> bornAdamBox = new ArrayDeque<>();
    public static Integer normal_dmg = 20; //damage rate

    public static int getNormal_dmg() {
        return normal_dmg;
    }

    private JFrame field = new JFrame("field");

    JPanel panf = new JPanel(){
        @Override
        public void paintComponent(Graphics g) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Main.WORLD_WIDTH, Main.WORLD_HEIGHT);

            for (int i = 0; i < adamBox.size(); i++) {
                if (adamBox.get(i).getSelf_color() != Color.WHITE && adamBox.get(i).isAlive()) {
                    adamBox.get(i).make_step(g);
                }else if(adamBox.get(i).getSelf_color() == Color.WHITE) {
                    adamBox.get(i).beCorpse(g);
                }else if(adamBox.get(i).getSelf_color() == Color.BLACK){
                    adamBox.remove(adamBox.get(i));
                    //Main.adamBoxCap = adamBox.size();
                    //System.out.println("done bot removing from world... popul size now: " + adamBox.size());
                    break;
                }

            }

            int populStat[] = new int[5];
            for (int i = 0; i < adamBox.size(); i++) {
                switch (adamBox.get(i).getSelf_color().hashCode()){
                    case -65536: populStat[0]++; break;
                    case -16711936: populStat[1]++; break;
                    case -16776961: populStat[2]++; break;
                    case -65281: populStat[3]++; break;
                    case -1: populStat[4]++; break;
                }
            }

            g.setColor(Color.YELLOW);
            g.drawString("POPULATION TOTAL: " + adamBox.size(),10,30);
            g.drawString("RED POPULATION: " + populStat[0],10,50);
            g.drawString("GREEN POPULATION: " + populStat[1],10,70);
            g.drawString("BLUE POPULATION: " + populStat[2],10,90);
            g.drawString("MAGENTA POPULATION: " + populStat[3],10,110);
            g.drawString("CORPSES: " + populStat[4],10,130);

        }
    };

    public Population(){
        field.setBounds(new Rectangle(100,10,Main.WORLD_WIDTH,Main.WORLD_HEIGHT));
        field.setResizable(false);
        field.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void init() {

        field.getContentPane().add(panf);

        for(int i = 0; i < Main.POP_CAPACITY; i++)
            adamBox.add(new Adam((int)(Math.random() * 1000),(int)(Math.random() * 1000), genColor()));

        field.setVisible(true);
    }

    public void iterate() {
        //System.out.println("population run time:" + new Date().toString());
        //System.out.println(adamBox.size());
        panf.repaint();
    }

    public static Color genColor(){

        //individual color for each bot
        //return new Color((int)(Math.random()*255),
                //(int)(Math.random()*255),(int)(Math.random()*255));


        switch ((int)(Math.random() * 4)){
            case 0: return Color.GREEN;
            case 1: return Color.RED;
            case 2: return Color.MAGENTA;
            case 3: return Color.BLUE;
            default: return Color.WHITE;
        }

    }
}