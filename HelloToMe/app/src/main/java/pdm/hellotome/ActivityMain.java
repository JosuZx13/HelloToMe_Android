package pdm.hellotome;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pdm.hellotome.Adapters.DoubleTapListener;
import pdm.hellotome.idiom.Flag;
import pdm.hellotome.idiom.FragmentChangeLanguage;
import pdm.hellotome.paint.ActivityPaint;

public class ActivityMain extends AppCompatActivity implements GestureDetector.OnDoubleTapListener {

    private static final int PERMISSION_REQUEST_AUDIO = 100;

    private static final int REQUEST_IN_LANGUAGE = 200; // La peticion se ha creado para cambiar el idioma de entrada
    private static final int REQUEST_OUT_LANGUAGE = 201; // La peticion se ha creado para cambiar el idioma de salida

    // Servicio de Google para Traducir
    Translate translate;

    // Su Listener se cargará usando una clase abstracta creada para detectar el "Double Tap" en la imagen
    private ImageView logo;

    private FloatingActionButton fb_voice;
    private FloatingActionButton fb_blackboard;
    private FloatingActionButton fb_mic;

    // Es donde se recoge el texto dictado por voz o escrito
    private EditText texBox;
    // Es el botón que permite eliminar lo que está escrito en la caja de texto
    private Button bt_cleanText;
    private Button bt_translateText;

    // Se recogen las imagenes que al pulsar sobre ellas se podra cambiar el idioma de entrada y salida
    private ImageView image_in_language;
    private ImageView image_out_language;

    public static ArrayList<Flag> idiomsList = new ArrayList<>();

    private String in_language; // Idioma de entrada
    private String out_language; //Idioma de salida

    // Este objeto permite recoger lo que un usuario diga por voz
    private SpeechRecognizer speech;

    private Pair<String, String> localTag;

    // Este objeto permitirá reproducir el contenido de una caja de texto
    private TextToSpeech tts;

    // Se va a implementar el uso del sensor de proximidad. Esto va a permitir que para hablar solo será necesario
    // tapar la pantalla del dispositivo con la mano
    SensorManager sensorMang;
    Sensor sensor;
    SensorEventListener sensorEvtList;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logo = findViewById(R.id.logoHelloToMe);

        fb_voice = (FloatingActionButton) findViewById(R.id.fb_voice);
        fb_blackboard = (FloatingActionButton) findViewById(R.id.fb_blackboard);
        fb_mic = (FloatingActionButton) findViewById(R.id.fb_mic);

        texBox = findViewById(R.id.texBox);
        bt_cleanText = findViewById(R.id.bt_cleanText);
        bt_translateText = findViewById(R.id.bt_translateText);

        sensorMang = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Se inicializan las banderas disponibles
        initializeFlag();

        logo.setOnClickListener(new DoubleTapListener() {
            @Override
            public void onSingleClick(View v) {
                //Nothing to do
            }

            @Override
            public void onDoubleClick(View v) {
                textToSpeech();
            }
        });

