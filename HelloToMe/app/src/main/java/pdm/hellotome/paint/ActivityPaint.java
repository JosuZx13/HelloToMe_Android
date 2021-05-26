package pdm.hellotome.paint;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import pdm.hellotome.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class ActivityPaint extends AppCompatActivity {

    RelativeLayout blackboard_pencil_black;
    RelativeLayout blackboard_pencil_red;
    RelativeLayout blackboard_pencil_blue;
    RelativeLayout blackboard_pencil_rubber;

    ImageView pencil_black;
    ImageView pencil_red;
    ImageView pencil_blue;
    ImageView pencil_rubber;

    int selected_color;

    private PaintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        // CANVAS PARA EL DIBUJO EN LA PIZARRA

        paintView = (PaintView) findViewById(R.id.view_blackboard);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);

        // Forma de recoger un color
        //selected_color = Color.valueOf(getColor(R.color.buttoncolor));

        // Se identifican todos los componentes que interactuan
        blackboard_pencil_black = findViewById(R.id.blackboard_pencil_black);
        blackboard_pencil_red = findViewById(R.id.blackboard_pencil_red);
        blackboard_pencil_blue = findViewById(R.id.blackboard_pencil_blue);
        blackboard_pencil_rubber = findViewById(R.id.blackboard_pencil_rubber);

        pencil_black = findViewById(R.id.pencil_black);
        pencil_red = findViewById(R.id.pencil_red);
        pencil_blue = findViewById(R.id.pencil_blue);
        pencil_rubber = findViewById(R.id.pencil_rubber);

        /*
        * Para hacer que no tenga un espacio tan limitado el elegir un color, el control de seleccion
        * se hace al pulsar en cualquier punto del recuadro que tiene dentro el circulo del color
        * */
        blackboard_pencil_black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pencil_black.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ly_border_selected_item_blackboard));
                pencil_red.setBackground(null);
                pencil_blue.setBackground(null);
                pencil_rubber.setBackground(null);
                // El lapiz escribe de color negro ahora
                selected_color = getColor(R.color.pencil_black);
                paintView.useRubber(false, selected_color);
            }
        });

        blackboard_pencil_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pencil_black.setBackground(null);
                pencil_red.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ly_border_selected_item_blackboard));
                pencil_blue.setBackground(null);
                pencil_rubber.setBackground(null);

                selected_color = getColor(R.color.pencil_red);
                paintView.useRubber(false, selected_color);
            }
        });

        blackboard_pencil_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pencil_black.setBackground(null);
                pencil_red.setBackground(null);
                pencil_blue.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ly_border_selected_item_blackboard));
                pencil_rubber.setBackground(null);

                selected_color = getColor(R.color.pencil_blue);
                paintView.useRubber(false, selected_color);
            }
        });

        blackboard_pencil_rubber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pencil_black.setBackground(null);
                pencil_red.setBackground(null);
                pencil_blue.setBackground(null);
                pencil_rubber.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ly_border_selected_item_blackboard));
                // La goma será como pintar blanco
                selected_color = getColor(R.color.pencil_rubber);
                paintView.useRubber(true, selected_color);
            }
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}