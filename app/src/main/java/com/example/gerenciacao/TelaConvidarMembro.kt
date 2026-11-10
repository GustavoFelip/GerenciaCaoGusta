package com.example.gerenciacao

import android.content.Intent
import android.util.Patterns
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext

// Explica o cargo escolhido sem prometer autorizacao real antes da integracao com servidor.
fun descricaoFuncaoAbrigo(funcao: FuncaoAbrigo): String = when (funcao) {
    FuncaoAbrigo.ADMIN -> "Gerencia dados, equipe e permissões do abrigo."
    FuncaoAbrigo.CUIDADOR -> "Registra cuidados e atualiza fichas, conforme as permissões do abrigo."
    FuncaoAbrigo.VOLUNTARIO -> "Acessa somente as atividades autorizadas."
}

// Gera um convite demonstrativo; o usuario decide se quer compartilhar o texto pelo Android.
@Composable
fun TelaConvidarMembro(ctx: ContextoAbrigo) {
    var email by rememberSaveable { mutableStateOf("") }
    var funcaoNome by rememberSaveable { mutableStateOf(FuncaoAbrigo.CUIDADOR.name) }
    var conviteId by rememberSaveable { mutableStateOf<String?>(null) }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    val funcao = FuncaoAbrigo.valueOf(funcaoNome)
    val contexto = LocalContext.current
    val convite = ctx.dados.convites.firstOrNull { it.id == conviteId }
    BaseRestante(ctx, "Convidar membro", alterado = convite == null && (email.isNotBlank() || funcao != FuncaoAbrigo.CUIDADOR)) {
        if (!administradorAbrigo(ctx.dados)) Text("Acesso de administração necessário para preparar convites.")
        else {
            CartaoRestante(CremeRestante) { Text("Mais cuidado, em equipe"); Text("Convide alguém para colaborar com ${ctx.dados.abrigo.nome}.") }
            CampoRestante("E-mail do membro", email, { email = it })
            SeletorRestante("Função", funcao.titulo, FuncaoAbrigo.values().map { it.name to it.titulo }) { funcaoNome = it }
            CartaoRestante { Text(funcao.titulo); Text(descricaoFuncaoAbrigo(funcao)) }
            Text("O código é apenas demonstrativo. Ainda não há envio de e-mail, resgate de convite ou ingresso real na equipe.")
            BotaoRestante("Gerar convite local") {
                when {
                    !administradorAbrigo(ctx.dados) -> erro = "Acesso de administração necessário."
                    !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> erro = "Informe um e-mail válido."
                    ctx.dados.convites.any { it.email.equals(email.trim(), true) } -> erro = "Já existe um convite local para este e-mail."
                    else -> {
                        val id = java.util.UUID.randomUUID().toString()
                        val novo = ConviteAbrigo(id, email.trim(), funcao, java.util.UUID.randomUUID().toString())
                        ctx.atualizar(ctx.dados.copy(convites = ctx.dados.convites + novo))
                        conviteId = id
                    }
                }
            }
            Text("A pessoa deverá receber somente as funções permitidas quando a autenticação real for implementada.")
        }
    }
    // O aplicativo abre o seletor, mas nao envia mensagem ou e-mail por conta propria.
    if (convite != null) AlertDialog(onDismissRequest = { conviteId = null }, title = { Text("Convite local gerado") },
        text = { Text("${convite.email}\nFunção: ${convite.funcao.titulo}\nCódigo de demonstração:\n${convite.codigo}\n\nEste código ainda não permite entrar no aplicativo.") },
        confirmButton = { TextButton(onClick = {
            runCatching {
                contexto.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Convite de demonstração para ${ctx.dados.abrigo.nome}\nFunção: ${convite.funcao.titulo}\nCódigo: ${convite.codigo}\nSem validade de acesso: protótipo local.")
                }, "Compartilhar convite"))
            }.onFailure { erro = "Não foi possível abrir o compartilhamento." }
        }) { Text("Compartilhar") } }, dismissButton = { TextButton(onClick = { conviteId = null }) { Text("Fechar") } })
    erro?.let { MensagemRestante(it) { erro = null } }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaConvidarMembro() {
    androidx.compose.material3.MaterialTheme { TelaConvidarMembro(contextoPreviewRestante()) }
}