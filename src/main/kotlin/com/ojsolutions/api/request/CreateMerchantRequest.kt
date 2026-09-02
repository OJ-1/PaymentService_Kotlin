package com.ojsolutions.api.request

import com.ojsolutions.domain.MerchantType
import kotlinx.serialization.Serializable

@Serializable
data class CreateMerchantRequest(
    val msgId: String,
    val type: MerchantType,
    val name: String,
    val registrationNumber: String,
    val country: String,
    val mobileNumber: String,
    val email: String,
    val physicalAddress: String
)