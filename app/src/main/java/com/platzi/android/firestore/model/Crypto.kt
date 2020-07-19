package com.platzi.android.firestore.model

class Crypto(var name: String = "", var imageUrl: String =  "", var available: Int = 0 ) {

    fun getDocumentId () : String {
        //en nuestro ejemplo consideramos al nombre como el ID
        return name.toLowerCase()
    }


}