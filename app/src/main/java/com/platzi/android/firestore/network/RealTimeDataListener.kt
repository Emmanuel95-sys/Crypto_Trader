package com.platzi.android.firestore.network

import java.lang.Exception

//mapear monitoriear, modificar, interactuar con cualquier tipo de datos
interface RealTimeDataListener<T> {

    fun onDataChange(updatedData: T)

    fun onError(exception: Exception)

}