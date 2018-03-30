package com.bori.hipe.controllers.adapters


interface OnLoadFinishedListener {

    /**
     * adapter shold call this when load of the lis will finished
     */
    fun onListLoadFinished()

    /**
     * called when adapter finishes his load
     */
    fun onListLoadStarted()

}
