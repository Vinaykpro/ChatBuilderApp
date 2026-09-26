package com.vinaykpro.chatbuilder

import android.app.Application
import com.vinaykpro.chatbuilder.billing.BillingManager

class ChatBuilderApplication : Application() {

    lateinit var billingManager: BillingManager
        private set

    override fun onCreate() {
        super.onCreate()

        billingManager = BillingManager(applicationContext)
    }
}