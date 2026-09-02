package com.ojsolutions.domain

import java.time.LocalDateTime
import java.util.UUID

data class Customer(
    val id: UUID,
    val createdDate: LocalDateTime,
    val updatedDate: LocalDateTime,
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