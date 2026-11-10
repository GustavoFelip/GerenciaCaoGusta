package com.example.gerenciacao

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext

// Cria um anuncio local na area demonstrativa do parceiro, sem publicar em servidor.
@Composable
fun TelaCriarAnuncio(ctx: ContextoAbrigo) {
    var tipo by rememberSaveable { mutableStateOf("Produto") }
    var titulo by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("Alimentação") }
    var parceiro by rememberSaveable { mutableStateOf("") }
    var preco by rememberSaveable { mutableStateOf("") }
    var link by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var imagem by rememberSaveable { mutableStateOf<String?>(null) }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmar by rememberSaveable { mutableStateOf(false) }
    val contexto = LocalContext.current
    // O Android fornece apenas a imagem escolhida, sem acesso amplo aos arquivos.
    val escolher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { contexto.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            imagem = uri.toString()
        }
    }
    BaseRestante(ctx, "Criar anúncio", alterado = titulo.isNotBlank() || parceiro.isNotBlank() || preco.isNotBlank() ||
            link.isNotBlank() || descricao.isNotBlank() || imagem != null || categoria != "Alimentação" || tipo != "Produto") {
        Text("Área do parceiro · demonstração local", color = MarromRestante)
        FiltrosRestantes(listOf("Produto", "Serviço"), tipo) { tipo = it }
        CampoRestante("Título", titulo, { titulo = it })
        SeletorRestante("Categoria", categoria, listOf("Alimentação", "Saúde", "Serviços").map { it to it }) { categoria = it }
        CampoRestante("Parceiro", parceiro, { parceiro = it })
        CampoRestante("Preço em reais · opcional", preco, { preco = it }, dica = "120,00 ou deixe vazio para consultar")
        CampoRestante("Link da oferta · HTTPS", link, { link = it })
        CampoRestante("Descrição e condições", descricao, { descricao = it }, longo = true)
        OutlinedButton(onClick = { escolher.launch(arrayOf("image/*")) }) { Text(if (imagem == null) "Adicionar imagem · opcional" else "Trocar imagem selecionada") }
        if (imagem != null) TextButton(onClick = { imagem = null }) { Text("Remover imagem") }
        CartaoRestante(CremeRestante) { Text("O link abre o site do parceiro. O pagamento ocorre fora do aplicativo. Publicar aqui disponibiliza o anúncio somente nesta demonstração local.") }
        BotaoRestante("Publicar anúncio") {
            erro = when {
                titulo.trim().length < 3 -> "Informe um título com pelo menos 3 caracteres."
                parceiro.trim().length < 2 -> "Informe o nome do parceiro."
                descricao.trim().length < 10 -> "Descreva a oferta e as condições com pelo menos 10 caracteres."
                !linkOfertaValidoAbrigo(link.trim()) -> "Informe um endereço HTTPS válido, sem espaços ou credenciais."
                preco.isNotBlank() && centavosAbrigo(preco) == null -> "Informe um preço válido, como 120,00."
                else -> null
            }
            if (erro == null) confirmar = true
        }
    }
    // A confirmacao deixa explicito que nao existe publicacao online neste prototipo.
    if (confirmar) AlertDialog(onDismissRequest = { confirmar = false }, title = { Text("Publicar no protótipo?") },
        text = { Text("O anúncio ficará disponível apenas na lista local de Parcerias. Nenhum servidor receberá os dados.") },
        confirmButton = { TextButton(onClick = {
            val oferta = OfertaAbrigo(titulo = titulo.trim(), categoria = categoria, parceiro = parceiro.trim(),
                descricao = descricao.trim(), link = link.trim(), tipo = tipo, centavos = if (preco.isBlank()) null else centavosAbrigo(preco), imagemUri = imagem)
            ctx.atualizar(ctx.dados.copy(ofertas = ctx.dados.ofertas + oferta))
            confirmar = false
            ctx.voltar()
        }) { Text("Publicar localmente") } }, dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } })
    erro?.let { MensagemRestante(it) { erro = null } }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaCriarAnuncio() {
    androidx.compose.material3.MaterialTheme { TelaCriarAnuncio(contextoPreviewRestante()) }
}