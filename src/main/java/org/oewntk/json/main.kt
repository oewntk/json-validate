package org.oewntk.json

import com.networknt.schema.*
import java.io.File
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SchemaLocation
import com.networknt.schema.SpecificationVersion
import com.networknt.schema.InputFormat
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.vararg
import kotlin.system.exitProcess

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
    //when (selector) {
    //    "model" -> return "schema-model.json"
    //    "data" -> return "schema-data.json"
    //    "oewn" -> return "schema-oewn.json"
    //    else -> throw IllegalArgumentException("Invalid schema selector $selector")
    //}
    return "schema-$selector.json"
}

fun main(args: Array<String>) {

    val parser = ArgParser("validator")
    // Options (start with - or --)
    // @formatter:off
    val schema by parser.argument(          ArgType.String,                                                      description = "Schema")
    val inputs by parser.argument(          ArgType.String,                                                      description = "File inputs")    .vararg()
    val verbose by parser.option(           ArgType.Boolean,       shortName = "v",  fullName = "verbose",       description = "Verbose output") .default(false)

    val traceTime by parser.option(         ArgType.Boolean,       shortName = "tt", fullName = "trace:time",    description = "trace time")     .default(false)
    val traceHeap by parser.option(         ArgType.Boolean,       shortName = "th", fullName = "trace:heap",    description = "trace heap")     .default(false)
    // @formatter:on

    parser.parse(args)
    if (verbose) {
        System.err.println("Schema: $schema")
        System.err.println("Files: $inputs")
    }
    val validator = Validator(toSchema(schema))
    if (verbose) {
        System.err.println("Schema id: ${validator.schema.id}")
    }
    inputs.forEach { f ->
        // Data node
        val jsonString = File(f).readText()

        // Validate
        val errors = validator.validate(jsonString)

        // Result
        if (errors.isEmpty()) {
            println("-$f is valid")
        } else {
            System.err.println("❌ Validation of $f failed with ${errors.size} error(s):")
            errors
                .take(3)
                .forEach { error ->
                    System.err.println("  - Path: ${error.property} | Message: ${error.message} | Instance: ${error.instanceNode}")
                }
            exitProcess(1)
        }
    }
}
