package pdm.hellotome.paint;

import android.graphics.Color;
import android.graphics.Path;

public class Brush {

    private int color;
    private int strokeWidth;
    private Path ruta;

    public Brush(int c, int sw, Path r){
        color = c;
        strokeWidth = sw;
        ruta = r;
    }

    public int getColor() {return color;}
    public int getStrokeWidth() {return strokeWidth;}
    public Path getRuta() {return ruta;}

    public void setColor(int color) {this.color = color;}

    public void setStrokeWitdh(int sw) {this.strokeWidth = sw;}
}
