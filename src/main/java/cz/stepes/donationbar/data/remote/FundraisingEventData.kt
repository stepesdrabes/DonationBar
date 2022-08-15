package cz.stepes.donationbar.data.remote

import com.google.gson.annotations.SerializedName

data class FundraisingEventData(
    @SerializedName("fundraiserGoalAmount")
    val goal: Double,
    @SerializedName("totalAmountRaised")
    val amountRaised: Double,
)