package pdm.hellotome.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import pdm.hellotome.R;
import pdm.hellotome.idiom.Flag;

public class AdapterLanguageSelection extends RecyclerView.Adapter<AdapterLanguageSelection.FlagViewHolder> implements View.OnClickListener{

    // Contendrá todas las banderas
    ArrayList<Flag> idioms;
    // Se carga de gestionar la accion de cada vista
    private View.OnClickListener listener;

    public AdapterLanguageSelection(ArrayList<Flag> fg){
        this.idioms = fg;
    }

    // Se recogen las referencias de cada componente del Layout
    public static class FlagViewHolder extends RecyclerView.ViewHolder{
        // El item creado para darle una visualización solo tiene una bandera
        // No se ha considerado añadir más elemento porque es bastante identificativo
        ImageView flag_option;

        // Aqui se recibe un layout, que en este caso será item_flag

        public FlagViewHolder(View itemFlagView){
            super(itemFlagView);
            // Se recoge la imagen del layout item_flag
            flag_option = itemFlagView.findViewById(R.id.flag_option); // Importante usar correctamente el id del layout y no otro
        }
    } // Fin de la clase estática ViewHolder

    // El Layout Manager llama a este método para renderizar el RecyclerView
    @Override
    public AdapterLanguageSelection.FlagViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        // Aquí se coge el modelo por defecto creado para usarlo como contenedor de diseño
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flag, parent, false);

        AdapterLanguageSelection.FlagViewHolder contenedor = new AdapterLanguageSelection.FlagViewHolder(itemView);

        // Se controla que al hacer click se ejecute una accion personalizada
        itemView.setOnClickListener(this);

        return contenedor;
    }

    @Override
    public void onBindViewHolder(AdapterLanguageSelection.FlagViewHolder viewHolder, int pos) {
        Flag item = idioms.get(pos);

        viewHolder.flag_option.setImageResource(item.getFlag());
        viewHolder.flag_option.layout(0, 0, 0, 0);
    }

    @Override
    public int getItemCount() {
        return idioms.size();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        if(listener != null){
            listener.onClick(v);
        }
    }
}

