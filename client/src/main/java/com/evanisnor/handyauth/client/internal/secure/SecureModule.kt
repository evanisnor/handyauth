package com.evanisnor.handyauth.client.internal.secure

internal interface SecureModule {

  fun authorizationValidator(): AuthorizationValidator

  fun codeGenerator(): CodeGenerator
}

internal class DefaultSecureModule : SecureModule {

  override fun authorizationValidator(): AuthorizationValidator = DefaultAuthorizationValidator()

  override fun codeGenerator(): CodeGenerator = CodeGenerator()
}
