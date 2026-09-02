package com.ojsolutions.application

import com.ojsolutions.api.dto.CustomerDto
import com.ojsolutions.api.request.CreateCustomerRequest
import com.ojsolutions.api.request.UpdateCustomerRequest
import com.ojsolutions.api.response.ApiError
import com.ojsolutions.api.response.ApiResponse
import com.ojsolutions.api.response.CreateReferenceResponse
import com.ojsolutions.api.response.CustomerResponse
import com.ojsolutions.api.response.CustomersResponse
import com.ojsolutions.api.response.ResultCode
import com.ojsolutions.domain.*
import com.ojsolutions.infrastructure.database.repository.CustomerRepository
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class CustomerService(private val customerRepository: CustomerRepository) {

    // CREATE CUSTOMER
    fun createCustomer(request: CreateCustomerRequest): ApiResponse<CreateReferenceResponse> = transaction {

        // Validate the ID number does not already exist
        if (customerRepository.existsByIdentityNumber(request.identityNumber)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "IDENTITY_NUMBER_ALREADY_EXISTS",
                        description = "Identity Number already exists."
                    )
                )
            )
        }

        // IF Passport is provided - validate is does not already exist
        request.passportNumber?.let {
            if (customerRepository.existsByPassportNumber(it)) {
                return@transaction ApiResponse(
                    resultCode = ResultCode.FAILED,
                    errors = listOf(
                        ApiError(
                            code = "PASSPORT_NUMBER_ALREADY_EXISTS",
                            description = "Passport Number already exists."
                        )
                    )
                )
            }
        }

        // Check the email address does not already exist
        if (customerRepository.existsByEmail(request.email)) {
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

        // Generate the CustomerId
        val customerId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val customer = Customer(
            id = customerId,
            createdDate = now,
            updatedDate = now,
            status = OwnerStatus.ACTIVE,
            title = request.title,
            firstName = request.firstName,
            lastName = request.lastName,
            identityNumber = request.identityNumber,
            passportNumber = request.passportNumber,
            country = request.country,
            mobileNumber = request.mobileNumber,
            email = request.email,
            physicalAddress = request.physicalAddress
        )

        // Add the CUSTOMER record in SQL DB
        customerRepository.create(customer)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CreateReferenceResponse(customerId.toString())
        )
    }

    // GET ALL CUSTOMERS
    fun getCustomers(): ApiResponse<CustomersResponse> = transaction {

        val customers = customerRepository.getAll()

        val response = customers.map { customer ->

            CustomerDto(
                id = customer.id.toString(),
                createdDate = customer.createdDate.toString(),
                updatedDate = customer.updatedDate.toString(),
                status = customer.status,
                title = customer.title,
                firstName = customer.firstName,
                lastName = customer.lastName,
                identityNumber = customer.identityNumber,
                passportNumber = customer.passportNumber,
                country = customer.country,
                mobileNumber = customer.mobileNumber,
                email = customer.email,
                physicalAddress = customer.physicalAddress
            )
        }

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CustomersResponse(
                customers = response
            )
        )
    }

    // GET CUSTOMER
    fun getCustomer(id: UUID): ApiResponse<CustomerResponse> = transaction {

        if (!customerRepository.existsById(id)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "CUSTOMER_NOT_FOUND",
                        description = "Customer does not exist."
                    )
                )
            )
        }

        val customer = customerRepository.get(id)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CustomerResponse(
                customer = CustomerDto(
                    id = customer.id.toString(),
                    createdDate = customer.createdDate.toString(),
                    updatedDate = customer.updatedDate.toString(),
                    status = customer.status,
                    title = customer.title,
                    firstName = customer.firstName,
                    lastName = customer.lastName,
                    identityNumber = customer.identityNumber,
                    passportNumber = customer.passportNumber,
                    country = customer.country,
                    mobileNumber = customer.mobileNumber,
                    email = customer.email,
                    physicalAddress = customer.physicalAddress
                )
            )
        )
    }

    // UPDATE CUSTOMER
    fun updateCustomer(id: UUID, request: UpdateCustomerRequest): ApiResponse<CreateReferenceResponse> = transaction {

        if (!customerRepository.existsById(id)) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "CUSTOMER_NOT_FOUND",
                        description = "Customer does not exist."
                    )
                )
            )
        }

        val existing = customerRepository.get(id)

        // Identity Number
        if (
            request.identityNumber != null &&
            request.identityNumber != existing.identityNumber &&
            customerRepository.existsByIdentityNumber(request.identityNumber)
        ) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "IDENTITY_NUMBER_ALREADY_EXISTS",
                        description = "Identity Number already exists."
                    )
                )
            )
        }

        // Passport Number
        if (
            request.passportNumber != null &&
            request.passportNumber != existing.passportNumber &&
            customerRepository.existsByPassportNumber(request.passportNumber)
        ) {
            return@transaction ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "PASSPORT_NUMBER_ALREADY_EXISTS",
                        description = "Passport Number already exists."
                    )
                )
            )
        }

        // Email
        if (
            request.email != null &&
            request.email != existing.email &&
            customerRepository.existsByEmail(request.email)
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

        val updatedCustomer = Customer(
            id = existing.id,
            createdDate = existing.createdDate,
            updatedDate = LocalDateTime.now(),
            status = request.status ?: existing.status,
            title = request.title ?: existing.title,
            firstName = request.firstName ?: existing.firstName,
            lastName = request.lastName ?: existing.lastName,
            identityNumber = request.identityNumber ?: existing.identityNumber,
            passportNumber = request.passportNumber ?: existing.passportNumber,
            country = request.country ?: existing.country,
            mobileNumber = request.mobileNumber ?: existing.mobileNumber,
            email = request.email ?: existing.email,
            physicalAddress = request.physicalAddress ?: existing.physicalAddress
        )

        customerRepository.update(updatedCustomer)

        return@transaction ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CreateReferenceResponse(
                updatedCustomer.id.toString()
            )
        )
    }

}