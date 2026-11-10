package com.example.gerenciacao

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

// Registra recebimentos em dinheiro, produtos ou bichinhos sem executar pagamentos.
@Composable
fun TelaRegistrarDoacao(ctx: ContextoAbrigo) {
    var tipo by rememberSaveable { mutableStateOf("Dinheiro") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var valor by rememberSaveable { mutableStateOf("") }
    var quantidade by rememberSaveable { mutableStateOf("") }
    var data by rememberSaveable { mutableStateOf(hojeAbrigo()) }
    var animalId by rememberSaveable { mutableStateOf("") }
    var observacoes by rememberSaveable { mutableStateOf("") }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    BaseRestante(ctx, "Registrar doação", "Finanças", descricao.isNotBlank() || valor.isNotBlank() || quantidade.isNotBlank() ||
            observacoes.isNotBlank() || animalId.isNotEmpty() || tipo != "Dinheiro" || data != hojeAbrigo()) {
        Text("Cada contribuição faz diferença", color = MarromRestante)
        FiltrosRestantes(listOf("Dinheiro", "Produtos", "Bichinho"), tipo) { tipo = it }
        CampoRestante("Descrição", descricao, { descricao = it })
        if (tipo == "Dinheiro") CampoRestante("Valor em reais", valor, { valor = it }, dica = "200,00 · sem separador de milhares")
        if (tipo == "Produtos") CampoRestante("Quantidade e unidade", quantidade, { quantidade = it }, dica = "20 kg de ração ou 2 pacotes")
        if (tipo == "Bichinho") CartaoRestante(CremeRestante) {
            Text("Este registro documenta o recebimento. Cadastre a ficha do bichinho em Adicionar → Cadastrar bichinho; ela não será criada automaticamente.")
        }
        CampoRestante("Data · DD/MM/AAAA", data, { data = it })
        SeletorRestante("Bichinho relacionado · opcional", ctx.animais.firstOrNull { it.id == animalId }?.nome ?: "Não vinculado",
            listOf("" to "Não vinculado") + ctx.animais.filterNot { it.arquivado }.map { it.id to it.nome }) { animalId = it }
        CampoRestante("Observações · opcional", observacoes, { observacoes = it }, longo = true)
        CartaoRestante(VerdeRestante) {
            Text("Entrada no histórico financeiro")
            Text("Somente valores em dinheiro entram no total em reais.")
        }
        // Valida os campos da categoria ativa; valores de outra categoria nao entram no registro.
        BotaoRestante("Salvar doação") {
            val centavos = if (tipo == "Dinheiro") centavosAbrigo(valor) else 0L
            when {
                descricao.trim().length < 3 -> erro = "Preencha a descrição com pelo menos 3 caracteres."
                !dataValidaAbrigo(data.trim()) -> erro = "Informe uma data válida no formato DD/MM/AAAA."
                centavos == null -> erro = "Informe um valor maior que zero, como 200,00, sem ponto de milhar."
                tipo == "Produtos" && !Regex("^[0-9]+(?:[,.][0-9]+)?\\s+.*\\p{L}.*$").matches(quantidade.trim()) -> erro = "Informe quantidade e unidade, por exemplo 20 kg de ração."
                tipo == "Produtos" && quantidade.trim().substringBefore(' ').replace(',', '.').toBigDecimalOrNull()?.signum() != 1 -> erro = "A quantidade deve ser maior que zero."
                animalId.isNotEmpty() && ctx.animais.none { it.id == animalId && !it.arquivado } -> erro = "Escolha um bichinho disponível."
                else -> {
                    val natureza = when (tipo) { "Dinheiro" -> NaturezaAbrigo.DINHEIRO; "Produtos" -> NaturezaAbrigo.PRODUTOS; else -> NaturezaAbrigo.BICHINHO }
                    val registro = LancamentoAbrigo(descricao = descricao.trim(), natureza = natureza, data = data.trim(),
                        centavos = centavos, quantidade = if (tipo == "Produtos") quantidade.trim() else if (tipo == "Bichinho") "1 bichinho" else "",
                        animalId = animalId.ifEmpty { null }, observacoes = observacoes.trim())
                    ctx.atualizar(ctx.dados.copy(lancamentos = ctx.dados.lancamentos + registro))
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
private fun PreviewTelaRegistrarDoacao() {
    androidx.compose.material3.MaterialTheme { TelaRegistrarDoacao(contextoPreviewRestante()) }
}