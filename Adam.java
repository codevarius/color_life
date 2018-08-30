package core;

import java.awt.*;
import java.util.Map;

public class Adam implements Bot {

    private String dnaA; //bot dna
    private String dnaB; //copy of dnaA
    //private int x_pos, y_pos; //x & y position
    private boolean alive; //status
    private Color self_color; //bot color
    //private int bot_width = 5;
    //private int bot_height = 5;
    //private long step = Math.round((double)(bot_width/bot_height));
    private int hp = MAX_HP;
    private String name;
    private int dist = 0; //sum of bots steps before getting normal damage
    private int fresh_rate = 10000; //corpse freshness (will decrease while being a corpse with -1 point each iteration
    private boolean born_permission = false;
    private int energy = 0;

    public static final int DEFAULT_BOT_SIZE = 3;
    public static final int REBORN_BOT_SIZE = 1;

    public static final int DNA_LEN = 7; //dna length (depends on number of available bot acts) used in act method
    public static final int DNA_COMPLEXITY = 40; //complexity of dna used in genDNA method must be dividable by 2
    public static final int STD_TIME_DELAY = 70; //screen update delay in mills
    public static final int STD_EXP = 10; //bot std exp;
    public static final int NORMAL_DIST = 25; //std num of bot steps before normal damage
    private int normal_dmg = 20;
    private int normal_recov = 1;
    public static final int MAX_HP = 100;
    private Rectangle bot_rect;
    private String bot_reg_id = ""; //bot register id in world tiled rect map

