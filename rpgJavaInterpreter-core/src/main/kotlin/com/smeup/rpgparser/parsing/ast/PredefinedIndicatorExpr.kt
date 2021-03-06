package com.smeup.rpgparser.parsing.ast

import com.strumenta.kolasu.model.Position

// *IN00..*IN99
data class PredefinedIndicatorExpr(val index: Int, override val position: Position? = null) :
    AssignableExpression(position) {
    override fun size(): Int = 1
}

// *IN
data class PredefinedGlobalIndicatorExpr(override val position: Position? = null) :
        AssignableExpression(position) {
    override fun size(): Int {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}

data class DataWrapUpIndicatorExpr(val dataWrapUpChoice: DataWrapUpChoice, override val position: Position? = null) :
        AssignableExpression(position) {
    override fun size(): Int = 1
}