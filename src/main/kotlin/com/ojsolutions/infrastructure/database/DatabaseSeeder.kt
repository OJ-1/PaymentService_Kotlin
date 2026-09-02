package com.ojsolutions.infrastructure.database

import com.ojsolutions.infrastructure.database.table.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDateTime
import kotlin.uuid.Uuid

object DatabaseSeeder {

    fun seed() = transaction {

        // Prevent duplicate development seed data
        if (CustomerTable.selectAll().count() > 0)
            return@transaction

        //====================================================
        // System
        //====================================================

        val systemSettlementId = Uuid.random()
        val systemFeeId = Uuid.random()

        val systemSettlementCreated = LocalDateTime.now()

        SystemTable.insert {
            it[id] = systemSettlementId
            it[status] = "ACTIVE"
            it[name] = "SETTLEMENTS"
            it[description] = "Settlement account"
            it[createdDate] = systemSettlementCreated
            it[updatedDate] = systemSettlementCreated
        }

        val systemFeeCreated = LocalDateTime.now()

        SystemTable.insert {
            it[id] = systemFeeId
            it[status] = "ACTIVE"
            it[name] = "FEES"
            it[description] = "Fee settlement account"
            it[createdDate] = systemFeeCreated
            it[updatedDate] = systemFeeCreated
        }

        //====================================================
        // Customers
        //====================================================

        val customer1Id = Uuid.random()
        val customer2Id = Uuid.random()

        val customer1Created = LocalDateTime.now()

        CustomerTable.insert {
            it[id] = customer1Id
            it[status] = "ACTIVE"
            it[title] = "Mr"
            it[firstName] = "John"
            it[lastName] = "Smith"
            it[identityNumber] = "9001015009087"
            it[passportNumber] = null
            it[country] = "ZA"
            it[mobileNumber] = "0821111111"
            it[email] = "john.smith@email.com"
            it[physicalAddress] = "1 Main Road, Johannesburg"
            it[createdDate] = customer1Created
            it[updatedDate] = customer1Created
        }

        val customer2Created = LocalDateTime.now()

        CustomerTable.insert {
            it[id] = customer2Id
            it[status] = "ACTIVE"
            it[title] = "Mrs"
            it[firstName] = "Jane"
            it[lastName] = "Doe"
            it[identityNumber] = "9202025009088"
            it[passportNumber] = null
            it[country] = "ZA"
            it[mobileNumber] = "0822222222"
            it[email] = "jane.doe@email.com"
            it[physicalAddress] = "2 Main Road, Johannesburg"
            it[createdDate] = customer2Created
            it[updatedDate] = customer2Created
        }

        //====================================================
        // Merchants
        //====================================================

        val merchant1Id = Uuid.random()
        val merchant2Id = Uuid.random()

        val merchant1Created = LocalDateTime.now()

        MerchantTable.insert {
            it[id] = merchant1Id
            it[status] = "ACTIVE"
            it[type] = "AIRTIME"
            it[name] = "Vodacom"
            it[registrationNumber] = "REG001"
            it[country] = "ZA"
            it[mobileNumber] = "0800000001"
            it[email] = "support@vodacom.co.za"
            it[physicalAddress] = "Midrand"
            it[createdDate] = merchant1Created
            it[updatedDate] = merchant1Created
        }

        val merchant2Created = LocalDateTime.now()

        MerchantTable.insert {
            it[id] = merchant2Id
            it[status] = "ACTIVE"
            it[type] = "ELECTRICITY"
            it[name] = "Eskom"
            it[registrationNumber] = "REG002"
            it[country] = "ZA"
            it[mobileNumber] = "0800000002"
            it[email] = "support@eskom.co.za"
            it[physicalAddress] = "Johannesburg"
            it[createdDate] = merchant2Created
            it[updatedDate] = merchant2Created
        }

        //====================================================
        // Accounts
        //====================================================

        // System Settlement Account
        val settlementAccountCreated = LocalDateTime.now()

        AccountTable.insert {
            it[id] = Uuid.random()
            it[status] = "ACTIVE"
            it[ownerId] = systemSettlementId
            it[ownerCategory] = "SYSTEM"
            it[ledger] = "PAYMENTS"
            it[accountNumber] = "1"
            it[accountType] = "SETTLEMENT"
            it[description] = "Settlement Account"
            it[createdDate] = settlementAccountCreated
            it[updatedDate] = settlementAccountCreated
        }

        // System Fee Account
        val feeAccountCreated = LocalDateTime.now()

        AccountTable.insert {
            it[id] = Uuid.random()
            it[status] = "ACTIVE"
            it[ownerId] = systemFeeId
            it[ownerCategory] = "SYSTEM"
            it[ledger] = "PAYMENTS"
            it[accountNumber] = "2"
            it[accountType] = "FEE"
            it[description] = "Fee Settlement Account"
            it[createdDate] = feeAccountCreated
            it[updatedDate] = feeAccountCreated
        }

        // Customer 1 Transactional Account
        val customer1AccountCreated = LocalDateTime.now()

        AccountTable.insert {
            it[id] = Uuid.random()
            it[status] = "ACTIVE"
            it[ownerId] = customer1Id
            it[ownerCategory] = "CUSTOMER"
            it[ledger] = "PAYMENTS"
            it[accountNumber] = "1001"
            it[accountType] = "TRANSACTIONAL"
            it[description] = "John Transactional Account"
            it[createdDate] = customer1AccountCreated
            it[updatedDate] = customer1AccountCreated
        }

        // Customer 2 Transactional Account
        val customer2AccountCreated = LocalDateTime.now()

        AccountTable.insert {
            it[id] = Uuid.random()
            it[status] = "ACTIVE"
            it[ownerId] = customer2Id
            it[ownerCategory] = "CUSTOMER"
            it[ledger] = "PAYMENTS"
            it[accountNumber] = "1002"
            it[accountType] = "TRANSACTIONAL"
            it[description] = "Jane Transactional Account"
            it[createdDate] = customer2AccountCreated
            it[updatedDate] = customer2AccountCreated
        }

        // Merchant 1 Settlement Account
        val merchant1AccountCreated = LocalDateTime.now()

        AccountTable.insert {
            it[id] = Uuid.random()
            it[status] = "ACTIVE"
            it[ownerId] = merchant1Id
            it[ownerCategory] = "MERCHANT"
            it[ledger] = "PAYMENTS"
            it[accountNumber] = "2001"
            it[accountType] = "TRANSACTIONAL"
            it[description] = "Vodacom Settlement Account"
            it[createdDate] = merchant1AccountCreated
            it[updatedDate] = merchant1AccountCreated
        }

        // Merchant 2 Settlement Account
        val merchant2AccountCreated = LocalDateTime.now()

        AccountTable.insert {
            it[id] = Uuid.random()
            it[status] = "ACTIVE"
            it[ownerId] = merchant2Id
            it[ownerCategory] = "MERCHANT"
            it[ledger] = "PAYMENTS"
            it[accountNumber] = "2002"
            it[accountType] = "TRANSACTIONAL"
            it[description] = "Eskom Settlement Account"
            it[createdDate] = merchant2AccountCreated
            it[updatedDate] = merchant2AccountCreated
        }

        //====================================================
        // Fee Types
        //====================================================

        // USD Fees
        
        val mobileTopUpFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "MOBILE_TOP_UP"
            it[asset] = "USD"
            it[rate] = "0.0015"
            it[description] = "0.15% Mobile Top Up Fee"
            it[createdDate] = mobileTopUpFeeCreated
            it[updatedDate] = mobileTopUpFeeCreated
        }

