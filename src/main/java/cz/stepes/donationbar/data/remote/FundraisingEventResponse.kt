package cz.stepes.donationbar.data.remote

import com.google.gson.annotations.SerializedName

data class FundraisingEventResponse(
    @SerializedName("data")
    val fundraisingEventData: FundraisingEventData,
)