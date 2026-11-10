package com.example.gerenciacao

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight

// Mostra a equipe e os pontos de acesso para convites e permissoes demonstrativas.
@Composable
fun TelaEquipeAbrigo(ctx: ContextoAbrigo) {
    var busca by rememberSaveable { mutableStateOf("") }
    BaseRestante(ctx, "Equipe do abrigo") {
        Text(ctx.dados.abrigo.nome, color = MarromRestante)
        Text("Quem acolhe com você", fontWeight = FontWeight.Bold)
        Text("${ctx.dados.membros.size} membros")
        CampoRestante("Buscar membro", busca, { busca = it })
        val visiveis = ctx.dados.membros.filter { it.nome.contains(busca.trim(), true) }
        if (visiveis.isEmpty()) Text("Nenhum membro encontrado.")
        visiveis.forEach { membro ->
            if (administradorAbrigo(ctx.dados)) AtalhoRestante(membro.nome, membro.funcao.titulo) {
                ctx.atualizar(ctx.dados.copy(membroId = membro.id))
                ctx.abrir("permissoes")
            } else CartaoRestante { Text(membro.nome); Text(membro.funcao.titulo) }
        }
        if (administradorAbrigo(ctx.dados)) {
            Text("Visualização de administração · exemplo local")
            CartaoRestante(VerdeRestante) { Text("Convide quem ajuda o abrigo", fontWeight = FontWeight.Bold); Text("Prepare um convite para compartilhar com quem colabora.") }
            BotaoRestante("Convidar membro") { ctx.abrir("convidar_membro") }
            AtalhoRestante("Gerenciar permissões", "Revisar funções da equipe") { ctx.abrir("permissoes") }
            if (ctx.dados.convites.isNotEmpty()) Text("${ctx.dados.convites.size} convites locais gerados; ainda não correspondem a membros.")
        }
    }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaEquipeAbrigo() {
    androidx.compose.material3.MaterialTheme { TelaEquipeAbrigo(contextoPreviewRestante()) }
}