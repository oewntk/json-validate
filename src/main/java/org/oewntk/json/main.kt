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

private fun toSchema(selector: String): String {
    when (selector) {
        "model" -> return "schema-model.json"
        "data" -> return "schema-data.json"
        "oewn" -> return "schema-oewn.json"
        else -> throw IllegalArgumentException("Invalid schema selector $selector")
    }
}

fun main(args: Array<String>) {

    //val validator = Validator(schemaJson, asString = true)
    val validator = Validator(toSchema(args[0]))
    args.asSequence()
        .drop(1)
        .forEach { f ->
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
                        println("  - Path: ${error.property} | Message: ${error.message} | Instance: ${error.instanceNode}")
                    }
            }
        }
}
