package pdm.hellotome.idiom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pdm.hellotome.ActivityMain;
import pdm.hellotome.Adapters.AdapterLanguageSelection;
import pdm.hellotome.R;

public class FragmentChangeLanguage extends FragmentActivity {

    // Se recogen los elementos que harán posible la carga de las distintas
    // opciones de idioma en la vista de la actividad
    AdapterLanguageSelection adapter;
    private RecyclerView rviewListLanguage;
    private LinearLayoutManager linearManager;

    private int method_InOut; // Esta variable guarda si el usuario llego a esta actividad para cambiar el idioma de entrada o salida

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_changelanguage);

        // Se recoge el motivo por el que ha sido invocada esta actividad
        // Que sera uno de los dos códigos REQUEST creados en el ActivityMain
        // A esta actividad no le importa mucho cual sea, pero será necesario que responda a
        // ActivityMain con el mismo código con el que fue llamada para saber como actuar en la otra
        method_InOut = getIntent().getIntExtra("METHOD", -100);

        // Se recoge el RecyclerView que contendrá todas las backup del usuario
        rviewListLanguage = findViewById(R.id.rview_flag);
        linearManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        // Mejora de rendimiento
        // Se usa solo si se sabe que el contenido no afecta al tamaño del RV
        rviewListLanguage.setHasFixedSize(true);

        loadFlags();

    }

    private void loadFlags(){

        // La lista de banderas ya se tiene como una variable estática inicializada en ActivityMain
        // idioms es la variable
        adapter = new AdapterLanguageSelection(ActivityMain.idiomsList);

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Se recoge el objeto en cuestion en la posicion en la que se clickó
                Flag flg = ActivityMain.idiomsList.get(rviewListLanguage.getChildAdapterPosition(v));

                Intent respuesta = new Intent();
                // Esta etiqueta o name debe coincidir con la usada en el onActivityResult de ActivityMain
                respuesta.putExtra("LANGUAGE-CHOSEN", flg.getLanguage());
                respuesta.putExtra("FLAG-ASSOCIATED", flg.getFlag()); // Si se pasa también el identificador de la bandera ahorramos un for luego al buscarlo
                setResult(method_InOut, respuesta); // Se devuelve una respuesta con el codigo (method_InOut) con el que se inició
                finish();

            }
        });

        rviewListLanguage.setAdapter(adapter);
        rviewListLanguage.setLayoutManager(linearManager);
        rviewListLanguage.setPadding(0,50,0,50);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        Intent respuesta = new Intent();
        // Aunque no se vaya a usar, hay que mandarle los valores para que los caze
        respuesta.putExtra("LANGUAGE-CHOSEN", "-f");
        respuesta.putExtra("FLAG-ASSOCIATED", -100);
        // Esta etiqueta o name debe coincidir con la usada en el onActivityResult de ActivityMain
        setResult(method_InOut, respuesta); // Se devuelve una respuesta con el codigo (method_InOut) con el que se inició
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}
