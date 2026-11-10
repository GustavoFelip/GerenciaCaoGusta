package com.example.gerenciacao

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

// Impede retirar o ultimo administrador ou alterar o proprio papel por esta demonstracao.
fun erroMudancaFuncaoAbrigo(dados: DadosRestantes, membroId: String, nova: FuncaoAbrigo): String? {
    if (!administradorAbrigo(dados)) return "Acesso de administração necessário."
    val membro = dados.membros.firstOrNull { it.id == membroId } ?: return "Escolha um membro válido."
    if (membro.id == dados.usuarioLocalId) return "Sua própria função não pode ser alterada nesta tela."
    if (membro.funcao == nova) return "A pessoa já possui essa função."
    if (membro.funcao == FuncaoAbrigo.ADMIN && nova != FuncaoAbrigo.ADMIN && dados.membros.count { it.funcao == FuncaoAbrigo.ADMIN } <= 1)
        return "O abrigo precisa manter ao menos um administrador."
    return null
}

// Permite revisar e confirmar a troca de funcao de um membro existente.
@Composable
fun TelaPermissoes(ctx: ContextoAbrigo) {
    var membroId by rememberSaveable { mutableStateOf(ctx.dados.membroId ?: ctx.dados.membros.firstOrNull { it.id != ctx.dados.usuarioLocalId }?.id.orEmpty()) }
    var novaNome by rememberSaveable(membroId) { mutableStateOf(ctx.dados.membros.firstOrNull { it.id == membroId }?.funcao?.name ?: FuncaoAbrigo.VOLUNTARIO.name) }
    var confirmar by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val membro = ctx.dados.membros.firstOrNull { it.id == membroId }
    val nova = FuncaoAbrigo.valueOf(novaNome)
    BaseRestante(ctx, "Permissões", alterado = membro != null && membro.funcao != nova) {
        if (!administradorAbrigo(ctx.dados)) Text("Acesso de administração necessário.")
        else {
            Text("Equipe de ${ctx.dados.abrigo.nome}", color = MarromRestante)
            Text("Acesso de administração · demonstração")
            FuncaoAbrigo.values().forEach { CartaoRestante { Text(it.titulo); Text(descricaoFuncaoAbrigo(it)) } }
            SeletorRestante("Membro", membro?.nome ?: "Selecione", ctx.dados.membros.filterNot { it.id == ctx.dados.usuarioLocalId }.map { it.id to it.nome }) { membroId = it }
            Text("Função atual: ${membro?.funcao?.titulo ?: "Não selecionada"}")
            SeletorRestante("Nova função", nova.titulo, FuncaoAbrigo.values().map { it.name to it.titulo }) { novaNome = it }
            CartaoRestante(CremeRestante) { Text("A alteração muda a função demonstrativa desta pessoa. Em produção, o servidor deverá validar e aplicar todas as autorizações.") }
            BotaoRestante("Atualizar função") {
                aviso = erroMudancaFuncaoAbrigo(ctx.dados, membroId, nova)
                if (aviso == null) confirmar = true
            }
            Text("Revise a função antes de confirmar.")
        }
    }
    // Revalida no momento da confirmacao e atualiza apenas o membro selecionado.
    if (confirmar) AlertDialog(onDismissRequest = { confirmar = false }, title = { Text("Confirmar alteração?") },
        text = { Text("${membro?.nome}: ${membro?.funcao?.titulo} → ${nova.titulo}") },
        confirmButton = { TextButton(onClick = {
            confirmar = false
            val erro = erroMudancaFuncaoAbrigo(ctx.dados, membroId, nova)
            if (erro != null) aviso = erro
            else {
                ctx.atualizar(ctx.dados.copy(membros = ctx.dados.membros.map { if (it.id == membroId) it.copy(funcao = nova) else it }))
                aviso = "Função atualizada nesta sessão local."
            }
        }) { Text("Confirmar") } }, dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } })
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaPermissoes() {
    androidx.compose.material3.MaterialTheme { TelaPermissoes(contextoPreviewRestante()) }
}