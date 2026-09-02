package com.ojsolutions.api.request

import com.ojsolutions.domain.OwnerStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCustomerRequest (
    val msgId: String,
    val status: OwnerStatus? = null,
    val title: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val identityNumber: String? = null,
    val passportNumber: String? = null,
    val country: String? = null,
    val mobileNumber: String? = null,
    val email: String? = null,
    val physicalAddress: String? = null
)