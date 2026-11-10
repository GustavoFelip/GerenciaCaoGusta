package com.example.gerenciacao

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as CorAndroid
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Gera a mesma arte utilizada na previa e no PNG exportado, sem fotografias inventadas.
fun gerarArtePostagemAbrigo(titulo: String, texto: String, abrigo: String): Bitmap {
    val imagem = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(imagem)
    canvas.drawColor(CorAndroid.rgb(255, 240, 201))
    val laranja = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = CorAndroid.rgb(255, 178, 28) }
    canvas.drawRect(0f, 0f, 1080f, 16f, laranja)
    // Simbolo simples de patinha, sem substituir a marca oficial por uma marca inventada.
    canvas.drawOval(455f, 165f, 625f, 310f, laranja)
    listOf(425f to 125f, 495f to 85f, 585f to 85f, 655f to 125f).forEach { (x, y) -> canvas.drawCircle(x, y, 33f, laranja) }
    val tituloPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = CorAndroid.rgb(56, 41, 31); textSize = 66f; typeface = Typeface.create("sans-serif", Typeface.BOLD) }
    val textoPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = CorAndroid.rgb(107, 57, 25); textSize = 36f }
    val rodapePaint = TextPaint(textoPaint).apply { textSize = 30f }
    val blocos = listOf(Triple(titulo.take(120), tituloPaint, 340f), Triple(texto.take(420), textoPaint, 550f), Triple(abrigo.take(100), rodapePaint, 935f))
    blocos.forEachIndexed { indice, (conteudo, tinta, y) ->
        val layout = StaticLayout.Builder.obtain(conteudo, 0, conteudo.length, tinta, 920)
            .setAlignment(Layout.Alignment.ALIGN_CENTER).setMaxLines(if (indice == 0) 2 else if (indice == 1) 7 else 2)
            .setEllipsize(android.text.TextUtils.TruncateAt.END).setIncludePad(false).build()
        canvas.save(); canvas.translate(80f, y); layout.draw(canvas); canvas.restore()
    }
    return imagem
}

// Compõe o texto inicial usando somente os dados existentes da ficha.
fun tituloPostagemAbrigo(nome: String, modelo: String): String = when (modelo) {
    "Foi adotado!" -> "$nome foi adotado!"
    "Conheça o bichinho" -> "Conheça $nome"
    else -> "$nome procura um lar"
}

