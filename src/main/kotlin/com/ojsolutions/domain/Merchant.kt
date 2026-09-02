package com.ojsolutions.domain

import java.time.LocalDateTime
import java.util.UUID

data class Merchant(
    val id: UUID,
    val createdDate: LocalDateTime,
    val updatedDate: LocalDateTime,
    val status: OwnerStatus,
    val type: MerchantType,
    val name: String,
    val registrationNumber: String,
    val country: String,
    val mobileNumber: String,
    val email: String,
    val physicalAddress: String
)