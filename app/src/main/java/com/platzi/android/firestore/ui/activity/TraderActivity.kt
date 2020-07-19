package com.platzi.android.firestore.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.R
import com.platzi.android.firestore.adapter.CryptosAdapter
import com.platzi.android.firestore.adapter.CryptosAdapterListener
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User
import com.platzi.android.firestore.network.Callback
import com.platzi.android.firestore.network.FireStoreService
import com.platzi.android.firestore.network.RealTimeDataListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_trader.*
import java.lang.Exception
import kotlin.random.Random


/**
 * @author Santiago Carrillo
 * 2/14/19.
 */
class TraderActivity : AppCompatActivity(), CryptosAdapterListener {
    //inicializar luego de setear el content view
    lateinit var fireStoreService: FireStoreService

    private val cryptosAdapter : CryptosAdapter = CryptosAdapter(this)

    //crear variable con el username
    //puede tomar un valor nulo
    //la vamos a inicializar en onCreate
    private var username : String? = null

    //usuario que obtenemos del servidor
    private var user : User? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trader)
        fireStoreService = FireStoreService(FirebaseFirestore.getInstance())

        //inicializando username
        username = intent.getStringExtra(USERNAME_KEY)
        //intent.extras!!.[USERNAME_KEY]!!.toString()

        usernameTextView.text = username

        configureRecyclerView()
        loadCryptos()

        fab.setOnClickListener { view ->
            Snackbar.make(view, getString(R.string.generating_new_cryptos), Snackbar.LENGTH_SHORT)
                .setAction("Info", null).show()
            generateCryptoCurrenciesRandom()
        }

    }

    private fun generateCryptoCurrenciesRandom() {
        //lista de criptomonedas que tiene el adapter
        for (crypto in cryptosAdapter.cryptoList){
            val amount : Int = (1..10).random()
            crypto.available += amount
            fireStoreService.updateCrypto(crypto)
        }
    }

    private fun loadCryptos() {
        fireStoreService.getCryptos(object : Callback<List<Crypto>>{
            override fun onSuccess(cryptoList: List<Crypto>?) {
                //una vez que tengamos la lista de cryptomonedas
                //vamos a utlizar el valor de username para consultar el saldo de las crypto monedas
                //vamos a cargar la lista por defecto si el usuario no tiene aun lista de cryptos

                fireStoreService.findUserById(username!!, object : Callback<User>{
                    override fun onSuccess(result: User?) {
                        user = result
                        if(user?.cryptosList == null){
                            val userCryptoList = mutableListOf<Crypto>()

                            for (crypto in cryptoList!!){
                               //añadiendo las cryptos de firestore a user
                                userCryptoList.add(crypto)
                            }
                            user!!.cryptosList = userCryptoList
                            fireStoreService.updateUser(user!!, null)
                        }
                        loadUserCryptos()
                        addRealTimeDatabaseListeners(user!!, cryptoList!!)
                    }
                    override fun onFailed(exception: Exception) {
                      showGeneralServerErrorMessage()
                    }
                })

                //cambio de thread por que vamos a actualizar la interfaz grafica
                this@TraderActivity.runOnUiThread {
                    //aqui el usuario aun es nulo
                    cryptosAdapter.cryptoList = cryptoList!!
                    //Forzar al componente para renderizar, dibujar las criptomonedas.
                    cryptosAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailed(exception: Exception) {
                Log.e("TraderActivity", "error Loading cryptos: $exception")
                showGeneralServerErrorMessage()
            }

        })
    }

    private fun addRealTimeDatabaseListeners(user: User, cryptosList: List<Crypto>) {

        fireStoreService.listenForUpdatesInUsers(user, object : RealTimeDataListener<User>{
            override fun onDataChange(updatedData: User) {
                this@TraderActivity.user = updatedData
                loadUserCryptos()
            }
            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }
        })


        fireStoreService.listenForUpdatesInCryptos(cryptosList, object : RealTimeDataListener<Crypto>{
            override fun onDataChange(updatedData: Crypto) {
                var pos = 0
                for (crypto in cryptosAdapter.cryptoList){
                    if ( crypto.name.equals(updatedData.name)){
                        //updatedData valor que viene del servidor.
                        crypto.available = updatedData.available
                        cryptosAdapter.notifyItemChanged(pos)
                    }
                    pos++
                }
            }

            override fun onError(exception: Exception) {
                showGeneralServerErrorMessage()
            }
        })

    }

    private fun loadUserCryptos() {
        //dentro de l ui thread podemos modificar la vista
        runOnUiThread{
            if (user != null && user!!.cryptosList != null){
                infoPanel.removeAllViews()
                for(crypto in user?.cryptosList!!){
                    addUserCryptoInfoRow(crypto)
                }
            }
        }
    }

    private fun addUserCryptoInfoRow(crypto: Crypto) {
        //cargando vista
        val view = LayoutInflater.from(this).inflate(R.layout.coin_info, infoPanel, false)
        view.findViewById<TextView>(R.id.coinLabel).text = getString(R.string.coin_info, crypto.name, crypto.available.toString())
        Picasso.get().load(crypto.imageUrl).into(view.findViewById<ImageView>(R.id.coinIcon))
        //ageregar al contenedor
        infoPanel.addView(view)

    }

    fun showGeneralServerErrorMessage(){
        Snackbar.make(fab, "Error while connecting to the server", Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }
    private fun configureRecyclerView() {
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        //setear el layout manager
        recyclerView.layoutManager = layoutManager
        //setear adaptador
        recyclerView.adapter = cryptosAdapter
    }

    override fun onBuyCryptoClicked(crypto: Crypto) {
        if(crypto.available > 0 ){
            for (userCrypto in user!!.cryptosList!!){
                if(userCrypto.name == crypto.name){
                    userCrypto.available += 1
                    break
                }
            }
            crypto.available--
            fireStoreService.updateUser(user!!, null)
            fireStoreService.updateCrypto(crypto)
        }
    }
}