// Edita a arte, salva o PNG no local escolhido e abre o compartilhamento nativo do Android.
@Composable
fun TelaCriarPostagem(ctx: ContextoAbrigo) {
    val disponiveis = ctx.animais.filterNot { it.arquivado }
    var animalId by rememberSaveable { mutableStateOf(disponiveis.firstOrNull()?.id ?: "") }
    var modelo by rememberSaveable { mutableStateOf(ctx.dados.modeloPostagem) }
    val animal = disponiveis.firstOrNull { it.id == animalId }
    var titulo by rememberSaveable(animalId, modelo) { mutableStateOf(tituloPostagemAbrigo(animal?.nome ?: "Bichinho", modelo)) }
    var texto by rememberSaveable(animalId, modelo) { mutableStateOf(animal?.let { "${it.idade} · ${it.raca}\n${it.sobre}" } ?: "") }
    var editar by rememberSaveable { mutableStateOf(false) }
    var trocarPor by rememberSaveable { mutableStateOf<String?>(null) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }
    var pendente by rememberSaveable { mutableStateOf<ArrayList<String>?>(null) }
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val textoInicial = animal?.let { "${it.idade} · ${it.raca}\n${it.sobre}" } ?: ""
    val alterado = titulo != tituloPostagemAbrigo(animal?.nome ?: "Bichinho", modelo) || texto != textoInicial
    val arte by produceState<ImageBitmap?>(null, titulo, texto, ctx.dados.abrigo.nome) {
        value = withContext(Dispatchers.Default) { gerarArtePostagemAbrigo(titulo, texto, ctx.dados.abrigo.nome).asImageBitmap() }
    }
    // O URI criado pelo seletor permite compartilhar sem FileProvider ou permissao de armazenamento.
    val salvar = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri ->
        val conteudo = pendente
        pendente = null
        if (uri != null && conteudo != null) escopo.launch {
            ocupado = true
            val resultado = withContext(Dispatchers.IO) { runCatching {
                val imagem = gerarArtePostagemAbrigo(conteudo[0], conteudo[1], conteudo[2])
                try {
                    val destino = contexto.contentResolver.openOutputStream(uri) ?: error("Arquivo indisponível")
                    destino.use { check(imagem.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                } finally { imagem.recycle() }
            } }
            ocupado = false
            if (resultado.isFailure) aviso = "Não foi possível salvar a imagem. Tente outro local."
            else runCatching {
                val compartilhar = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"; putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = android.content.ClipData.newRawUri("Postagem GerenciaCão", uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                contexto.startActivity(Intent.createChooser(compartilhar, "Compartilhar publicação"))
            }.onFailure { aviso = "A imagem foi salva. Não foi possível abrir o menu de compartilhamento." }
        }
    }
    BaseRestante(ctx, "Criar postagem", alterado = alterado) {
        if (disponiveis.isEmpty()) Text("Cadastre um bichinho antes de criar uma postagem.")
        else {
            SeletorRestante("Bichinho", animal?.nome ?: "Selecione", disponiveis.map { it.id to it.nome }) {
                if (it != animalId) { if (alterado) trocarPor = "animal:$it" else animalId = it }
            }
            FiltrosRestantes(listOf("Procura um lar", "Foi adotado!", "Conheça o bichinho"), modelo) {
                if (it != modelo) { if (alterado) trocarPor = "modelo:$it" else { modelo = it; editar = false } }
            }
            Text("Prévia da publicação", color = MarromRestante)
            arte?.let { Image(it, "Prévia: $titulo. $texto", Modifier.fillMaxWidth().aspectRatio(1f), contentScale = ContentScale.Fit) }
            TextButton(onClick = { editar = !editar }) { Text(if (editar) "Ocultar edição" else "Editar texto") }
            if (editar) {
                CampoRestante("Título da publicação", titulo, { titulo = it.take(120) })
                CampoRestante("Texto da publicação", texto, { texto = it.take(420) }, longo = true)
            }
            Text("Revise os dados antes de compartilhar. A arte usa um símbolo; não inclui fotografia do animal.")
            BotaoRestante(if (ocupado) "Criando imagem…" else "Criar e compartilhar", !ocupado && pendente == null && animal != null) {
                when {
                    titulo.isBlank() -> aviso = "Informe um título para a publicação."
                    modelo == "Foi adotado!" && animal?.situacao != "Adotado" -> aviso = "Confirme primeiro a adoção na ficha. Este bichinho ainda não consta como adotado."
                    modelo == "Procura um lar" && animal?.situacao == "Adotado" -> aviso = "Este bichinho já está adotado. Escolha outro modelo."
                    else -> {
                        pendente = arrayListOf(titulo, texto, ctx.dados.abrigo.nome)
                        salvar.launch("GerenciaCao-${animalId.take(8)}.png")
                    }
                }
            }
        }
    }
    // Trocar o animal ou o modelo exige confirmar antes de descartar texto personalizado.
    if (trocarPor != null) AlertDialog(onDismissRequest = { trocarPor = null }, title = { Text("Descartar o texto editado?") },
        text = { Text("A troca vai preencher novamente o texto com os dados do modelo escolhido.") },
        confirmButton = { TextButton(onClick = {
            val alvo = trocarPor.orEmpty(); trocarPor = null
            if (alvo.startsWith("animal:")) animalId = alvo.removePrefix("animal:") else modelo = alvo.removePrefix("modelo:")
            editar = false
        }) { Text("Descartar e trocar") } }, dismissButton = { TextButton(onClick = { trocarPor = null }) { Text("Continuar editando") } })
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaCriarPostagem() {
    androidx.compose.material3.MaterialTheme { TelaCriarPostagem(contextoPreviewRestante()) }
}