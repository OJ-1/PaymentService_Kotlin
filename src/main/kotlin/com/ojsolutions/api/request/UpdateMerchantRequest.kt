package com.ojsolutions.api.request

import com.ojsolutions.domain.MerchantType
import com.ojsolutions.domain.OwnerStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMerchantRequest (
    val msgId: String,
    val status: OwnerStatus? = null,
    val type: MerchantType? = null,
    val name: String? = null,
    val registrationNumber: String? = null,
    val country: String? = null,
    val mobileNumber: String? = null,
    val email: String? = null,
    val physicalAddress: String? = null
)