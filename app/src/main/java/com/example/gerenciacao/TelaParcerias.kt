package com.example.gerenciacao

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight

// Organiza as ofertas disponiveis na colecao local, incluindo anuncios criados no prototipo.
@Composable
fun TelaParcerias(ctx: ContextoAbrigo) {
    var busca by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Tudo") }
    var ordem by rememberSaveable { mutableStateOf("Nome") }
    val ofertas = ctx.dados.ofertas.filter {
        (it.titulo.contains(busca.trim(), true) || it.parceiro.contains(busca.trim(), true)) &&
                (filtro == "Tudo" || it.categoria == if (filtro == "Ração") "Alimentação" else filtro)
    }.let { if (ordem == "Nome") it.sortedBy { oferta -> oferta.titulo.lowercase() } else it.reversed() }
    BaseRestante(ctx, "Parcerias") {
        Text("Ofertas para quem acolhe", color = MarromRestante)
        CampoRestante("Buscar produtos e serviços", busca, { busca = it })
        FiltrosRestantes(listOf("Tudo", "Ração", "Saúde", "Serviços"), filtro) { filtro = it }
        SeletorRestante("Ordenar", ordem, listOf("Nome" to "Nome", "Recentes" to "Recentes")) { ordem = it }
        Text("Ofertas para o abrigo", fontWeight = FontWeight.Bold)
        if (ofertas.isEmpty()) Text("Nenhuma oferta encontrada.")
        ofertas.forEach { oferta ->
            AtalhoRestante(oferta.titulo, "${oferta.categoria} · ${oferta.parceiro}\nVer condições") {
                ctx.atualizar(ctx.dados.copy(ofertaId = oferta.id))
                ctx.abrir("oferta_parceiro")
            }
        }
        Text("Consulte valores e condições com o parceiro. As ofertas iniciais são exemplos sem endereço comercial real.")
        AtalhoRestante("Área do parceiro · demonstração", "Criar um anúncio local", CremeRestante) { ctx.abrir("criar_anuncio") }
    }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaParcerias() {
    androidx.compose.material3.MaterialTheme { TelaParcerias(contextoPreviewRestante()) }
}