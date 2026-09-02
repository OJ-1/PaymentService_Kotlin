package com.ojsolutions.api.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerRequest(
    val msgId: String,
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