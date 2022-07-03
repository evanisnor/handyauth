package com.evanisnor.handyauth.client.fakes

import com.evanisnor.handyauth.client.internal.secure.AuthorizationValidator
import com.evanisnor.handyauth.client.internal.secure.CodeGenerator
import com.evanisnor.handyauth.client.internal.secure.DefaultSecureModule
import com.evanisnor.handyauth.client.internal.secure.SecureModule

internal class TestSecureModule(
    private val defaultSecureModule: DefaultSecureModule = DefaultSecureModule(),
    private val testAuthorizationValidator: TestAuthorizationValidator?
) : SecureModule {


    override fun authorizationValidator(): AuthorizationValidator =
        testAuthorizationValidator ?: defaultSecureModule.authorizationValidator()

    override fun codeGenerator(): CodeGenerator = defaultSecureModule.codeGenerator()
}