package com.example.gerenciacao

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Protege celulas de texto contra formulas quando o CSV e aberto em uma planilha.
fun celulaCsvAbrigo(texto: String): String {
    val protegido = if (texto.trimStart().firstOrNull() in listOf('=', '+', '-', '@') || texto.startsWith('\t') || texto.startsWith('\r')) "'$texto" else texto
    return "\"${protegido.replace("\"", "\"\"")}\""
}

// Usa os mesmos registros do financeiro; produtos ficam separados dos totais em reais.
@Composable
fun TelaRelatorios(ctx: ContextoAbrigo) {
    var mes by rememberSaveable { mutableIntStateOf(11) }
    var ano by rememberSaveable { mutableIntStateOf(2026) }
    var modo by rememberSaveable { mutableStateOf("Mensal") }
    var faixa by rememberSaveable { mutableIntStateOf(0) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    var exportacao by rememberSaveable { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val faixas = listOf("1–7", "8–14", "15–21", "22–fim")
    val periodo = ctx.dados.lancamentos.filter { doPeriodoAbrigo(it.data, mes, ano) }
    val selecionados = periodo.filter { modo == "Mensal" || ((it.data.take(2).toInt() - 1) / 7).coerceAtMost(3) == faixa }
    val entradas = selecionados.filter { it.natureza == NaturezaAbrigo.DINHEIRO }.sumOf { it.centavos }
    val saidas = selecionados.filter { it.natureza == NaturezaAbrigo.DESPESA }.sumOf { it.centavos }
    // O Android solicita ao usuario o local do arquivo; nenhuma permissao ampla e necessaria.
    val salvar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        val conteudo = exportacao
        exportacao = null
        if (uri != null && conteudo != null) escopo.launch {
            ocupado = true
            val resultado = withContext(Dispatchers.IO) { runCatching {
                val destino = contexto.contentResolver.openOutputStream(uri) ?: error("Não foi possível abrir o arquivo.")
                destino.bufferedWriter(Charsets.UTF_8).use { it.write("\uFEFF" + conteudo) }
            } }
            ocupado = false
            aviso = if (resultado.isSuccess) "Relatório CSV salvo no local escolhido." else "Não foi possível salvar o relatório. Tente outro local."
        }
    }
    BaseRestante(ctx, "Relatórios", "Finanças") {
        Row {
            TextButton(onClick = { if (mes == 1) { mes = 12; ano-- } else mes-- }) { Text("‹") }
            Text("${mes.toString().padStart(2, '0')}/$ano", Modifier.weight(1f))
            TextButton(onClick = { if (mes == 12) { mes = 1; ano++ } else mes++ }) { Text("›") }
        }
        FiltrosRestantes(listOf("Semanal", "Mensal"), modo) { modo = it }
        if (modo == "Semanal") {
            SeletorRestante("Dias do mês", faixas[faixa], faixas.mapIndexed { i, nome -> i.toString() to nome }) { faixa = it.toInt() }
            Text("Faixas fixas de dias; não correspondem a semanas de calendário.", fontSize = 12.sp)
        }
        CartaoRestante(VerdeRestante) { Text("Entradas: ${reaisAbrigo(entradas)}"); Text("Saídas: ${reaisAbrigo(saidas)}"); Text("Resultado: ${reaisAbrigo(entradas - saidas)}", fontWeight = FontWeight.Bold) }
        Text("Evolução financeira", fontWeight = FontWeight.Bold)
        val grupos = (0..3).map { i ->
            val registros = periodo.filter { ((it.data.take(2).toInt() - 1) / 7).coerceAtMost(3) == i }
            registros.filter { it.natureza == NaturezaAbrigo.DINHEIRO }.sumOf { it.centavos } to registros.filter { it.natureza == NaturezaAbrigo.DESPESA }.sumOf { it.centavos }
        }
        val maximo = grupos.flatMap { listOf(it.first, it.second) }.maxOrNull()?.coerceAtLeast(1L) ?: 1L
        // Cada barra tem legenda e valor textual para leitura sem depender da cor.
        CartaoRestante {
            grupos.forEachIndexed { i, (entrada, saida) ->
                Text("Dias ${faixas[i]}", fontWeight = FontWeight.SemiBold)
                BarraRelatorioAbrigo("Entradas", entrada, maximo, Color(0xFF45854A))
                BarraRelatorioAbrigo("Despesas", saida, maximo, Color(0xFFB5673D))
            }
        }
        CartaoRestante(CremeRestante) {
            Text("Recebimentos sem valor em reais", fontWeight = FontWeight.Bold)
            val outros = selecionados.filter { it.natureza == NaturezaAbrigo.PRODUTOS || it.natureza == NaturezaAbrigo.BICHINHO }
            if (outros.isEmpty()) Text("Nenhum neste período.")
            outros.forEach { Text("${it.descricao}: ${it.quantidade}") }
        }
        // Congela o conteudo antes de abrir o seletor para exportar o periodo revisado.
        BotaoRestante(if (ocupado) "Salvando…" else "Gerar relatório CSV", !ocupado && exportacao == null) {
            val cabecalho = "Data;Descrição;Natureza;Valor em reais;Quantidade;Categoria\n"
            exportacao = cabecalho + selecionados.joinToString("\n") {
                listOf(it.data, it.descricao, it.natureza.name,
                    if (it.natureza == NaturezaAbrigo.DINHEIRO || it.natureza == NaturezaAbrigo.DESPESA) java.math.BigDecimal.valueOf(it.centavos, 2).toPlainString().replace('.', ',') else "",
                    it.quantidade, it.categoria).joinToString(";", transform = ::celulaCsvAbrigo)
            }
            salvar.launch("GerenciaCao-$ano-${mes.toString().padStart(2, '0')}-${if (modo == "Mensal") "mes" else "faixa${faixa + 1}"}.csv")
        }
        Text("Registros locais; inclui dados demonstrativos iniciais.", fontSize = 12.sp)
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// A altura da barra nao altera o texto e a proporcao e limitada ao intervalo valido.
@Composable
private fun BarraRelatorioAbrigo(rotulo: String, valor: Long, maximo: Long, cor: Color) {
    Column(Modifier.fillMaxWidth().semantics { contentDescription = "$rotulo: ${reaisAbrigo(valor)}" }) {
        Text("$rotulo: ${reaisAbrigo(valor)}", fontSize = 12.sp)
        Box(Modifier.fillMaxWidth().height(10.dp).background(BordaRestante)) {
            Box(Modifier.fillMaxWidth((valor.toFloat() / maximo).coerceIn(0f, 1f)).height(10.dp).background(cor))
        }
    }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaRelatorios() {
    androidx.compose.material3.MaterialTheme { TelaRelatorios(contextoPreviewRestante()) }
}