package com.ojsolutions.infrastructure.workflow

import com.ojsolutions.infrastructure.database.repository.AccountRepository
import com.ojsolutions.infrastructure.database.repository.FeeTypeRepository
import com.ojsolutions.infrastructure.database.repository.PaymentRepository
import com.ojsolutions.domain.port.LedgerPort
import dev.restate.sdk.http.vertx.RestateHttpServer
import dev.restate.sdk.kotlin.endpoint.endpoint

fun startRestate(
    accountRepository: AccountRepository,
    feeTypeRepository: FeeTypeRepository,
    ledgerPort: LedgerPort,
    paymentRepository: PaymentRepository
) {

    val paymentWorkflow = PaymentWorkflow(
        accountRepository = accountRepository,
        feeTypeRepository = feeTypeRepository,
        ledgerPort = ledgerPort,
        paymentRepository = paymentRepository
    )

    RestateHttpServer.listen(
        endpoint {
            bind(paymentWorkflow)
        },
        9090
    )
}