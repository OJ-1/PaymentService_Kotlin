package com.ojsolutions.infrastructure.di

import com.ojsolutions.application.AccountService
import com.ojsolutions.application.CustomerService
import com.ojsolutions.application.MerchantService
import com.ojsolutions.application.PaymentService
import com.ojsolutions.application.SystemService
import com.ojsolutions.infrastructure.database.repository.SystemRepository
import com.ojsolutions.domain.port.LedgerPort
import com.ojsolutions.domain.port.WorkflowPort
import com.ojsolutions.infrastructure.database.repository.AccountRepository
import com.ojsolutions.infrastructure.database.repository.CustomerRepository
import com.ojsolutions.infrastructure.database.repository.FeeTypeRepository
import com.ojsolutions.infrastructure.database.repository.MerchantRepository
import com.ojsolutions.infrastructure.database.repository.PaymentRepository
import com.ojsolutions.infrastructure.ledger.TigerBeetleLedgerAdapter
import com.ojsolutions.infrastructure.workflow.RestateWorkflowAdapter
import dev.restate.client.Client
import org.koin.dsl.module

val appModule = module {

    //========================================================= Repositories \/

    single { CustomerRepository() }

    single { MerchantRepository() }

    single { SystemRepository() }

    single { AccountRepository() }

    single { PaymentRepository() }

    single { FeeTypeRepository() }

    //========================================================= Repositories /\

    //========================================================= Infrastructure \/

    single<LedgerPort> { TigerBeetleLedgerAdapter() }

    single { Client.connect("http://restate:8080") }

    single<WorkflowPort> {
        RestateWorkflowAdapter(
            restateClient = get()
        )
    }

    //========================================================= Infrastructure /\

    //========================================================= Services \/

    single {
        CustomerService(
            customerRepository = get()
        )
    }

    single {
        MerchantService(
            merchantRepository = get()
        )
    }

    single {
        SystemService(
            systemRepository = get()
        )
    }

    single {
        AccountService(
            accountRepository = get(),
            customerRepository = get(),
            merchantRepository = get(),
            systemRepository = get(),
            ledgerPort = get()
        )
    }

    single {
        PaymentService(
            workflowPort = get(),
            paymentRepository = get()
        )
    }

    //========================================================= Services /\


    //===
}