        // Las banderas Que indicarán en cada momento el idioma en el que se escucha y el que se traduce
        image_in_language = findViewById(R.id.iv_in_language); // El idioma de entrada
        image_in_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent inMethod = new Intent(ActivityMain.this, FragmentChangeLanguage.class);
                inMethod.putExtra("METHOD", REQUEST_IN_LANGUAGE);
                ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                startActivityForResult(inMethod, REQUEST_IN_LANGUAGE, animacion.toBundle());
            }
        });

        image_out_language = findViewById(R.id.iv_out_language); // El idioma de salida
        image_out_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent outMethod = new Intent(ActivityMain.this, FragmentChangeLanguage.class);
                outMethod.putExtra("METHOD", REQUEST_OUT_LANGUAGE);
                ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.fade_in, R.anim.fade_out);
                startActivityForResult(outMethod, REQUEST_OUT_LANGUAGE, animacion.toBundle());
            }
        });

        image_in_language.setTag(idiomsList.get(0).getLanguage()); // La app siempre empezará con el idioma de entrada SPANISH
        image_out_language.setTag(idiomsList.get(0).getLanguage()); // Y en el de salida SPANISH

        in_language = image_in_language.getTag().toString(); // Se recoge el idioma que actualmente está en uso
        // Se usan recursos para evitar confusiones y que así quede parametrizado, sin "numeros mágicos"
        out_language = image_out_language.getTag().toString();

        // Comprueba que el usuario haya dado permisos a la aplicación para poder usar el micrófono
        checkAudioPermissions();

        // Se crea el Speech
        speech = SpeechRecognizer.createSpeechRecognizer(this);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // Si no hay ningún problema en la inicialización del Text-To-Speech
                if (status != TextToSpeech.ERROR) {
                    // Ya que para España NO HAY CONSTANTE
                    SwitchTag(out_language);
                    tts.setLanguage(new Locale(localTag.first, localTag.second));

                }
            }
        });

        // En este apartado se crea la activdad que va a ejecutar el sistema de audio para que el dispositivo se ponga en modo escucha

        // Da inicio a una actividad que escuchará al usuario y mandará los datos al speech
        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Se le informa al speech sobre el modelo preferido al ejecutarse ACTION_RECOGNIZE_SPEECH
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        // Habilitando el modo dictado se puede ir recogiendo y procesando (Debe usarse el metodo onPartialResults
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speech.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
                texBox.setText(""); // Para eliminar lo que ya hubiese escrito
                texBox.setHint(getString(R.string.tx_escuchando));
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int error) {
                texBox.setHint(getString(R.string.textBox_hint));
            }

            @Override
            public void onResults(Bundle results) { //Este es el método que recoge lo escuchado por el dispositivo y lo traduce en texto
                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String mensaje = traduction(data.get(0));
                texBox.setText(mensaje);
                texBox.setHint("");
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                texBox.setText(data.get(0)); // Aquí se va escribiendo al mismo tiempo que se habla
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        // Boton para reproducir lo que está escrito en la caja de Texto
        fb_voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textToSpeech();
            }
        });

        // Boton para abrir la pizarra
        fb_blackboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent blackboard = new Intent(getApplicationContext(), ActivityPaint.class);
                ActivityOptions animacion = ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.slide_in, R.anim.slide_out);
                startActivity(blackboard, animacion.toBundle());
            }
        });

        // Boton para escuchar al usuario y redactarlo en la caja de Texto
        fb_mic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Si se levanta el dedo del botón, entonces se detendrá la escucha
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    speech.stopListening();
                }

                // Si se mantiene pulsado, se inicia la escucha
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    checkAudioPermissions(); //Por si lo ha rechazado pero intenta usar el micro igualmente que le avise si no los tiene
                    speech.startListening(speechRecognizerIntent);
                }

                return false;
            }
        });

        bt_cleanText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texBox.setText(""); // Para eliminar el texto que haya en la caja de texto
            }
        });

        // Por si el usuario escribe a mano la traducción y pueda traducirlo
        bt_translateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                texBox.setText(traduction(texBox.getText().toString()));
            }
        });

        // Tratamiento del SENSOR
        sensor = sensorMang.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (sensor != null) {
            // Sirve para detectar si nos hemos alejado o acercado al dispositivo
            sensorEvtList = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    // Si estamos en el ratio de detección del sensor
                    if (event.values[0] < sensor.getMaximumRange()) {
                        checkAudioPermissions(); //Por si lo ha rechazado pero intenta usar el micro igualmente que le avise si no los tiene
                        speech.startListening(speechRecognizerIntent);
                    } else {
                        // Al alejarnos termina la escucha
                        speech.stopListening();
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
        }

        startSensor();

    }

    // Cuando un intent se ha iniciado mediante startActivityForResult
    // Es posible realizar una acción a razón de las acciones y los valores devueltos por la actividad
    // a la que se ha navegado mediante dicho INTENT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Esto servirá en ambos case del Switch y además para comprobar si se eligió o no una bandera
        int flag_code = data.getIntExtra("FLAG-ASSOCIATED", -100);

        // Al venir de la actiividad de la bandera habrá usado el mismo código con el que se inició
        switch (requestCode) {

            case REQUEST_IN_LANGUAGE:
                // Debe de valer distinto a -100 pues si vale esto, significará que no se eligió nada
                // (Cuando por ejemplo se pulsa back)
                if (flag_code != -100) {
                    // Traera consigo un idioma elegido (puede ser el mismo o no)
                    // Esto tendrá siempre un elemento de String-Array por como fueron creados los objetos Flag
                    in_language = data.getStringExtra("LANGUAGE-CHOSEN"); // Se actualiza el idioma actual elegido de entrada

                    image_in_language.setImageResource(flag_code);
                }
                break;

            case REQUEST_OUT_LANGUAGE:

                if (flag_code != -100) {
                    // El NAME usado para el dato agregado será el mismo para evitar problemas
                    out_language = data.getStringExtra("LANGUAGE-CHOSEN"); // Se actualiza el idioma actual elegido de entrada

                    // El proceso es exactamente el mismo, pero en esta ocasion se modifica la bandera
                    // que identifica al idioma de salida

                    image_out_language.setImageResource(flag_code);

                    SwitchTag(out_language);

                }

                break;

            default:
                break;
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Controlar el salir de la aplicación para que no quede abierta y asegurar que el usuario quería salir
    protected void finishApp() {
        new AlertDialog.Builder(ActivityMain.this)
                .setIcon(R.drawable.ic_baseline_exit_to_app_24)
                .setTitle(getResources().getString(R.string.app_cerrar))
                .setMessage(getResources().getString(R.string.cerrar_mensaje))
                .setPositiveButton(getResources().getString(R.string.aceptar), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent salir = new Intent(getApplicationContext(), ActivityExit.class);
                        salir.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        stopTTS();
                        startActivity(salir);
                    }

                }).setNegativeButton(getResources().getString(R.string.cancelar), null).show();
    }

    // COMPRUEBA QUE EL USUARIO HA ACEPTADO LOS PERMISOS PARA USAR EL MICROFONO DEL DISPOSITIVO
    private void checkAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_AUDIO);
        }
    }

    // COMPRUEBA SI EL USUARIO TIENE ACCESO A INTERNET
    private boolean checkInternet(){
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    // Para mostrar mensajes popput
    public void toastAdvice(String t){
        LayoutInflater inflater = this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_toast, this.findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.text);
        text.setText(t);

        Toast toast = new Toast(this);
        toast.setGravity(Gravity.BOTTOM, 0, 250);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    // Se inicializan los idiomas que estarán disponibles
    private void initializeFlag() {
        // Se recoge el array del fichero xml string.xml que serán los idiomas disponibles
        List<String> language_options = Arrays.asList(getResources().getStringArray(R.array.language_array));
        // Spanish
        idiomsList.add(new Flag(language_options.get(0), R.drawable.flag_spanish));
        // British
        idiomsList.add(new Flag(language_options.get(1), R.drawable.flag_british));
        // German
        idiomsList.add(new Flag(language_options.get(2), R.drawable.flag_german));
        // Norway
        idiomsList.add(new Flag(language_options.get(3), R.drawable.flag_norway));
    }

    private String traduction(String mensaje) {
        // Se traduce de un idioma de entrada (in_language) a otro de salida (out_language)
        if(checkInternet()){
            getTranslateService();
            mensaje = translate(mensaje);
        }else{
            toastAdvice(getString(R.string.not_internet_translation));
        }

        return mensaje;
    }

    private void SwitchTag(String lan) {
        switch (lan) {
            case "SPANISH":
                localTag = new Pair<>("spa", "ESP");
                break;

            case "BRITISH":
                localTag = new Pair<>("eng", "GBR");
                break;

            case "GERMAN":
                localTag = new Pair<>("de", "DEU");
                break;

            case "NORWAY":
                localTag = new Pair<>("no", "NO");
                break;

        }
        // Se cambia el idioma con el que hace la lectura
        tts.setLanguage(new Locale(localTag.first, localTag.second));

    }

    private void textToSpeech() {
        String voz = texBox.getText().toString();
        tts.speak(voz, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    // Para detener el servicio del Text-To-Speech
    private void stopTTS() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    private void startSensor() {
        sensorMang.registerListener(sensorEvtList, sensor, 2000 * 1000);
    }

    private void stopSensor() {
        sensorMang.unregisterListener(sensorEvtList);
    }

    // Obtiene una configuración del servicio de traduccion
    private void getTranslateService() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = getResources().openRawResource(R.raw.hellotome314111d64f73544295)) {

            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }

    private String translate(String texto) {
        // El texto que se va a traducir
        Translation translation = translate.translate(texto, Translate.TranslateOption.targetLanguage(localTag.first), Translate.TranslateOption.model("base"));
        return translation.getTranslatedText();

    }

    @Override
    protected void onPause() {
        stopSensor();
        super.onPause();

    }

    @Override
    protected void onResume() {
        startSensor();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finishApp();
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        textToSpeech();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}