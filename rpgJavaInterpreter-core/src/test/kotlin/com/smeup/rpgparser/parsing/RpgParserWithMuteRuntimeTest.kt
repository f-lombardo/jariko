package com.smeup.rpgparser.parsing;


import com.smeup.rpgparser.ast.CompilationUnit
import com.smeup.rpgparser.execute
import com.smeup.rpgparser.facade.RpgParserFacade
import com.smeup.rpgparser.facade.RpgParserResult
import com.smeup.rpgparser.inputStreamFor
import com.smeup.rpgparser.parsetreetoast.ToAstConfiguration
import com.smeup.rpgparser.parsetreetoast.injectMuteAnnotation
import com.smeup.rpgparser.parsetreetoast.resolve
import com.smeup.rpgparser.parsetreetoast.toAst
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class RpgParserWithMuteRuntimeTest {

    // Temporary replacement to return RpgParserResult
    private fun assertCanBeParsed(exampleName: String, withMuteSupport: Boolean = true) : RpgParserResult {
        val result = RpgParserFacade()
                .apply { this.muteSupport = withMuteSupport }
                .parse(inputStreamFor(exampleName))
        assertTrue(result.correct,
                message = "Errors: ${result.errors.joinToString(separator = ", ")}")

        return result
    }

    // Temporary replacement
    private fun assertASTCanBeProduced(exampleName: String, considerPosition : Boolean = false,
                                       withMuteSupport: Boolean = true) : CompilationUnit {
        val parseTreeRoot = assertCanBeParsed(exampleName, withMuteSupport)
        val ast = parseTreeRoot.root!!.rContext.toAst(ToAstConfiguration(
                considerPosition = considerPosition))
        if (withMuteSupport) {
            if (!considerPosition) {
                throw IllegalStateException("Mute annotations can be injected only when retaining the position")
            }

        }
        if (withMuteSupport) {
            ast.injectMuteAnnotation(parseTreeRoot.root!!.rContext, parseTreeRoot.root!!.muteContexts!!)
        }
        return ast
    }

    @Test
    fun parseMUTE01_runtime() {
        val cu = assertASTCanBeProduced("mute/MUTE01_RUNTIME", true)
        cu.resolve()
        val interpreter = execute(cu, mapOf())

        assertEquals(interpreter.executedAnnotation.size, 8)

        // VAL1(FIELD1) VAL2('AAAA') COMP(EQ)
        var annotation = interpreter.executedAnnotation[3]
        assertTrue (actual = annotation != null)
        assertTrue (annotation.result.asBoolean().value)

        // VAL1(NBR) VAL2(11) COMP(LT)
        annotation = interpreter.executedAnnotation[7]
        assertTrue (actual = annotation != null)
        assertTrue (annotation.result.asBoolean().value)

        // FAILS VAL1(FIELD1) VAL2('A ' + ' B') COMP(NE)
        //annotation = interpreter.executedAnnotation[15]
        //assertTrue (actual = annotation != null)
        //assertFalse (annotation.result.asBoolean().value)

        // VAL1(B) VAL2(1) COMP(GE)
        annotation = interpreter.executedAnnotation[16]
        assertTrue (actual = annotation != null)
        assertTrue (annotation.result.asBoolean().value)

        // VAL1(B) VAL2(1) COMP(LE)
        annotation = interpreter.executedAnnotation[16]
        assertTrue (actual = annotation != null)
        assertTrue (annotation.result.asBoolean().value)


        // VAL1(B) VAL2(1) COMP(GT)
        annotation = interpreter.executedAnnotation[19]
        assertTrue (actual = annotation != null)
        assertFalse (annotation.result.asBoolean().value)

        // VAL1(B) VAL2(1) COMP(LT)
        annotation = interpreter.executedAnnotation[20]
        assertTrue (actual = annotation != null)
        assertFalse (annotation.result.asBoolean().value)


    }


}