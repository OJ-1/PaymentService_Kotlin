package com.ojsolutions.domain

enum class OwnerCategory {
    CUSTOMER,
    MERCHANT,
    SYSTEM
}

enum class OwnerStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}

enum class AccountStatus {
    ACTIVE,
    SUSPENDED,
    RESTRICTED,
    DORMANT,
    CLOSED
}

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REJECTED,
    REVERSED
}

enum class AccountType {
    TRANSACTIONAL,
    SETTLEMENT,
    FEE,
    RESERVE
}

enum class MerchantType {
    AIRTIME,
    ELECTRICITY,
    GAMING,
    RETAIL
}

enum class Asset {
    USD,
    GBP,
    EUR,
    ZAR
}

enum class AssetType {
    FIAT
}

enum class PaymentType {
    MOBILE_TOP_UP,
    ELECTRICITY_PURCHASE,
    GAMING_VOUCHER,
    RETAIL_TOP_UP
}

enum class Ledger(val id: Int) {
    PAYMENTS(1),
    SETTLEMENT(2)
}

enum class TransferCodes(val id: Int) {
    PAYMENT(1),
    FUND_CUSTOMER(2),
    FEE(3)
}

enum class LedgerTransferResult {
    SUCCESS,
    INSUFFICIENT_FUNDS,
    DEBTOR_ACCOUNT_NOT_FOUND,
    CREDITOR_ACCOUNT_NOT_FOUND
}