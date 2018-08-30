package core;

public interface Bot {

    default String genDNA(int dna_len, int dnaComplexity) {
        StringBuilder dna = new StringBuilder();

        for (int i = 0; i < dnaComplexity; i++){
            dna.append((int)(Math.random() * dna_len));
        }

        return dna.toString();
    }

    default int getDamage(int current_hp, int damage){

        current_hp = current_hp - damage;

        return current_hp > 0 ? current_hp : 0;
    }

    default int getRecover(int current_hp, int recov){

        current_hp = current_hp + recov;

        return current_hp > 0 ? current_hp : 0;
    }
}
