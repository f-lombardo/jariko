package com.smeup.rpgparser.ast

import com.smeup.rpgparser.*
import com.smeup.rpgparser.parsetreetoast.ToAstConfiguration
import com.smeup.rpgparser.parsetreetoast.toAst
import com.strumenta.kolasu.model.ReferenceByName
import org.junit.Ignore
import kotlin.test.assertEquals
import org.junit.Test as test

class ExpressionsTest {

    private fun expression(code: String) : Expression {
        val exprContext = assertExpressionCanBeParsed("                                   $code")
        return exprContext.toAst(ToAstConfiguration(considerPosition = false))
    }

    @test fun intLiteralParsing() {
        assertEquals(IntLiteral(200), expression("200"))
    }

    @test fun stringLiteralParsing() {
        assertEquals(StringLiteral("INZ"), expression("'INZ'"))
    }

    @test fun dataRefParsing() {
        assertEquals(dataRef(("\$\$SVAR")), expression("\$\$SVAR"))
    }

    @test fun numberOfElementsParsing() {
        assertEquals(NumberOfElementsExpr(dataRef("\$\$SVAR")), expression("%ELEM(\$\$SVAR)"))
    }

    @test fun equalityParsing() {
        assertEquals(EqualityExpr(dataRef("U\$FUNZ"), StringLiteral("INZ")), expression("U\$FUNZ='INZ'"))
    }

    @test fun greaterThanParsing() {
        assertEquals(GreaterThanExpr(dataRef("\$X"), IntLiteral(0)), expression("\$X>0"))
    }

    @test fun functionCallParsing() {
        assertEquals(FunctionCall(
                ReferenceByName("\$\$SVARVA"),
                listOf(dataRef(("\$R")))),
                expression("\$\$SVARVA(\$R)"))
    }

    @test fun lookupParsing() {
        assertEquals(LookupExpr(
                        StringLiteral("Url"),
                        dataRef("\$\$SVARCD")
                ),
                expression("%LOOKUP('Url':\$\$SVARCD)"))
    }

    @test fun translateExprParsing() {
        assertEquals(TranslateExpr(dataRef("lo"), dataRef("up"), StringLiteral("rpg dept"),  IntLiteral(1)),
                     expression("%XLATE(lo:up:'rpg dept')"))
    }

    @test fun translateExprParsingWithStartPos() {
        assertEquals(TranslateExpr(
                dataRef("lo"),
                dataRef("up"),
                StringLiteral("RPG DEPT"),
                IntLiteral(6)),
                expression("%XLATE(lo:up:'RPG DEPT':6)")
        )
    }

    @test fun notExprParsing() {
        assertEquals(NotExpr(
                dataRef("OK")),
                expression("NOT(OK)")
        )
    }

    @test fun scanExprParsing() {
        assertEquals(ScanExpr(
                StringLiteral("oo"), dataRef("source")),
                expression("%scan ('oo' : source)")
        )
    }

    @test fun scanExprParsingWithStart() {
        assertEquals(ScanExpr(
                StringLiteral("Dr."), dataRef("source"), IntLiteral(2)),
                expression("%scan ('Dr.' : source : 2)")
        )
    }

    @test fun trimExprParsing() {
        assertEquals(TrimExpr(
                dataRef("§§NAM")),
                expression("%TRIM(§§NAM)")
        )
    }

    @test fun trimExprParsingWithCharacters() {
        assertEquals(TrimExpr(
                dataRef("edited"), StringLiteral("\$*")),
                expression("%trim(edited : '\$*')")
        )
    }

    //TODO Changes in grammar needed
    @test @Ignore
    fun multExpressionWithoutBlanksParsing() {
        val expected = expression("x = y * 2")
        val actual = expression("x=y*2")
        assertEquals(expected, actual)
    }

//    @test @Ignore
//    fun multExpExpressionWithoutBlanksParsing() {
//        val expected = expression("x = y * 10 **13")
//        val actual = expression("x=y*10**13")
//        assertEquals(expected, actual)
//    }


    @test
    fun arithmeticExpressionWithoutBlanksParsing() {
        assertEquals(expression("x = y + 2"), expression("x=y+2"))
        assertEquals(expression("x = y - 2"), expression("x=y-2"))
        assertEquals(expression("x = y / 2"), expression("x=y/2"))
        assertEquals(expression("x = y ** 2"), expression("x=y**2"))
    }
}


