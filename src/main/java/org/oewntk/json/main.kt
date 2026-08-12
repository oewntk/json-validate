package org.oewntk.json

import com.networknt.schema.*
import java.io.File
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SchemaLocation
import com.networknt.schema.SpecificationVersion
import com.networknt.schema.InputFormat

class Validator(schema: String) {

    val schema: Schema =
        SchemaRegistry
            .withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
            .getSchema(SchemaLocation.of("classpath:$schema"))
            .apply {
                // eagerly surface $ref resolution problems instead of failing lazily on first validate()
                initializeValidators()
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
                    .take(3)
                    .forEach { error ->
                        println("  - Path: ${error.property} | Message: ${error.message} | Instance: ${error.instanceNode}")
                    }
            }
        }
}
