package com.example.gerenciacao

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight

// Mostra os dados do compromisso escolhido, sem afirmar que uma data de exemplo e hoje.
@Composable
fun TelaDetalhesCompromisso(ctx: ContextoAbrigo) {
    val compromisso = ctx.dados.compromissos.firstOrNull { it.id == ctx.dados.compromissoId }
    var excluir by rememberSaveable(ctx.dados.compromissoId) { mutableStateOf(false) }
    BaseRestante(ctx, "Detalhes do compromisso", "Agenda") {
        if (compromisso == null) Text("Este compromisso não está disponível.")
        else {
            CartaoRestante(CremeRestante) {
                Text(compromisso.titulo, fontWeight = FontWeight.Bold, color = TextoRestante)
                Text("${compromisso.data} · ${compromisso.horario}")
                Text("Registrado na agenda", color = MarromRestante)
            }
            CartaoRestante {
                Text("Informações", fontWeight = FontWeight.Bold)
                Text("Tipo: ${compromisso.tipo}")
                val animal = ctx.animais.firstOrNull { it.id == compromisso.animalId }
                if (animal != null && !animal.arquivado) AtalhoRestante("Bichinho: ${animal.nome}", "Abrir ficha") { ctx.verAnimal(animal.id) }
                else Text(if (compromisso.animalId == null) "Bichinho: não vinculado" else "Bichinho: ficha arquivada ou indisponível")
                Text("Local: ${compromisso.local.ifBlank { "Não informado" }}")
            }
            CartaoRestante {
                Text("Observações", fontWeight = FontWeight.Bold)
                Text(compromisso.observacoes.ifBlank { "Sem observações." })
            }
            BotaoRestante("Reagendar") {
                ctx.atualizar(ctx.dados.copy(editarCompromissoId = compromisso.id))
                ctx.abrir("novo_compromisso")
            }
            TextButton(onClick = { excluir = true }) { Text("Excluir compromisso", color = MarromRestante) }
        }
    }
    // A remocao acontece somente depois de uma confirmacao explicita.
    if (excluir && compromisso != null) AlertDialog(onDismissRequest = { excluir = false },
        title = { Text("Excluir compromisso?") }, text = { Text("${compromisso.titulo} será removido da agenda local.") },
        confirmButton = { TextButton(onClick = {
            excluir = false
            ctx.atualizar(ctx.dados.copy(compromissos = ctx.dados.compromissos.filterNot { it.id == compromisso.id }, compromissoId = null))
            ctx.voltar()
        }) { Text("Excluir") } }, dismissButton = { TextButton(onClick = { excluir = false }) { Text("Cancelar") } })
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaDetalhesCompromisso() {
    androidx.compose.material3.MaterialTheme { TelaDetalhesCompromisso(contextoPreviewRestante()) }
}