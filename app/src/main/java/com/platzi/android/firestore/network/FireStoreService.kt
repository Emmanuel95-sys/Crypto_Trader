package com.platzi.android.firestore.network

import com.google.firebase.firestore.FirebaseFirestore
import com.platzi.android.firestore.model.Crypto
import com.platzi.android.firestore.model.User


const val CRYPTO_COLLECTION_NAME = "cryptos"
const val USERS_COLLECTION_NAME = "users"
class FireStoreService(val firebaseFirestore: FirebaseFirestore) {
    //tipo generico para evaluar cualquier tipo de documento en la coleccion

    fun setDocument(data : Any, collectionName: String , id: String, callback: Callback<Void>){
        firebaseFirestore.collection(collectionName)
            .document(id).set(data)
            .addOnSuccessListener {
                callback.onSuccess(null) }
            .addOnFailureListener { //propagando la excepcion
                callback.onFailed(it) }
    }

    fun updateUser(user: User, callback : Callback<User>?){
        firebaseFirestore.collection(USERS_COLLECTION_NAME).document(user.username)
            .update("cryptosList", user.cryptosList)
            .addOnSuccessListener { if(callback != null) { callback.onSuccess(user) } }
            .addOnFailureListener { callback?.onFailed(it) }
    }

    fun updateCrypto(crypto: Crypto){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME).document(crypto.getDocumentId())
            .update("available", crypto.available)
    }

    fun getCryptos(callback: Callback<List<Crypto>>?){
        firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
            .get()
            .addOnSuccessListener {cryptoCollection ->
                for (document in cryptoCollection){
                    val cryptoObjectList = cryptoCollection.toObjects(Crypto::class.java)
                    //propagando resultado a traves del callback
                    callback?.onSuccess(cryptoObjectList)
                    break }
            }
            .addOnFailureListener { callback?.onFailed(it) }
    }

    fun findUserById(id: String, callback: Callback<User>){
        firebaseFirestore.collection(USERS_COLLECTION_NAME).document(id).get()
            .addOnSuccessListener {userById ->
                if(userById.data != null ){
                    callback.onSuccess(userById.toObject(User::class.java))
                }else{
                    //si el usuario no existe userById.data seria null
                    //operacion exitosa pero el usuario no existe
                    callback.onSuccess(null)
                }
            }
            .addOnFailureListener {exception ->
                callback.onFailed(exception)
            }
    }

    fun listenForUpdatesInCryptos(cryptos: List<Crypto>, listener : RealTimeDataListener<Crypto>){
        val cryptoReference = firebaseFirestore.collection(CRYPTO_COLLECTION_NAME)
        for(crypto in cryptos){
            cryptoReference.document(crypto.getDocumentId()).addSnapshotListener{snapshotDataInstance, exception ->
                if(exception != null){
                    listener.onError(exception)
                }
                if(snapshotDataInstance != null && snapshotDataInstance.exists()){
                    listener.onDataChange(snapshotDataInstance.toObject(Crypto::class.java)!!)
                }
            }
        }
    }

    fun listenForUpdatesInUsers(user: User, listener: RealTimeDataListener<User>){
        val usersReference = firebaseFirestore.collection(USERS_COLLECTION_NAME)
        usersReference.document(user.username).addSnapshotListener{snapshotDataInstance, exception ->
            if(exception != null){
                listener.onError(exception)
            }
            if(snapshotDataInstance != null && snapshotDataInstance.exists()){
                listener.onDataChange(snapshotDataInstance.toObject(User::class.java)!!)
            }
        }
    }
}