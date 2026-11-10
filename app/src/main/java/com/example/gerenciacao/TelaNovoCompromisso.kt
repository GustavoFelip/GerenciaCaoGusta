package com.example.gerenciacao

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

// O mesmo formulario cria um compromisso ou reagenda o registro recebido pelo ID.
@Composable
fun TelaNovoCompromisso(ctx: ContextoAbrigo) {
    val original = ctx.dados.compromissos.firstOrNull { it.id == ctx.dados.editarCompromissoId }
    val chave = original?.id ?: "novo"
    var titulo by rememberSaveable(chave) { mutableStateOf(original?.titulo ?: "") }
    var tipo by rememberSaveable(chave) { mutableStateOf(original?.tipo ?: "Cuidados") }
    var data by rememberSaveable(chave) { mutableStateOf(original?.data ?: hojeAbrigo()) }
    var horario by rememberSaveable(chave) { mutableStateOf(original?.horario ?: "") }
    var animalId by rememberSaveable(chave) { mutableStateOf(original?.animalId ?: "") }
    var local by rememberSaveable(chave) { mutableStateOf(original?.local ?: "") }
    var observacoes by rememberSaveable(chave) { mutableStateOf(original?.observacoes ?: "") }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    val alterado = titulo != (original?.titulo ?: "") || tipo != (original?.tipo ?: "Cuidados") ||
            data != (original?.data ?: hojeAbrigo()) || horario != (original?.horario ?: "") ||
            animalId != (original?.animalId ?: "") || local != (original?.local ?: "") || observacoes != (original?.observacoes ?: "")
    BaseRestante(ctx, if (original == null) "Novo compromisso" else "Reagendar", "Agenda", alterado) {
        Text("Organize os próximos cuidados", color = MarromRestante)
        CampoRestante("Nome do compromisso", titulo, { titulo = it })
        SeletorRestante("Tipo", tipo, listOf("Cuidados", "Visitas", "Serviços").map { it to it }) { tipo = it }
        CampoRestante("Data · DD/MM/AAAA", data, { data = it }, dica = "07/11/2026")
        CampoRestante("Horário · HH:MM", horario, { horario = it }, dica = "14:00")
        SeletorRestante("Bichinho relacionado · opcional",
            ctx.animais.firstOrNull { it.id == animalId }?.nome ?: "Não vinculado",
            listOf("" to "Não vinculado") + ctx.animais.filterNot { it.arquivado }.map { it.id to it.nome }) { animalId = it }
        CampoRestante("Local · opcional", local, { local = it })
        CampoRestante("Observações · opcional", observacoes, { observacoes = it }, longo = true)
        CartaoRestante(CremeRestante) { Text("O compromisso aparecerá na agenda do abrigo. Nenhum lembrete automático será enviado.") }
        // Valida antes de alterar a colecao; a edicao preserva o mesmo ID.
        BotaoRestante("Salvar compromisso") {
            when {
                titulo.trim().length < 3 -> erro = "Informe um título com pelo menos 3 caracteres."
                !dataValidaAbrigo(data.trim()) -> erro = "Informe uma data válida no formato DD/MM/AAAA."
                !Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]").matches(horario.trim()) -> erro = "Informe um horário válido, como 14:00."
                animalId.isNotEmpty() && ctx.animais.none { it.id == animalId && !it.arquivado } -> erro = "Escolha um bichinho disponível ou Não vinculado."
                else -> {
                    val salvo = CompromissoAbrigo(id = original?.id ?: java.util.UUID.randomUUID().toString(),
                        titulo = titulo.trim(), tipo = tipo, data = data.trim(), horario = horario.trim(),
                        animalId = animalId.ifEmpty { null }, local = local.trim(), observacoes = observacoes.trim())
                    ctx.atualizar(ctx.dados.copy(compromissos = ctx.dados.compromissos.filterNot { it.id == salvo.id } + salvo,
                        compromissoId = salvo.id, editarCompromissoId = null, agendaDataAlvo = salvo.data))
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
private fun PreviewTelaNovoCompromisso() {
    androidx.compose.material3.MaterialTheme { TelaNovoCompromisso(contextoPreviewRestante()) }
}