    public Color getSelf_color() {
        return self_color;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    //default constructor
    public Adam(int x, int y, Color color){
        int bot_width = DEFAULT_BOT_SIZE;
        int bot_height = DEFAULT_BOT_SIZE;
        self_color = color;
        int x_pos = x;
        int y_pos = y;
        dnaA = genDNA(DNA_LEN, DNA_COMPLEXITY);
        name = "Bot" + String.valueOf(this.hashCode());
        dnaB = dnaA;
        bot_rect = new Rectangle(x_pos,y_pos,bot_width,bot_height);
        register_bot_tile();
        alive = true;
        Thread bot_life = new Thread(() -> {
            //System.out.println("bot " + name + " was born on pos: " + new Point(x_pos,y_pos)
             //+ " and with start dna: " + dnaA + " and with reg id: " + bot_reg_id);
            int dna_index = 0;
            while(alive){
                normal_dmg = Population.getNormal_dmg();
                act(Integer.valueOf(String.valueOf(dnaA.charAt(dna_index))));
                mutate(0); //mutate every new step

                if (dna_index < dnaA.length() - 1) {
                    dna_index++;
                    //System.out.println("next index " + dna_index );
                }else
                    dna_index = 0;

                if (hp <= 0){alive = false;}

                try {
                    Thread.sleep(STD_TIME_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            die();
        });
        bot_life.setDaemon(true);
        bot_life.start();
    }

    //reborn constructor
    public Adam(int x, int y, Color color, String parent_dna){
        self_color = color;
        int x_pos = x;
        int y_pos = y;
        dnaA = parent_dna;
        //mutate(1);
        name = "Bot" + String.valueOf(this.hashCode());
        dnaB = dnaA;
        bot_rect = new Rectangle(x_pos,y_pos,REBORN_BOT_SIZE,REBORN_BOT_SIZE);
        register_bot_tile();
        alive = true;
        Thread bot_life = new Thread(() -> {
            //System.out.println("bot " + name + " was born on pos: "
                    //+ new Point(x_pos,y_pos) + " and with start dna: " + dnaA + " and with reg id: " + bot_reg_id);
            int dna_index = 0;
            while(alive){
                //System.out.println(Integer.valueOf(String.valueOf(dnaA.charAt(dna_index))));
                normal_dmg = Population.getNormal_dmg();
                act(Integer.valueOf(String.valueOf(dnaA.charAt(dna_index))));
                mutate(0); //mutate every new step

                if (dna_index < dnaA.length() - 1) {
                    dna_index++;
                    //System.out.println("next index " + dna_index );
                }else
                    dna_index = 0;

                if (hp <= 0){alive = false;}

                try {
                    Thread.sleep(STD_TIME_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            die();
        });
        bot_life.setDaemon(true);
        bot_life.start();
    }

    private void register_bot_tile() {
        for(Map.Entry<String, Rectangle> entry : Main.rectMap.entrySet()) {
            String tile_key = entry.getKey();
            Rectangle tile_rect = entry.getValue();

            if (tile_rect.contains(bot_rect)){
                bot_reg_id = tile_key;
            }
        }
    }

    //change dna of bot (if flag == 1 then will change parent dna; if flag == 0 then
    // will modify dna copy that will passed to children on reborn
    private void mutate(int flag) {
        if(flag == 0) {
            int mutate_pos = (int) (Math.random() * dnaB.length());
            StringBuffer buf = new StringBuffer(dnaB);
            buf.replace(mutate_pos, mutate_pos, String.valueOf(((int) (Math.random() * DNA_LEN))));
            buf.deleteCharAt(buf.length() - 1);
            //System.out.print("old dnaB: " + dnaB);
            dnaB = buf.toString();
            //System.out.print(" || new dnaB: " + dnaB + "\n");
        }else if(flag == 1){
            int mutate_pos = (int) (Math.random() * dnaA.length());
            StringBuffer buf = new StringBuffer(dnaA);
            buf.replace(mutate_pos, mutate_pos, String.valueOf(((int) (Math.random() * DNA_LEN))));
            buf.deleteCharAt(buf.length() - 1);
            //System.out.print("old dnaA: " + dnaA);
            dnaA = buf.toString();
            //System.out.print(" || new dnaA: " + dnaA + "\n");
        }
    }

    //inactivate bot and turn it to white corpse
    private void die() {
        //System.out.println(energy);
        if (born_permission && energy >= 100) {
            Color parent_color = this.self_color;
            this.self_color = Color.WHITE;
            int bornSize = bot_rect.width * bot_rect.height > 1 ? bot_rect.width : 0;
            int x = bot_rect.x;
            int y = bot_rect.y;
            char[] parent_dnaB = this.dnaB.toCharArray();
            char[] parent_dnaA = this.dnaA.toCharArray();
            char pdna[] = new char[parent_dnaB.length];

            //mixing dnaA and dnaB
            for (int i = 0; i < parent_dnaB.length; i++) {
                if (parent_dnaB[i] == parent_dnaA[i]) {
                    pdna[i] = parent_dnaA[i];
                } else {
                    pdna[i] = parent_dnaB[i];
                }
            }

            //pack into string buffer
            StringBuffer dna = new StringBuffer();
            dna.append(pdna);
            //System.out.println(dna);

            //System.out.println("bot " + name + " died on pos: " + new Point(this.x_pos,this.y_pos));
            for (int i = 0; i < bornSize; i++)
                Population.bornAdamBox.add(new Adam(x + ((int)(Math.random() * bot_rect.width)),
                        y + ((int)(Math.random() * bot_rect.height)), parent_color, dna.toString()));
        }else{
            this.self_color = Color.WHITE;
        }
    }

    //make action
    private void act(int dna_index) {
        //System.out.println("adam make act #" + dna_index);

        if (bot_rect.x > Main.WORLD_WIDTH)
            bot_rect.x = 1;
        if (bot_rect.x < 0)
            bot_rect.x = Main.WORLD_WIDTH - 1;
        if (bot_rect.y > Main.WORLD_HEIGHT)
            bot_rect.y = 1;
        if (bot_rect.y < 0)
            bot_rect.y = Main.WORLD_HEIGHT - 1;

        switch (dna_index){
            case 0: bot_rect.x += bot_rect.width; dist+=bot_rect.width; break;
            case 1: bot_rect.y += bot_rect.width; dist+=bot_rect.width; break;
            case 2: bot_rect.x -= bot_rect.width; dist+=bot_rect.width; break;
            case 3: bot_rect.y -= bot_rect.width; dist+=bot_rect.width; break;
            case 4:
                if(dist >= NORMAL_DIST) {
                    hp = getDamage(hp, normal_dmg);
                    //System.out.println("bot get damage current hp:" + hp);
                    break;
                }else{
                    dist = 0;
                    break;
                }
            case 5:
                mutate(1); break; //System.out.println("bot mutated -> new dna: " + dnaA);
            case 6: getRecover(hp,normal_recov); break;
            case 7: born_permission = false; die(); break;
        }
        register_bot_tile();
        check_collision(); //collision checking entry point
    }

    private void check_collision() {
        try {
            for (int i = 0; i < Population.adamBox.size(); i++) {

                Adam adam = Population.adamBox.get(i);

                //if bot meets corpse then eat corpse, restore hp and gain hp with std val
                if (adam.bot_reg_id == bot_reg_id &&
                        bot_rect.intersects(adam.bot_rect) &&
                        adam.self_color == Color.WHITE &&
                        adam.name != name && hp <= MAX_HP) {
                    hp += STD_EXP;
                    bot_rect.setSize(++bot_rect.width,++bot_rect.height);
                    dnaB = dnaB.substring(0, (dnaB.length() / 2)).
                            concat(adam.dnaA.substring(0, (adam.dnaA.length() / 2)));
                    adam.self_color = Color.BLACK;
                }

                //if bot meets other bot then...
                if (adam.bot_reg_id == bot_reg_id &&
                        bot_rect.intersects(adam.bot_rect) &&
                        adam.self_color != self_color &&
                        adam.name != name) {

                    int hp_delta = hp - adam.hp;

                    if (hp_delta > 0 && hp <= MAX_HP) {
                        adam.getDamage(adam.hp, normal_dmg * (bot_rect.width * bot_rect.height));
                        adam.energy += normal_dmg;
                    } else if (hp_delta < 0 && adam.hp <= MAX_HP) {
                        getDamage(hp, normal_dmg * (adam.bot_rect.width * adam.bot_rect.height));
                        adam.energy += normal_dmg;
                    }
                }

                //if bot meets the same color bot then...
                if (adam.bot_reg_id == bot_reg_id &&
                        bot_rect.intersects(adam.bot_rect) &&
                        adam.self_color == self_color &&
                        adam.name != name && !born_permission) {

                    born_permission = true;
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    // basic change of bot coordinates
    public void make_step(Graphics g) {
        g.setColor(self_color);
        g.fillRect(bot_rect.x, bot_rect.y, bot_rect.width, bot_rect.height);
    }

    //activities while being a corpse
    public void beCorpse(Graphics g){
        fresh_rate--;
        if (fresh_rate > 0) {
            g.setColor(self_color);
        }else{
            g.setColor(Color.BLACK);
            self_color = Color.BLACK;
            //System.out.println("corpse become rotten");
        }
        g.fillRect(bot_rect.x, bot_rect.y, bot_rect.width, bot_rect.height);
    }


}
