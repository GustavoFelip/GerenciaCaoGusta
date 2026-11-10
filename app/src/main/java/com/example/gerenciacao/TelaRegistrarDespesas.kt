package com.example.gerenciacao

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

// Registra uma despesa local; nao solicita conta bancaria nem efetua pagamentos.
@Composable
fun TelaRegistrarDespesa(ctx: ContextoAbrigo) {
    var descricao by rememberSaveable { mutableStateOf("") }
    var valor by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("Alimentação") }
    var data by rememberSaveable { mutableStateOf(hojeAbrigo()) }
    var animalId by rememberSaveable { mutableStateOf("") }
    var observacoes by rememberSaveable { mutableStateOf("") }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    BaseRestante(ctx, "Registrar despesa", "Finanças", descricao.isNotBlank() || valor.isNotBlank() || observacoes.isNotBlank() ||
            animalId.isNotEmpty() || categoria != "Alimentação" || data != hojeAbrigo()) {
        Text("Organize os gastos do abrigo", color = MarromRestante)
        CampoRestante("Descrição", descricao, { descricao = it })
        CampoRestante("Valor em reais", valor, { valor = it }, dica = "330,00 · sem separador de milhares")
        SeletorRestante("Categoria", categoria, listOf("Alimentação", "Saúde", "Higiene", "Transporte", "Estrutura", "Outros").map { it to it }) { categoria = it }
        CampoRestante("Data · DD/MM/AAAA", data, { data = it })
        SeletorRestante("Bichinho relacionado · opcional", ctx.animais.firstOrNull { it.id == animalId }?.nome ?: "Não vinculado",
            listOf("" to "Não vinculado") + ctx.animais.filterNot { it.arquivado }.map { it.id to it.nome }) { animalId = it }
        CampoRestante("Observações · opcional", observacoes, { observacoes = it }, longo = true)
        CartaoRestante(PessegoRestante) { Text("Saída no histórico financeiro. O registro não realiza o pagamento.") }
        // Salva somente quando data, descricao e quantia sao validas.
        BotaoRestante("Salvar despesa") {
            val centavos = centavosAbrigo(valor)
            when {
                descricao.trim().length < 3 -> erro = "Informe uma descrição com pelo menos 3 caracteres."
                centavos == null -> erro = "Informe um valor maior que zero, como 330,00, sem ponto de milhar."
                !dataValidaAbrigo(data.trim()) -> erro = "Informe uma data válida no formato DD/MM/AAAA."
                animalId.isNotEmpty() && ctx.animais.none { it.id == animalId && !it.arquivado } -> erro = "Escolha um bichinho disponível."
                else -> {
                    ctx.atualizar(ctx.dados.copy(lancamentos = ctx.dados.lancamentos + LancamentoAbrigo(
                        descricao = descricao.trim(), natureza = NaturezaAbrigo.DESPESA, data = data.trim(), centavos = centavos,
                        categoria = categoria, animalId = animalId.ifEmpty { null }, observacoes = observacoes.trim())))
                    ctx.voltar()
                }
            }
        }
    }
    erro?.let { MensagemRestante(it) { erro = null } }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaRegistrarDespesa() {
    androidx.compose.material3.MaterialTheme { TelaRegistrarDespesa(contextoPreviewRestante()) }
}