package com.platzi.android.firestore.network

import java.lang.Exception

//callback para notificarnos si la operacion fue exitosa o no
interface Callback<T> {

    fun onSuccess(result: T?)

    fun onFailed(exception: Exception)

}