package com.smeup.rpgparser.ast

import com.smeup.rpgparser.*
import com.strumenta.kolasu.model.ReferenceByName
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test as test

class DataDefinitionTest {

    fun processDataDefinition(code: String) : CompilationUnit {
        val completeCode = """
|     H/COPY QILEGEN,£INIZH
|      *---------------------------------------------------------------
|     I/COPY QILEGEN,£TABB£1DS
|     I/COPY QILEGEN,£PDS
|     $code
        """.trimMargin("|")
        val rContext = assertCodeCanBeParsed(completeCode)
        return rContext.toAst(ToAstConfiguration(considerPosition = false))
    }

    @test fun singleDataParsing() {
        val cu = processDataDefinition("D U\$FUNZ          S             10")
        cu.assertDataDefinitionIsPresent("U\$FUNZ", StringType(10))
    }

    @test fun booleanDataParsing() {
        val cu = processDataDefinition("D OK              S              1N")
        cu.assertDataDefinitionIsPresent("OK", BooleanType)
    }

    @test fun singleDataParsingOther() {
        val cu = processDataDefinition("D U\$FUNZ          S             99")
        cu.assertDataDefinitionIsPresent("U\$FUNZ", StringType(99))
    }

    @test fun singleDataParsingWithDecimals() {
        val cu = processDataDefinition("D \$X              S              3  2")
        cu.assertDataDefinitionIsPresent("\$X", NumberType(1, 2))
    }

    @test fun arrayParsing() {
        val cu = processDataDefinition("D U\$FUNZ          S             10    DIM(200)")
        cu.assertDataDefinitionIsPresent("U\$FUNZ", ArrayType(StringType(10), 200))
    }

    @test fun structParsing() {
        val cu = processDataDefinition("D                 DS\n" +
                "     D \$\$SVAR                      1050    DIM(200)\n" +
                "     D  \$\$SVARCD                     50    OVERLAY(\$\$SVAR:1)                    Name\n" +
                "     D  \$\$SVARVA                   1000    OVERLAY(\$\$SVAR:*NEXT)                Value")
        cu.assertDataDefinitionIsPresent("\$\$SVAR", ArrayType(DataStructureType(
                listOf(
                        FieldType("\$\$SVARCD", StringType(50)),
                        FieldType("\$\$SVARVA", StringType(1000))),
                1050), 200),
                fields = listOf(
                        FieldDefinition("\$\$SVARCD", StringType(50)),
                        FieldDefinition("\$\$SVARVA", StringType(1000))
                ))
    }

    @test fun likeAndDimClauseParsing() {
        StaticallyEvaluator.systemInterface = object : SystemInterface {
            override fun display(value: String) {
                // nothing to do
            }

            override fun getUnsolvedExpression(variable: ReferenceByName<AbstractDataDefinition>): Value {
                if (variable.name == "\$\$SVAR") {
                    return createArrayValue(12, 38) { StringValue("")}
                }
                throw RuntimeException("Unexpected call")
            }
        }
        val cu = processDataDefinition("D U\$SVARSK        S                   LIKE(\$\$SVAR) DIM(%ELEM(\$\$SVAR))")
        cu.assertDataDefinitionIsPresent("U\$SVARSK", ArrayType(StringType(12), 38))
    }

//    @test fun dimClauseParsing() {
//        val cu = processDataDefinition("D U\$SVARSK        S                                  DIM(%ELEM(\$\$SVAR))")
//        cu.assertDataDefinitionIsPresent("U\$SVARSK", ArrayType(StringType(12), 27)
//                /*arrayLength = NumberOfElementsExpr(DataRefExpr(ReferenceByName("\$\$SVAR")))*/)
//    }

    @test fun inStatementDataDefinitionInClearIsProcessed() {
        val cu = assertASTCanBeProduced("CALCFIB", true)
        cu.resolve()
        assertTrue(cu.hasAnyDataDefinition("dsp"))
        // TODO test the type is StringType(50)
    }

    @test fun executeJD_useOfLike() {
        val cu = assertASTCanBeProduced("JD_001", true)
        cu.resolve()
        val interpreter = Interpreter(DummySystemInterface)
        interpreter.simplyInitialize(cu, emptyMap())
        val dataDefinition = cu.getDataDefinition("U\$SVARSK_INI")
        assertEquals(IntValue(200), dataDefinition.actualArrayLength(interpreter))
    }

    @test fun executeJD_useOfDim() {
        val cu = assertASTCanBeProduced("JD_001", true)
        cu.resolve()
        val interpreter = Interpreter(DummySystemInterface)
        interpreter.simplyInitialize(cu, emptyMap())
        val dataDefinition = cu.getDataDefinition("U\$SVARSK_INI")
        assertEquals(IntValue(1050), dataDefinition.actualElementSize(interpreter))
    }
}
