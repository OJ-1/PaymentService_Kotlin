package com.ojsolutions.application

import com.ojsolutions.api.dto.MerchantDto
import com.ojsolutions.api.request.CreateMerchantRequest
import com.ojsolutions.api.request.UpdateMerchantRequest
import com.ojsolutions.api.response.ApiError
import com.ojsolutions.api.response.ApiResponse
import com.ojsolutions.api.response.CreateReferenceResponse
import com.ojsolutions.api.response.MerchantResponse
import com.ojsolutions.api.response.MerchantsResponse
import com.ojsolutions.api.response.ResultCode
import com.ojsolutions.domain.Merchant
import com.ojsolutions.domain.OwnerStatus
import com.ojsolutions.infrastructure.database.repository.MerchantRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

class MerchantService(private val merchantRepository: MerchantRepository) {

    // CREATE MERCHANT
    fun createMerchant(request: CreateMerchantRequest): ApiResponse<CreateReferenceResponse> = transaction {

        // Check the Merchant name does not already exist
        if (merchantRepository.existsByName(request.name)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "MERCHANT_NAME_ALREADY_EXISTS",
                        description = "Merchant Name already exists."
                    )
                )
            )
        }

        // Check the Merchant Registration Number does not already exist
        if (merchantRepository.existsByRegistrationNumber(request.registrationNumber)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "REGISTRATION_NUMBER_ALREADY_EXISTS",
                        description = "Registration Number already exists."
                    )
                )
            )
        }

        // Check the Merchant email does not already exist
        if (merchantRepository.existsByEmail(request.email)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "EMAIL_ALREADY_EXISTS",
                        description = "Email address already exists."
                    )
                )
            )
        }

        // Generate the MerchantId
        val merchantId = UUID.randomUUID()
        val now = java.time.LocalDateTime.now()

        val merchant = Merchant(
            id = merchantId,
            createdDate = now,
            updatedDate = now,
            status = OwnerStatus.ACTIVE,
            type = request.type,
            name = request.name,
            registrationNumber = request.registrationNumber,
            country = request.country,
            mobileNumber = request.mobileNumber,
            email = request.email,
            physicalAddress = request.physicalAddress
        )

        // Add the merchant record in SQL DB
        merchantRepository.create(merchant)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CreateReferenceResponse(merchantId.toString())
        )
    }

    // GET ALL MERCHANTS
    fun getMerchants(): ApiResponse<MerchantsResponse> = transaction {

        val merchants = merchantRepository.getAll()

        val response = merchants.map { merchant ->
            MerchantDto(
                id = merchant.id.toString(),
                createdDate = merchant.createdDate.toString(),
                updatedDate = merchant.updatedDate.toString(),
                status = merchant.status,
                type = merchant.type,
                name = merchant.name,
                registrationNumber = merchant.registrationNumber,
                country = merchant.country,
                mobileNumber = merchant.mobileNumber,
                email = merchant.email,
                physicalAddress = merchant.physicalAddress
            )
        }

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = MerchantsResponse(
                merchants = response
            )
        )
    }

    // GET MERCHANT
    fun getMerchant(id: UUID): ApiResponse<MerchantResponse> = transaction {

        if (!merchantRepository.existsById(id)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "MERCHANT_NOT_FOUND",
                        description = "Merchant does not exist."
                    )
                )
            )
        }

        val merchant = merchantRepository.get(id)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = MerchantResponse(
                merchant = MerchantDto(
                    id = merchant.id.toString(),
                    createdDate = merchant.createdDate.toString(),
                    updatedDate = merchant.updatedDate.toString(),
                    status = merchant.status,
                    type = merchant.type,
                    name = merchant.name,
                    registrationNumber = merchant.registrationNumber,
                    country = merchant.country,
                    mobileNumber = merchant.mobileNumber,
                    email = merchant.email,
                    physicalAddress = merchant.physicalAddress
                )
            )
        )
    }

    // UPDATE MERCHANT
    fun updateMerchant(
        id: UUID,
        request: UpdateMerchantRequest
    ): ApiResponse<CreateReferenceResponse> = transaction {

        if (!merchantRepository.existsById(id)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "MERCHANT_NOT_FOUND",
                        description = "Merchant does not exist."
                    )
                )
            )
        }

        val existing = merchantRepository.get(id)

        // Registration Number
        if (
            request.registrationNumber != null &&
            request.registrationNumber != existing.registrationNumber &&
            merchantRepository.existsByRegistrationNumber(request.registrationNumber)
        ) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "REGISTRATION_NUMBER_ALREADY_EXISTS",
                        description = "Registration Number already exists."
                    )
                )
            )
        }

        // Email
        if (
            request.email != null &&
            request.email != existing.email &&
            merchantRepository.existsByEmail(request.email)
        ) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "EMAIL_ALREADY_EXISTS",
                        description = "Email address already exists."
                    )
                )
            )
        }

        val updatedMerchant = Merchant(
            id = existing.id,
            createdDate = existing.createdDate,
            updatedDate = java.time.LocalDateTime.now(),
            status = request.status ?: existing.status,
            type = request.type ?: existing.type,
            name = request.name ?: existing.name,
            registrationNumber = request.registrationNumber ?: existing.registrationNumber,
            country = request.country ?: existing.country,
            mobileNumber = request.mobileNumber ?: existing.mobileNumber,
            email = request.email ?: existing.email,
            physicalAddress = request.physicalAddress ?: existing.physicalAddress
        )

        merchantRepository.update(updatedMerchant)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CreateReferenceResponse(
                updatedMerchant.id.toString()
            )
        )
    }


    //===
}