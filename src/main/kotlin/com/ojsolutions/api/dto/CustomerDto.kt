package com.ojsolutions.api.dto

import com.ojsolutions.domain.OwnerStatus
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto (
    val id: String,
    val createdDate: String,
    val updatedDate: String,
    val status: OwnerStatus,
    val title: String,
    val firstName: String,
    val lastName: String,
    val identityNumber: String,
    val passportNumber: String?,
    val country: String,
    val mobileNumber: String,
    val email: String,
    val physicalAddress: String
)