package com.smeup.rpgparser.db.sql

import com.smeup.rpgparser.interpreter.*

fun DBField.sqlType(): String =
    when (this.type) {
        is StringType -> "CHAR (${this.type.size}) NOT NULL"
        is NumberType -> "DECIMAL (${this.type.size}, ${this.type.decimalDigits}) NOT NULL"
        else -> TODO("Conversion to SQL Type not yet implemented: ${this.type}")
    }

fun typeFor(sqlType: String, columnSize: Int, decimalDigits: Int): Type =
    when (sqlType) {
        "CHARACTER" -> StringType(columnSize.toLong())
        "DECIMAL" -> NumberType(columnSize - decimalDigits, decimalDigits)
        else -> TODO("Conversion from SQL Type not yet implemented: $sqlType")
    }

fun Value.toDBValue() =
    when (this) {
        is StringValue -> this.valueWithoutPadding
        is IntValue -> this.value
        else -> TODO("Conversion to DB Obejct not yet implemented: $this")
    }