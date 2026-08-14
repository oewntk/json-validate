package org.oewntk.json

import com.networknt.schema.Error
import com.networknt.schema.InputFormat
import com.networknt.schema.Schema
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SpecificationVersion
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class TestValidation {

    class TextValidator(schema: String) {

        val schema: Schema = SchemaRegistry
            .withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
            .getSchema(schema, InputFormat.JSON)

        fun validate(json: String): List<Error> = schema.validate(json, InputFormat.JSON)
    }

    // Test Schema
    val oewnSchemaJson = $$"""
        {
          "$schema-model.json": "https://json-schema.org/draft/2020-12/schema",
          "type": "object",
          "properties": {
            "synsetId": { "type": "string" },
            "type": { "type": "string" },
            "domain": { "type": "string" },
            "members": { "type": "array" },
            "definitions": { "type": "array" },
            "examples": { "type": "array" },
            "relations": { "type": "object" }
          },
          "required": ["synsetId", "type", "domain", "members", "definitions"]
        }
        """.trimIndent()

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

    val validator = TextValidator(schemaJson)

    @Test
    fun testValid() {
        val errors = validator.validate(inputJson)
        assertTrue(errors.isEmpty())
    }

    @Test
    fun testInvalid() {
        val errors = validator.validate(invalidInputJson)
        assertFalse(errors.isEmpty())
        errors.forEach { error ->
            println("- Path: ${error.property} | Message: ${error.message} | Instance: ${error.instanceNode}")
        }
    }
}