package org.oewntk.json

import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class TestValidation {

    // Test Schema
    val schemaJson = """
        {
          "${'$'}schema-model.json": "https://json-schema.org/draft/2020-12/schema",
          "type": "object",
          "properties": {
            "name": { "type": "string" },
            "age": { "type": "integer", "minimum": 18 }
          },
          "required": ["name", "age"]
        }
        """.trimIndent()

    // Test JSON (Valid: age 18)
    val inputJson = """
        {
          "name": "Alex",
          "age": 18
        }
        """.trimIndent()

    // Test JSON (Invalid: age under 18)
    val invalidInputJson = """
        {
          "name": "Alex",
          "age": 15
        }
        """.trimIndent()

    val validator = Validator(schemaJson, asString = true)

    @Test
    fun testValid() {
        val errors = validator.validate(validator.getNode(inputJson))
        assertTrue(errors.isEmpty())
    }

    @Test
    fun testInvalid() {
        val errors = validator.validate(validator.getNode(invalidInputJson))
        assertFalse(errors.isEmpty())
        errors.forEach { error ->
            println("- Path: ${error.property} | Message: ${error.message}")
        }
    }
}