        val electricityFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "ELECTRICITY_PURCHASE"
            it[asset] = "USD"
            it[rate] = "0.10"
            it[description] = "10% Electricity Fee"
            it[createdDate] = electricityFeeCreated
            it[updatedDate] = electricityFeeCreated
        }

        val gamingVoucherFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "GAMING_VOUCHER"
            it[asset] = "USD"
            it[rate] = "0.12"
            it[description] = "12% Gaming Voucher Fee"
            it[createdDate] = gamingVoucherFeeCreated
            it[updatedDate] = gamingVoucherFeeCreated
        }

        val retailTopUpFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "RETAIL_TOP_UP"
            it[asset] = "USD"
            it[rate] = "0.08"
            it[description] = "8% Retail Credit Fee"
            it[createdDate] = retailTopUpFeeCreated
            it[updatedDate] = retailTopUpFeeCreated
        }

        //===================
        // GBP fees

        val mobileTopUpGBPFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "MOBILE_TOP_UP"
            it[asset] = "GBP"
            it[rate] = "0.0013"
            it[description] = "0.13% Mobile Top Up Fee"
            it[createdDate] = mobileTopUpGBPFeeCreated
            it[updatedDate] = mobileTopUpGBPFeeCreated
        }

        val electricityGBPFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "ELECTRICITY_PURCHASE"
            it[asset] = "GBP"
            it[rate] = "0.09"
            it[description] = "9% Electricity Fee"
            it[createdDate] = electricityGBPFeeCreated
            it[updatedDate] = electricityGBPFeeCreated
        }

        val gamingVoucherGBPFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "GAMING_VOUCHER"
            it[asset] = "GBP"
            it[rate] = "0.14"
            it[description] = "14% Gaming Voucher Fee"
            it[createdDate] = gamingVoucherGBPFeeCreated
            it[updatedDate] = gamingVoucherGBPFeeCreated
        }

        val retailTopUpGBPFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "RETAIL_TOP_UP"
            it[asset] = "GBP"
            it[rate] = "0.07"
            it[description] = "7% Retail Credit Fee"
            it[createdDate] = retailTopUpGBPFeeCreated
            it[updatedDate] = retailTopUpGBPFeeCreated
        }

        //===================
        // ZAR fees

        val mobileTopUpZARFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "MOBILE_TOP_UP"
            it[asset] = "ZAR"
            it[rate] = "0.0017"
            it[description] = "0.17% Mobile Top Up Fee"
            it[createdDate] = mobileTopUpZARFeeCreated
            it[updatedDate] = mobileTopUpZARFeeCreated
        }

        val electricityZARFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "ELECTRICITY_PURCHASE"
            it[asset] = "ZAR"
            it[rate] = "0.15"
            it[description] = "15% Electricity Fee"
            it[createdDate] = electricityZARFeeCreated
            it[updatedDate] = electricityZARFeeCreated
        }

        val gamingVoucherZARFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "GAMING_VOUCHER"
            it[asset] = "ZAR"
            it[rate] = "0.14"
            it[description] = "14% Gaming Voucher Fee"
            it[createdDate] = gamingVoucherZARFeeCreated
            it[updatedDate] = gamingVoucherZARFeeCreated
        }

        val retailTopUpZARFeeCreated = LocalDateTime.now()

        FeeTypeTable.insert {
            it[id] = Uuid.random()
            it[type] = "RETAIL_TOP_UP"
            it[asset] = "ZAR"
            it[rate] = "0.11"
            it[description] = "11% Retail Credit Fee"
            it[createdDate] = retailTopUpZARFeeCreated
            it[updatedDate] = retailTopUpZARFeeCreated
        }
        
        

        println("Database seeded successfully.")
    }
}