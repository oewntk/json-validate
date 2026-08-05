package org.oewntk.json

import com.networknt.schema.*
import java.io.File

class Validator(schema: String, asString: Boolean = false) {
    private val registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)

    val schema: Schema = if (asString) {
        registry.getSchema(schema, InputFormat.JSON)
    } else {
        val text = Thread.currentThread().contextClassLoader
            .getResourceAsStream(schema)
            ?.bufferedReader()?.readText()
            ?: throw IllegalArgumentException("Schema resource not found: $schema")
        registry.getSchema(text, InputFormat.JSON)
    }

    fun validate(json: String): List<Error> = schema.validate(json, InputFormat.JSON)
}

// Test Schema
val schemaJson = """
        {
          "${'$'}schema-model.json": "https://json-schema.org/draft/2020-12/schema",
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

fun main(args: Array<String>) {

    //val validator = Validator("schema-model.json-from-xml.json")
    //val validator = Validator(schemaJson, asString = true)
    val validator = Validator("schema-model.json")
    for (f in args) {
        // Data node
        val jsonString = File(f).readText()

        // Validate
        val errors = validator.validate(jsonString)

        // Result
        if (errors.isEmpty()) {
            println("$f is valid!")
        } else {
            println("❌ Validation of $f failed with ${errors.size} error(s):")
            errors
                .take(25)
                .forEach { error ->
                    println("  - Path: ${error.property} | Message: ${error.message} Instance: ${error.instanceNode}")
                }
        }
    }
}
