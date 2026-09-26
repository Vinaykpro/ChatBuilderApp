package com.vinaykpro.chatbuilder.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(context: Context) {

    companion object {
        private const val PRODUCT_ID = "chatbuilder_premium"
    }

    private val _monthlyProduct = MutableStateFlow<ProductDetails?>(null)
    val monthlyProduct: StateFlow<ProductDetails?> = _monthlyProduct

    private val _yearlyProduct = MutableStateFlow<ProductDetails?>(null)
    val yearlyProduct: StateFlow<ProductDetails?> = _yearlyProduct

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
        }
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(
                billingResult: BillingResult
            ) {
                if (billingResult.responseCode ==
                    BillingClient.BillingResponseCode.OK
                ) {
                    queryProducts()
                    checkPremiumStatus()
                }
            }

            override fun onBillingServiceDisconnected() {
                // We can reconnect later if needed
            }
        })
    }

    private fun queryProducts() {

        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ID)
            .setProductType(
                BillingClient.ProductType.SUBS
            )
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult,
                                                         productDetailsResult ->

            if (billingResult.responseCode !=
                BillingClient.BillingResponseCode.OK
            ) {
                return@queryProductDetailsAsync
            }
            val details = productDetailsResult.productDetailsList

            val productDetails = details.firstOrNull()
                ?: return@queryProductDetailsAsync

            productDetails.subscriptionOfferDetails?.forEach { offer ->

                when (offer.basePlanId) {

                    "monthly" -> {
                        _monthlyProduct.value = productDetails
                    }

                    "yearly" -> {
                        _yearlyProduct.value = productDetails
                    }
                }
            }
        }
    }

    fun launchMonthlyPurchase(activity: Activity) {
        launchPurchase(activity, "monthly")
    }

    fun launchYearlyPurchase(activity: Activity) {
        launchPurchase(activity, "yearly")
    }

    private fun launchPurchase(
        activity: Activity,
        basePlanId: String
    ) {

        val productDetails = when (basePlanId) {
            "monthly" -> _monthlyProduct.value
            "yearly" -> _yearlyProduct.value
            else -> null
        } ?: return

        val offer = productDetails.subscriptionOfferDetails
            ?.firstOrNull { it.basePlanId == basePlanId }
            ?: return

        val productDetailsParams =
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offer.offerToken)
                .build()

        val billingFlowParams =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(productDetailsParams)
                )
                .build()

        billingClient.launchBillingFlow(
            activity,
            billingFlowParams
        )
    }

    private fun handlePurchase(purchase: Purchase) {

        if (purchase.purchaseState ==
            Purchase.PurchaseState.PURCHASED
        ) {

            if (!purchase.isAcknowledged) {

                val acknowledgeParams =
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()

                billingClient.acknowledgePurchase(
                    acknowledgeParams
                ) { billingResult ->
                    // Handle acknowledgement result
                }
            }

            // TODO:
            // Unlock Premium
            _isPremium.value = true
        }
    }

    fun checkPremiumStatus() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _isPremium.value = purchases.any { purchase ->
                    purchase.products.any { productId ->
                        productId == PRODUCT_ID
                    }
                }
            }
        }
    }

    fun destroy() {
        billingClient.endConnection()
    }
}