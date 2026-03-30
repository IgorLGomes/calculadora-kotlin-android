package com.example.calculadora_kotlin_android.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Creme        = Color(0xFFF0EBE0)
private val CremeEscuro  = Color(0xFFDED8CC)
private val Tinta        = Color(0xFF1A1A18)
private val NeonVerde    = Color(0xFF39FF14)
private val VerdePress   = Color(0xFF1DB800)
private val BtnNumFundo  = Color(0xFFE8E2D7)
private val BtnAcFundo   = Color(0xFFCDC8BE)
private val SombraColor  = Tinta

class Calculadora(var num01: Double = 0.0, var num02: Double = 0.0) {
    fun somar()       = num01 + num02
    fun subtrair()    = num01 - num02
    fun multiplicar() = num01 * num02
    fun dividir(): Double {
        if (num02 == 0.0) throw ArithmeticException("DIV/0")
        return num01 / num02
    }
}

@Composable
fun CalculadoraScreen() {

    var visor             by remember { mutableStateOf("0") }
    var num01             by remember { mutableDoubleStateOf(0.0) }
    var operadorAtual     by remember { mutableStateOf("") }
    var aguardandoNum02   by remember { mutableStateOf(false) }
    var resultadoMostrado by remember { mutableStateOf(false) }
    var erroAtivo         by remember { mutableStateOf(false) }

    val calc = remember { Calculadora() }

    fun fmt(v: Double) =
        if (v == v.toLong().toDouble()) v.toLong().toString()
        else v.toBigDecimal().stripTrailingZeros().toPlainString()

    fun digito(d: String) {
        if (erroAtivo) { visor = "0"; erroAtivo = false }
        when {
            d == "." && visor.contains(".") -> return
            aguardandoNum02 || resultadoMostrado -> {
                visor = if (d == ".") "0." else d
                aguardandoNum02 = false; resultadoMostrado = false
            }
            visor == "0" && d != "." -> visor = d
            visor.length >= 11       -> return
            else                     -> visor += d
        }
    }

    fun calcEncadeado(): Double? = try {
        calc.num01 = num01
        calc.num02 = visor.toDoubleOrNull() ?: 0.0
        when (operadorAtual) {
            "+" -> calc.somar()
            "-" -> calc.subtrair()
            "×" -> calc.multiplicar()
            "÷" -> calc.dividir()
            else -> null
        }
    } catch (e: ArithmeticException) { null }

    fun operador(op: String) {
        if (operadorAtual.isNotEmpty() && !aguardandoNum02) {
            val r = calcEncadeado()
            if (r == null) { visor = "ERR"; erroAtivo = true; operadorAtual = ""; return }
            visor = fmt(r); num01 = r
        } else {
            num01 = visor.toDoubleOrNull() ?: 0.0
        }
        operadorAtual = op; aguardandoNum02 = true
    }

    fun igual() {
        if (operadorAtual.isEmpty()) return
        val r = calcEncadeado()
        if (r == null) { visor = "ERR"; erroAtivo = true }
        else           { visor = fmt(r) }
        operadorAtual = ""; resultadoMostrado = true
    }

    fun limpar() {
        visor = "0"; num01 = 0.0; operadorAtual = ""
        aguardandoNum02 = false; resultadoMostrado = false; erroAtivo = false
    }

    fun sinal() { val v = visor.toDoubleOrNull() ?: return; visor = fmt(v * -1) }
    fun pct()   { val v = visor.toDoubleOrNull() ?: return; visor = fmt(v / 100) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Creme)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {

        Box(
            Modifier.fillMaxWidth().padding(bottom = 4.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = if (operadorAtual.isNotEmpty()) operadorAtual else " ",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = NeonVerde
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = SombraColor, topLeft = Offset(5.dp.toPx(), 5.dp.toPx()), size = size)
                    drawRect(
                        color = SombraColor, topLeft = Offset(0f, 0f), size = size,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                }
                .background(CremeEscuro)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = visor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = when {
                    visor.length > 9 -> 34.sp
                    visor.length > 6 -> 46.sp
                    else             -> 62.sp
                },
                color = if (erroAtivo) Color(0xFFCC2200) else Tinta,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(18.dp))

        val gap = 10.dp

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            BrutalBtn("AC",  BtnAcFundo,  Tinta,     Modifier.weight(1f)) { limpar() }
            BrutalBtn("+/-", BtnAcFundo,  Tinta,     Modifier.weight(1f)) { sinal() }
            BrutalBtn("%",   BtnAcFundo,  Tinta,     Modifier.weight(1f)) { pct() }
            BrutalBtn("÷",   NeonVerde,   Tinta,     Modifier.weight(1f), ativo = operadorAtual == "÷") { operador("÷") }
        }
        Spacer(Modifier.height(gap))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            BrutalBtn("7",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito("7") }
            BrutalBtn("8",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito("8") }
            BrutalBtn("9",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito("9") }
            BrutalBtn("×",   NeonVerde,   Tinta,     Modifier.weight(1f), ativo = operadorAtual == "×") { operador("×") }
        }
        Spacer(Modifier.height(gap))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            BrutalBtn("4",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito("4") }
            BrutalBtn("5",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito("5") }
            BrutalBtn("6",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito("6") }
            BrutalBtn("-",   NeonVerde,   Tinta,     Modifier.weight(1f), ativo = operadorAtual == "-") { operador("-") }
        }
        Spacer(Modifier.height(gap))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            BrutalBtn("1",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito("1") }
            BrutalBtn("2",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito("2") }
            BrutalBtn("3",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito("3") }
            BrutalBtn("+",   NeonVerde,   Tinta,     Modifier.weight(1f), ativo = operadorAtual == "+") { operador("+") }
        }
        Spacer(Modifier.height(gap))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            BrutalBtn("0",   BtnNumFundo, Tinta,     Modifier.weight(2f), largura = true) { digito("0") }
            BrutalBtn(".",   BtnNumFundo, Tinta,     Modifier.weight(1f)) { digito(".") }
            BrutalBtn("=",   Tinta,       NeonVerde, Modifier.weight(1f)) { igual() }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun BrutalBtn(
    texto: String,
    fundo: Color,
    textoColor: Color,
    modifier: Modifier = Modifier,
    ativo: Boolean = false,
    largura: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val fundoAnim by animateColorAsState(
        targetValue = when {
            isPressed && fundo == NeonVerde -> VerdePress
            isPressed                       -> fundo.copy(alpha = 0.65f)
            ativo                           -> NeonVerde.copy(alpha = 0.85f)
            else                            -> fundo
        },
        animationSpec = tween(80), label = "fundo"
    )

    val shadowOffset by animateFloatAsState(
        targetValue = if (isPressed) 1f else 4f,
        animationSpec = tween(80), label = "shadow"
    )

    Box(
        modifier = modifier
            .aspectRatio(if (largura) 2.18f else 1f)
            .drawBehind {
                val o = shadowOffset * density
                drawRect(color = SombraColor, topLeft = Offset(o, o), size = size)
                drawRect(
                    color = SombraColor, topLeft = Offset(0f, 0f), size = size,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f * density)
                )
            }
            .background(fundoAnim)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = if (largura) Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = texto,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = textoColor,
            modifier = if (largura) Modifier.padding(start = 26.dp) else Modifier
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF0EBE0)
@Composable
fun CalculadoraPreview() {
    CalculadoraScreen()
}