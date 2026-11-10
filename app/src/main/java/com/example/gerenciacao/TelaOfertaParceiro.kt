package com.example.gerenciacao

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Permite somente enderecos HTTPS com host e sem credenciais embutidas.
fun linkOfertaValidoAbrigo(link: String): Boolean = runCatching {
    val uri = Uri.parse(link)
    uri.scheme.equals("https", true) && !uri.host.isNullOrBlank() && uri.userInfo == null &&
            link.none { it.isWhitespace() } && uri.host!!.contains('.')
}.getOrDefault(false)

// Apresenta as condicoes e abre um endereco real apenas quando ele foi informado.
@Composable
fun TelaOfertaParceiro(ctx: ContextoAbrigo) {
    val oferta = catalogoLoja(ctx.dados.ofertas).firstOrNull { it.id == ctx.dados.ofertaId }
    val navegador = LocalUriHandler.current
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    BaseRestante(ctx, "Oferta do parceiro", aba = "Loja") {
        if (oferta == null) Text("Oferta indisponível.")
        else {
            if (oferta.id.startsWith("vitrine-demo-")) CartaoRestante(CremeRestante) {
                Text("Exemplo de catálogo: o preço é ilustrativo e não representa uma oferta comercial real.")
            }
            val foto = fotoMiniaturaAdocao(oferta.imagemUri)
            CartaoRestante(CremeRestante) {
                if (foto != null) Image(foto, "Imagem da oferta", Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop)
                Text(oferta.titulo, fontWeight = FontWeight.Bold)
                Text(oferta.parceiro)
                Text(oferta.categoria)
                oferta.centavos?.let { Text("Preço informado: ${reaisAbrigo(it)}") }
            }
            CartaoRestante {
                Text("Sobre a oferta", fontWeight = FontWeight.Bold)
                Text(oferta.descricao)
                Text("Categoria: ${oferta.categoria}")
                Text("Atendimento: consulte a região")
                Text("Condições: consulte o parceiro")
            }
            CartaoRestante(VerdeRestante) {
                Text("Você será direcionado ao site do parceiro. A compra e o pagamento acontecem fora do GerenciaCão.")
                if (oferta.link.isNotBlank()) Text("Destino: ${Uri.parse(oferta.link).host.orEmpty()}")
            }
            BotaoRestante("Ver no site do parceiro", linkOfertaValidoAbrigo(oferta.link)) {
                runCatching { navegador.openUri(oferta.link) }.onFailure { aviso = "Não foi possível abrir o endereço. Verifique se há um navegador disponível." }
            }
            if (oferta.link.isBlank()) Text("Oferta demonstrativa: o parceiro ainda não informou um endereço real.")
        }
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaOfertaParceiro() {
    androidx.compose.material3.MaterialTheme { TelaOfertaParceiro(contextoPreviewRestante()) }
}