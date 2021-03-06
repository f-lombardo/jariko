package com.smeup.rpgparser.interpreter

import com.smeup.rpgparser.parsing.ast.*
import java.math.BigDecimal

private fun assignStringToString(operationExtender: String?, target: AssignableExpression, valueExpression: Expression, interpreterCoreHelper: InterpreterCoreHelper): Value {
    var newValue = interpreterCoreHelper.interpret(valueExpression)
    if (valueExpression is AllExpr) {
        return interpreterCoreHelper.assign(target, newValue)
    }
    if (newValue is NumberValue) {
        newValue = newValue.numberToString()
    }
    val valueSize = size(valueExpression)
    val targetSize = size(target)
    if (valueSize > targetSize) {
        newValue = newValue.takeFirst(target.size())
    } else if (valueSize < targetSize) {
        val append = if (operationExtender == null) {
            val value = interpreterCoreHelper.interpret(target)
            require(value is StringValue)
            StringValue.padded(value.value, targetSize).takeLast(targetSize - valueSize)
        } else {
            StringValue.blank(targetSize - valueSize)
        }
        newValue = newValue.concatenate(append)
    }
    return interpreterCoreHelper.assign(target, newValue)
}

private fun assignNumberToNumber(operationExtender: String?, target: AssignableExpression, valueExpression: Expression, interpreterCoreHelper: InterpreterCoreHelper): Value {
    if (valueExpression is FigurativeConstantRef) {
        return interpreterCoreHelper.assign(target, interpreterCoreHelper.interpret(valueExpression))
    }
    val newValue = interpreterCoreHelper.interpret(valueExpression) as NumberValue
    val targetType = target.type() as NumberType
    val newDecimalValue = DecimalValue(BigDecimal(newValue.bigDecimal.unscaledValue(), targetType.decimalDigits))
    return interpreterCoreHelper.assign(target, newDecimalValue)
}

fun size(valueExpression: Expression): Int {
    if (valueExpression is ArrayAccessExpr) {
        return valueExpression.array.type().elementSize()
    }
    return valueExpression.type().size
}

// for future use
// map conversion 1 -> J, 2 -> K, ..., R -> 9
private fun Char.numberToLetter(): CharSequence {
    val offset = 'J'.toInt() - '1'.toInt()
    return (this.toInt() + offset).toChar().toString()
}

private fun NumberValue.numberToString(): Value {
    val value = this.bigDecimal.abs().toString().replaceFirst(".", "")
    require(this.bigDecimal >= BigDecimal.ZERO) {
        "negative factor 2 not allowed"
    }
    return StringValue(value)
}

fun movel(operationExtender: String?, target: AssignableExpression, value: Expression, interpreterCoreHelper: InterpreterCoreHelper): Value {
    val valueType = value.type()
    if (target.type() is StringType && (valueType is StringType || valueType is NumberType || valueType is FigurativeType)) {
        return assignStringToString(operationExtender, target, value, interpreterCoreHelper)
    }
    if (target.type() is NumberType && (valueType is NumberType || valueType is FigurativeType)) {
        return assignNumberToNumber(operationExtender, target, value, interpreterCoreHelper)
    }
    throw IllegalArgumentException("Cannot assign ${valueType::class.qualifiedName} to ${target.type()::class.qualifiedName}")
}
