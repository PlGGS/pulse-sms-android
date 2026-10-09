package xyz.klinker.messenger.shared.util

import android.app.Activity
import xyz.klinker.messenger.api.implementation.Account

class PromotionUtils(private val context: Activity) {

    fun checkPromotions(onTrialExpired: () -> Unit) {
        if (trialExpired()) {
            onTrialExpired()
        }
    }

    private fun trialExpired(): Boolean {
        return Account.exists() && Account.subscriptionType == Account.SubscriptionType.FREE_TRIAL && Account.getDaysLeftInTrial() <= 0
    }

    
}
