package pdm.hellotome.idiom;

import java.io.Serializable;

// Se carga la interfaz serializable para poder hacer construcciones con ellos en listas como un objeto identificativo
public class Flag implements Serializable {

    // Atributos
    private String language;
    private int flag; // Se cargará el número identificado del recurso de la bandera

    // Realmente no hace falta, pero se crea
    public Flag(){
        language = "-l";
        flag = -100;
    }

    public Flag(String la, int fl){
        language = la;
        flag = fl;
    }

    // Metodos GET
    public String getLanguage(){return this.language;}
    public int getFlag(){return this.flag;}

    // Metodos SET
    public void setLanguage(String la){this.language = la;}
    public void setFlag(int fl){this.flag = fl;}

    @Override
    public String toString() {
        return "Flag{" +
                "language='" + language + '\'' +
                ", flag=" + flag +
                '}';
    }
}
