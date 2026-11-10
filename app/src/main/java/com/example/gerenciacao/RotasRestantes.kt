package com.example.gerenciacao

import androidx.compose.runtime.*
import androidx.compose.material3.Text

// Enumera apenas as novas rotas; a Agenda integrada e chamada pela rota ja existente.
val DestinosRestantes = setOf("novo_compromisso", "detalhes_compromisso", "registrar_doacao", "registrar_despesa",
    "relatorios", "parcerias", "oferta_parceiro", "criar_anuncio", "divulgacao", "criar_postagem",
    "meu_abrigo", "equipe_abrigo", "convidar_membro", "permissoes")

// Direciona cada nome de rota para uma tela completa, sem instanciar uma Activity por tela.
@Composable
fun RotasRestantes(destino: String, ctx: ContextoAbrigo) {
    when (destino) {
        "novo_compromisso" -> TelaNovoCompromisso(ctx)
        "detalhes_compromisso" -> TelaDetalhesCompromisso(ctx)
        "registrar_doacao" -> TelaRegistrarDoacao(ctx)
        "registrar_despesa" -> TelaRegistrarDespesa(ctx)
        "relatorios" -> TelaRelatorios(ctx)
        "parcerias" -> TelaParcerias(ctx)
        "oferta_parceiro" -> TelaOfertaParceiro(ctx)
        "criar_anuncio" -> TelaCriarAnuncio(ctx)
        "divulgacao" -> TelaDivulgacao(ctx)
        "criar_postagem" -> TelaCriarPostagem(ctx)
        "meu_abrigo" -> TelaMeuAbrigo(ctx)
        "equipe_abrigo" -> TelaEquipeAbrigo(ctx)
        "convidar_membro" -> TelaConvidarMembro(ctx)
        "permissoes" -> TelaPermissoes(ctx)
        else -> Text("Tela indisponível.")
    }
}

// Fornece dados controlados aos Previews e permite interacoes locais sem servidor.
@Composable
fun contextoPreviewRestante(): ContextoAbrigo {
    val animais = remember { AnimaisExemplo }
    var dados by remember { mutableStateOf(dadosRestantesExemplo(animais).let {
        it.copy(compromissoId = it.compromissos.firstOrNull()?.id, ofertaId = it.ofertas.firstOrNull()?.id)
    }) }
    return ContextoAbrigo(dados, animais, atualizar = { dados = it }, abrir = {}, voltar = {}, principal = {}, verAnimal = {})
}