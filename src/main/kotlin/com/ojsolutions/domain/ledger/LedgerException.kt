package com.ojsolutions.domain.ledger

sealed class LedgerException(message: String) : RuntimeException(message)

class InsufficientFundsException : LedgerException("Customer account has insufficient funds.")

class AccountNotFoundException : LedgerException("Account not found.")

class CustomerAccountNotFoundException : LedgerException("Customer account not found.")

class MerchantAccountNotFoundException : LedgerException("Merchant account not found.")

class TransferNotFoundException : LedgerException("Transfer not found.")

class LedgerOperationException(message: String) : LedgerException(message)

class DebtorAccountNotFoundException : LedgerException("Debtor account not found.")

class CreditorAccountNotFoundException : LedgerException("Creditor account not found.")