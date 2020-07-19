package com.platzi.android.firestore.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.R
import com.platzi.android.firestore.model.User
import com.platzi.android.firestore.network.Callback
import com.platzi.android.firestore.network.FireStoreService
import com.platzi.android.firestore.network.USERS_COLLECTION_NAME
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception

/**
 * @author Santiago Carrillo
 * github sancarbar
 * 1/29/19.
 */


const val USERNAME_KEY = "username_key"

class LoginActivity : AppCompatActivity() {


    private val TAG = "LoginActivity"
    //acceso al modulo de autenticacion de Firebase
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    lateinit var firestoreService: FireStoreService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //instanciando a firestoreService
        firestoreService = FireStoreService(FirebaseFirestore.getInstance())
    }


    fun onStartClicked(view: View) {
        //mejorando experiencia de usuario
        view.isEnabled = false
        //realizar proceso de autenticacion
        //le agregamos un listener para saber si la operacion fue exitosa o no
        //regresa una tarea que puede ser o o exitosa
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //una vez la autenticaion anonima es exitosa
                    //vamos a busccar si ese usuario existe o no
                    val username = username.text.toString()
                    firestoreService.findUserById(username, object : Callback<User>{
                        override fun onSuccess(result: User?) {
                            //el usuario no ha sido creado en la DB
                            if(result == null){
                                val userInstance : User = User()
                                userInstance.username = username
                                //se envia la vista para notificar con show error
                                saveUserAndStartMainActivity(userInstance, view)

                            }else{
                                startMainActivity(username)
                            }
                        }
                        override fun onFailed(exception: Exception) {
                            showErrorMessage(view)
                        }
                    })
                        } else {
                    showErrorMessage(view)
                    view.isEnabled = true
                }
            }
    }

    private fun saveUserAndStartMainActivity(user: User, view: View) {

        firestoreService.setDocument(user, USERS_COLLECTION_NAME, user.username, object : Callback<Void>{
            override fun onSuccess(result: Void?) {
                startMainActivity(user.username)

            }
            override fun onFailed(exception: Exception) {
                showErrorMessage(view)
                Log.e(TAG, exception.toString())
                view.isEnabled = true
            }
        })
    }

    private fun showErrorMessage(view: View) {
        Snackbar.make(view, getString(R.string.error_while_connecting_to_the_server), Snackbar.LENGTH_LONG)
            .setAction("Info", null).show()
    }

    private fun startMainActivity(username: String) {
        val intent = Intent(this@LoginActivity, TraderActivity::class.java)
        intent.putExtra(USERNAME_KEY, username)
        startActivity(intent)
        finish()
    }
    //verificar usuarios ya creados para no volverlos a verificar.



}
