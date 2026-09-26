package com.vinaykpro.chatbuilder.ui.screens.premium

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import com.vinaykpro.chatbuilder.R
import com.vinaykpro.chatbuilder.billing.BillingManager
import com.vinaykpro.chatbuilder.ui.theme.LightColorScheme
import com.vinaykpro.chatbuilder.ui.theme.White
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale


enum class PremiumPlan {
    MONTHLY,
    YEARLY
}

data class PremiumPricing(
    val monthlyPrice: String = "99",
    val originalMonthlyPrice: String? = "199",
    val yearlyPrice: String = "599",
    val originalYearlyPrice: String? = "1199",
    val savingsPercentage: String? = null
)


@Composable
fun PremiumScreen(
    isPurchasing: Boolean = false,
    isDarkTheme: Boolean = false,
    isLoading: Boolean = false,
    isPremium: Boolean = false,
    billingManager: BillingManager,
    onBack: () -> Unit
) {
    val view = LocalView.current
    val activity = LocalContext.current as? Activity

    val monthlyProduct by billingManager.monthlyProduct
        .collectAsState()

    val yearlyProduct by billingManager.yearlyProduct
        .collectAsState()

    SideEffect {
        val window = activity?.window
        if (window != null)
            WindowInsetsControllerCompat(window, view)
                .isAppearanceLightStatusBars = !isDarkTheme
    }

    var selectedPlan by remember {
        mutableStateOf(PremiumPlan.YEARLY)
    }

    val colors = MaterialTheme.colorScheme
    val onPrimaryColor =
        if (colors.primary.luminance() > 0.5f)
            Color.Black
        else
            Color.White

    val selectedProduct = when (selectedPlan) {
        PremiumPlan.MONTHLY -> monthlyProduct
        PremiumPlan.YEARLY -> yearlyProduct
        else -> yearlyProduct
    }

    val selectedOffer = selectedProduct
        ?.subscriptionOfferDetails
        ?.firstOrNull {
            it.basePlanId == when (selectedPlan) {
                PremiumPlan.MONTHLY -> "monthly"
                PremiumPlan.YEARLY -> "yearly"
            }
        }

    val selectedPricingPhase = selectedOffer
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()

    val currentPrice = selectedPricingPhase
        ?.formattedPrice
        ?: "—"

    val currentAmountMicros = selectedPricingPhase
        ?.priceAmountMicros

    val originalPrice = currentAmountMicros?.let {
        formatOriginalPrice(
            priceAmountMicros = it,
            currencyCode = selectedPricingPhase.priceCurrencyCode
        )
    }

    val monthlyOffer = monthlyProduct
        ?.subscriptionOfferDetails
        ?.firstOrNull { it.basePlanId == "monthly" }

    val yearlyOffer = yearlyProduct
        ?.subscriptionOfferDetails
        ?.firstOrNull { it.basePlanId == "yearly" }

    val monthlyPhase = monthlyOffer
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()

    val yearlyPhase = yearlyOffer
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()
//
//    var phase = monthlyPhase
//    Log.d("BILLING M", "priceMicros = ${phase?.priceAmountMicros}")
//    Log.d("BILLING M", "currency = ${phase?.priceCurrencyCode}")
//    Log.d("BILLING M", "formatted = ${phase?.formattedPrice}")
//    phase = yearlyPhase
//    Log.d("BILLING Y", "priceMicros = ${phase?.priceAmountMicros}")
//    Log.d("BILLING Y", "currency = ${phase?.priceCurrencyCode}")
//    Log.d("BILLING Y", "formatted = ${phase?.formattedPrice}")

    val monthlyPrice = monthlyPhase?.formattedPrice ?: "—"
    val yearlyPrice = yearlyPhase?.formattedPrice ?: "—"

    val monthlyOriginalPrice = monthlyPhase?.let {
        formatOriginalPrice(
            it.priceAmountMicros,
            it.priceCurrencyCode
        )
    }

    val yearlyOriginalPrice = yearlyPhase?.let {
        formatOriginalPrice(
            it.priceAmountMicros,
            it.priceCurrencyCode
        )
    }

    val savingsPercentage = "50% OFF"

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_premium2),
                    contentDescription = "Premium",
                    modifier = Modifier.size(68.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isPremium)
                    "Premium User 🎉"
                else
                    "Go Premium",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.onPrimaryContainer,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isPremium)
                    "Congratulations! Enjoy all premium features"
                else
                    "Unlock the full power of ChatBuilder",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = LightColorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(color = colors.primary)
            } else if (!isPremium) {

                PremiumPlanCard(
                    selected = selectedPlan == PremiumPlan.YEARLY,
                    title = "Yearly",
                    subtitle = "Best value • billed yearly",
                    price = yearlyPrice,
                    originalPrice = yearlyOriginalPrice,
                    secondaryText = "per year",
                    badge = savingsPercentage,
                    popular = false,
                    onPrimaryColor = onPrimaryColor,
                    onClick = {
                        selectedPlan = PremiumPlan.YEARLY
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                PremiumPlanCard(
                    selected = selectedPlan == PremiumPlan.MONTHLY,
                    title = "Monthly",
                    subtitle = "Flexible • cancel anytime",
                    price = monthlyPrice,
                    originalPrice = monthlyOriginalPrice,
                    secondaryText = "per month",
                    badge = null,
                    popular = false,
                    onPrimaryColor = onPrimaryColor,
                    onClick = {
                        selectedPlan = PremiumPlan.MONTHLY
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (activity != null) {
                            when (selectedPlan) {
                                PremiumPlan.MONTHLY ->
                                    billingManager.launchMonthlyPurchase(activity)

                                PremiumPlan.YEARLY ->
                                    billingManager.launchYearlyPurchase(activity)
                            }
                        }
                    },
                    enabled = !isPurchasing && selectedProduct != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightColorScheme.primary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = White
                        )
                    } else {
                        Text(
                            text = if (selectedPlan == PremiumPlan.YEARLY)
                                "Continue with Yearly"
                            else
                                "Continue with Monthly",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.CenterVertically)
                                .rotate(-90f),
                            painter = painterResource(R.drawable.ic_arrow),
                            contentDescription = null,
                            tint = White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = colors.onSecondaryContainer
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Secure payment powered by Google Play",
                        fontSize = 12.sp,
                        color = colors.onSecondaryContainer
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = LightColorScheme.primary.copy(alpha = 0.09f)
                    ),
                    border = BorderStroke(2.dp, LightColorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.primary
                        ) {
                            Text(
                                text = "ACTIVE PLAN",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = onPrimaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "ChatBuilder Premium",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Full Access Unlocked • All Features Active",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = LightColorScheme.onSecondaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PremiumFeaturesCard(onPrimaryColor)
        }
    }
}


@Composable
private fun PremiumPlanCard(
    selected: Boolean,
    title: String,
    subtitle: String,
    price: String,
    originalPrice: String?,
    secondaryText: String,
    badge: String?,
    popular: Boolean,
    onPrimaryColor: Color,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    val borderColor = if (selected) LightColorScheme.primary else colors.outlineVariant
    val containerColor =
        if (selected) LightColorScheme.primary.copy(alpha = 0.09f) else colors.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // Set to 0 to avoid shadow bleed on borders
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = LightColorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.onPrimaryContainer
                    )

                    if (popular) {
                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.primary
                        ) {
                            Text(
                                text = "POPULAR",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = onPrimaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = colors.onSecondaryContainer,
                    lineHeight = 16.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (originalPrice != null) {
                        Text(
                            text = originalPrice,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSecondaryContainer.copy(alpha = 0.5f),
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = price,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onPrimaryContainer
                    )
                }

                Text(
                    text = secondaryText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.onSecondaryContainer
                )

                if (badge != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.primary
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = onPrimaryColor
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PremiumFeaturesCard(onPrimaryColor: Color) {
    val colors = MaterialTheme.colorScheme

    val features = listOf(
        "No Ads" to "Enjoy a clean, uninterrupted experience",
        "Unlimited Free Imports" to "Import all your chat backups without any hidden limits",
        "Unlimited Free Exports with Media" to "Save and export your entire chat history including all media files",
        "Full Media Support" to "Photos, videos, documents, voice notes and more",
        "Pin Chats" to "Keep your most important conversations pinned to the very top",
        "Future Premium Features" to "Get instant access to all upcoming tools and enhancements"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Premium Features",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            features.forEach { (title, description) ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(LightColorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onPrimaryContainer
                        )

                        Text(
                            text = description,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = colors.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

private fun formatOriginalPrice(
    priceAmountMicros: Long,
    currencyCode: String
): String {

    val originalAmount = priceAmountMicros * 2 / 1_000_000.0

    val currency = Currency.getInstance(currencyCode)

    val formatter = NumberFormat.getCurrencyInstance(
        Locale.getDefault()
    )

    formatter.currency = currency

    return formatter.format(originalAmount)
}
