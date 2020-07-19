package com.platzi.android.firestore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.platzi.android.firestore.R
import com.platzi.android.firestore.model.Crypto
import com.squareup.picasso.Picasso

/**
 * @author Santiago Carrillo
 * 3/9/19.
 */


class CryptosAdapter(val cryptosAdapterListener: CryptosAdapterListener) :
    RecyclerView.Adapter<CryptosAdapter.ViewHolder>() {


    var cryptoList: List<Crypto> = ArrayList()

    //creara al view holder basasdo en el layout.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //attacht paretn es para agregar al padre
        val view = LayoutInflater.from(parent.context).inflate(R.layout.crypto_row, parent, false)
        //hacer wrapper de la vista utilizando el viewHolder.
        return ViewHolder(view)
    }

    //aqui simplemten hay que retornar el tamaño de la lista
    override fun getItemCount(): Int {
        return cryptoList.size
    }


    //hace la actualizacon de cada uno de los valores de la lista de las cripto monedas.
    //necesitamos una libreria para la carga de imagenes.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //obtener la cryptomoneda en la posicion que obtenemos como parametro
        val crypto = cryptoList[position]
        //
        Picasso.get().load(crypto.imageUrl).into(holder.imageFL)
        //
        holder.nameFL.text = crypto.name
        holder.availableFL.text = holder.itemView.context.getString(R.string.available_message, crypto.available.toString())

        holder.buyButtonFL.setOnClickListener {
            cryptosAdapterListener.onBuyCryptoClicked(crypto)
        }
//        if(crypto.available == 0){
//            holder.availableFL.visibility = View.GONE
//            holder.buyButtonFL.visibility = View.GONE
//            holder.outOfStockTvFL.visibility = View.VISIBLE
//        }

    }

    //representacion grafica de una instancia
    //mapear los atributos del objeto con las vistas definidas en un layout
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageFL = view.findViewById<ImageView>(R.id.image)
        var nameFL = view.findViewById<TextView>(R.id.nameTextView)
        var availableFL = view.findViewById<TextView>(R.id.availableTextView)
        var buyButtonFL = view.findViewById<TextView>(R.id.buyButton)
//        var outOfStockTvFL = view.findViewById<TextView>(R.id.outOfStock)
    }
}