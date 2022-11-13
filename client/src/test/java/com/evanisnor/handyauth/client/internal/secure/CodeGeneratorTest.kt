package com.evanisnor.handyauth.client.internal.secure

import com.google.common.truth.Truth
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random

class CodeGeneratorTest {

  @Test
  fun generatesCodeVerifier() {
    val codeGenerator = CodeGenerator(
      random = Random(0),
    )

    val codeVerifier = codeGenerator.generate(128)
    Truth.assertThat(codeVerifier).isEqualTo(
      "Oeh~ITt3qNg_MYNi6Hf6d9WgWX1e~vWUEjKKxpXB1u1LfW5lvfcKNl3vKutNly47VGR.228H9xWRiHaGoitYY6rRee8066IOSu00V6Skya5i44Z41Fz86Lt0qf_sEJCI",
    )
    Truth.assertThat(codeVerifier.length).isEqualTo(128)
  }

  @Test
  fun generatesCodeChallenge() {
    val codeGenerator = CodeGenerator(
      random = Random(0),
    )

    val codeVerifier = codeGenerator.generate(128)
    val codeChallenge = codeGenerator.codeChallenge(codeVerifier)

    Assert.assertEquals(
      "xv74p0V4Jz6vXzwCOI8Ds6flXSOwY6RQ_kElnhxvGZ4",
      codeChallenge,
    )
  }

  @Test
  fun generatesCodeChallenge_Const() {
    val codeGenerator = CodeGenerator(
      random = Random(0),
    )

    val codeChallenge =
      codeGenerator.codeChallenge("h_Z-zAIJ3LGnpyl9dD8HF7U-EZijSzQBn5eqQuDscZEuZVkP")

    Assert.assertEquals(
      "QXCgUPhEzG2eYj7NsvsmOymfqlABNhpCxW45gwxz7T4",
      codeChallenge,
    )
  }
}
