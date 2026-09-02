package com.ojsolutions.application

import com.ojsolutions.api.dto.AccountDto
import com.ojsolutions.api.request.CreateAccountRequest
import com.ojsolutions.api.request.UpdateAccountRequest
import com.ojsolutions.api.response.*
import com.ojsolutions.domain.Account
import com.ojsolutions.domain.AccountStatus
import com.ojsolutions.domain.OwnerCategory
import com.ojsolutions.domain.port.LedgerPort
import com.ojsolutions.infrastructure.database.repository.AccountRepository
import com.ojsolutions.infrastructure.database.repository.CustomerRepository
import com.ojsolutions.infrastructure.database.repository.MerchantRepository
import com.ojsolutions.infrastructure.database.repository.SystemRepository
import java.time.LocalDateTime
import java.util.*

class AccountService(
    private val accountRepository: AccountRepository,
    private val customerRepository: CustomerRepository,
    private val merchantRepository: MerchantRepository,
    private val systemRepository: SystemRepository,
    private val ledgerPort: LedgerPort
) {

    // CREATE ACCOUNT
    fun createAccount(request: CreateAccountRequest): ApiResponse<CreateAccountResponse> {

        // Validate owner ID format
        val ownerId = try {
            UUID.fromString(request.ownerId)
        } catch (ex: IllegalArgumentException) {
            return ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "INVALID_OWNER_ID",
                        description = "Owner ID must be a valid UUID."
                    )
                )
            )
        }

        // Validate owner
        val ownerExists = when (request.ownerCategory) {
            OwnerCategory.CUSTOMER -> customerRepository.existsById(UUID.fromString(request.ownerId))

            OwnerCategory.MERCHANT -> merchantRepository.existsById(UUID.fromString(request.ownerId))

            OwnerCategory.SYSTEM -> systemRepository.existsById(UUID.fromString(request.ownerId))
        }

        if (!ownerExists) {
            return ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "OWNER_NOT_FOUND",
                        description = "The specified owner does not exist."
                    )
                )
            )
        }

        // Set the account prefix based on the account type
        var accountPrefix = ""
        when (request.ownerCategory) {
            OwnerCategory.CUSTOMER -> accountPrefix = "220"
            OwnerCategory.MERCHANT -> accountPrefix = "330"
            OwnerCategory.SYSTEM -> accountPrefix = "100"
        }

        // Generate a unique account number
        var accountNumber: String

        do {
            val accNo = (100_000_000L..999_999_999L).random().toString()
            accountNumber = accountPrefix + accNo
        } while (accountRepository.existsByAccountNumber(accountNumber))

        // Build domain object
        val accountId = UUID.randomUUID()
        val now = LocalDateTime.now()

        val account = Account(
            id = accountId,
            createdDate = now,
            updatedDate = now,
            ownerId = UUID.fromString(request.ownerId),
            ownerCategory = request.ownerCategory,
            status = AccountStatus.ACTIVE,
            ledger = request.ledger,
            accountNumber = accountNumber,
            accountType = request.accountType,
            description = request.description
        )

        // Create account in TigerBeetle
        ledgerPort.createAccount(
            accountNumber = account.accountNumber,
            accountType = account.accountType
        )

        // Persist account metadata
        accountRepository.create(account)

        return ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CreateAccountResponse(
                account.accountNumber
            )
        )
    }

    // UPDATE ACCOUNT
    fun updateAccount(accountNumber: String, request: UpdateAccountRequest): ApiResponse<CreateReferenceResponse> {

        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            return ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "ACCOUNT_NOT_FOUND",
                        description = "Account does not exist."
                    )
                )
            )
        }

        val existing = accountRepository.get(accountNumber)

        val updatedAccount = Account(
            id = existing.id,
            createdDate = existing.createdDate,
            updatedDate = LocalDateTime.now(),
            ownerId = existing.ownerId,
            ownerCategory = existing.ownerCategory,
            status = request.status ?: existing.status,
            ledger = existing.ledger,
            accountNumber = existing.accountNumber,
            accountType = existing.accountType,
            description = request.description ?: existing.description
        )

        accountRepository.update(updatedAccount)

        return ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = CreateReferenceResponse(
                updatedAccount.accountNumber
            )
        )
    }

    // GET ACCOUNT
    fun getAccount(accountNumber: String): ApiResponse<AccountResponse> {

        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            return ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "ACCOUNT_NOT_FOUND",
                        description = "Account does not exist."
                    )
                )
            )
        }

        // Retrieve the account details from the Account DB repo and the Ledger
        val account = accountRepository.get(accountNumber)
        val ledger = ledgerPort.getAccount(accountNumber)

        val accountDto = AccountDto(
            id = account.id.toString(),
            createdDate = account.createdDate.toString(),
            updatedDate = account.updatedDate.toString(),
            status = account.status,
            ownerCategory = account.ownerCategory,
            ownerId = account.ownerId.toString(),
            ledger = account.ledger,
            accountNumber = account.accountNumber,
            accountType = account.accountType,
            description = account.description
        )

        return ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = AccountResponse(
                account = accountDto,
                ledger = ledger
            )
        )
    }

    // GET ALL ACCOUNTS
    fun getAccounts(): ApiResponse<AccountsResponse> {

        val accounts = accountRepository.getAll()

        val response = accounts.map { account ->

            // Retrieve account info from the Ledger
            val ledger = ledgerPort.getAccount(account.accountNumber)

            AccountResponse(
                account = AccountDto(
                    id = account.id.toString(),
                    createdDate = account.createdDate.toString(),
                    updatedDate = account.updatedDate.toString(),
                    status = account.status,
                    ownerCategory = account.ownerCategory,
                    ownerId = account.ownerId.toString(),
                    ledger = account.ledger,
                    accountNumber = account.accountNumber,
                    accountType = account.accountType,
                    description = account.description
                ),
                ledger = ledger
            )
        }

        return ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = AccountsResponse(
                accounts = response
            )
        )
    }

    // GET TRANSFERS
    fun getTransfers(accountNumber: String ,from: Long? = null, to: Long? = null): ApiResponse<TransfersResponse> {

        if (!accountRepository.existsByAccountNumber(accountNumber)) {
            return ApiResponse(
                resultCode = ResultCode.FAILED,
                errors = listOf(
                    ApiError(
                        code = "ACCOUNT_NOT_FOUND",
                        description = "Account does not exist."
                    )
                )
            )
        }

        val account = accountRepository.get(accountNumber)

        val transfers = ledgerPort.getTransfers(
            accountNumber = accountNumber,
            from = from,
            to = to
        )

        val accountDto = AccountDto(
            id = account.id.toString(),
            createdDate = account.createdDate.toString(),
            updatedDate = account.updatedDate.toString(),
            status = account.status,
            ownerCategory = account.ownerCategory,
            ownerId = account.ownerId.toString(),
            ledger = account.ledger,
            accountNumber = account.accountNumber,
            accountType = account.accountType,
            description = account.description
        )

        return ApiResponse(
            resultCode = ResultCode.SUCCESS,
            data = TransfersResponse(
                account = accountDto,
                transfers = transfers
            )
        )
    }
}