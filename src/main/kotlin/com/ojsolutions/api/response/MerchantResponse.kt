package com.ojsolutions.api.response

import com.ojsolutions.api.dto.MerchantDto
import kotlinx.serialization.Serializable

@Serializable
data class MerchantResponse(
    val merchant: MerchantDto
)