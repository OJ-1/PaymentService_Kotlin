package com.ojsolutions.api.dto

import com.ojsolutions.domain.MerchantType
import com.ojsolutions.domain.OwnerStatus
import kotlinx.serialization.Serializable

@Serializable
data class MerchantDto(
    val id: String,
    val createdDate: String,
    val updatedDate: String,
    val status: OwnerStatus,
    val type: MerchantType,
    val name: String,
    val registrationNumber: String,
    val country: String,
    val mobileNumber: String,
    val email: String,
    val physicalAddress: String
)