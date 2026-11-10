package com.example.gerenciacao

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Transporta os dados da pessoa. Funcao e abrigo nao sao alterados pelo formulario pessoal.
data class DadosMeuPerfil(
    val nome: String = "Helena Silva",
    val funcao: String = "Cuidadora",
    val abrigo: String = "Abrigo Esperança",
    val email: String = "helena@exemplo.com",
    val telefone: String = "11999999999",
    val fotoUri: String? = null
) : java.io.Serializable

// Usa a mesma paleta da home e do financeiro, com superficies suaves e texto marrom.
private val FundoPerfil = Color(0xFFFFFCF7)
private val TextoPerfil = Color(0xFF38291F)
private val MarromPerfil = Color(0xFF6B3919)
private val LaranjaPerfil = Color(0xFFFFB21C)
private val CremePerfil = Color(0xFFFFF0C9)
private val VerdePerfil = Color(0xFFE1F3E8)
private val PessegoPerfil = Color(0xFFFFE8DA)
private val BordaPerfil = Color(0xFFE8DFD4)
private val SecundarioPerfil = Color(0xFF807165)

// Carrega uma miniatura fora da interface, compartilhavel com o avatar da home.
@Composable
internal fun lembrarFotoMeuPerfil(endereco: String?): ImageBitmap? {
    val contexto = LocalContext.current
    val imagem by produceState<ImageBitmap?>(null, endereco) {
        value = null
        if (endereco != null) value = withContext(Dispatchers.IO) {
            runCatching {
                val uri = Uri.parse(endereco)
                val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
                val opcoes = BitmapFactory.Options().apply {
                    inSampleSize = 1
                    while (maxOf(limites.outWidth, limites.outHeight) / inSampleSize > 512) inSampleSize *= 2
                }
                contexto.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, opcoes)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    return imagem
}

// Apresenta o perfil e encaminha as alteracoes ao estado mantido pela Activity.
@Composable
fun TelaMeuPerfil(
    dados: DadosMeuPerfil,
    aoAtualizarPerfil: (DadosMeuPerfil) -> Unit,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit,
    aoSair: () -> Unit,
    aoAbrirAbrigo: (() -> Unit)? = null,
    aoAbrirEquipe: (() -> Unit)? = null,
    aoNotificacoes: (() -> Unit)? = null,
    aoSeguranca: (() -> Unit)? = null,
    aoAparencia: (() -> Unit)? = null,
    aoAjuda: (() -> Unit)? = null
) {
    // Controla somente os dialogos locais; nao simula autenticacao ou banco de dados.
    var editar by rememberSaveable { mutableStateOf(false) }
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val contexto = LocalContext.current
    val foto = lembrarFotoMeuPerfil(dados.fotoUri)
    val escolherFoto = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { contexto.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            aoAtualizarPerfil(dados.copy(fotoUri = uri.toString()))
        }
    }
    val abrir: ((() -> Unit)?, String) -> Unit = { destino, nome ->
        if (destino != null) destino() else aviso = "A tela de $nome ainda será criada."
    }
    BackHandler(onBack = aoVoltar)

    Column(Modifier.fillMaxSize().background(FundoPerfil)) {
        // Reproduz a faixa laranja e o menu fixo utilizados nas outras telas.
        // A cor laranja pertence ao cabecalho compartilhado.
        MolduraPadraoApp(
            modifier = Modifier.weight(1f), containerColor = FundoPerfil,
            bottomBar = {
                MenuInferiorInicio(avisar = { aviso = it }, selecionado = "",
                    aoInicio = aoInicio, aoAnimais = aoAnimais,
                    aoAdicionar = aoAdicionar, aoAgenda = aoAgenda)
            }
        ) { espacos ->
            Column(Modifier.fillMaxSize().padding(espacos).verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp)) {
                // Identifica o destino atual e permite retornar sem confundir com a home.
                TituloPaginaPadrao("Meu perfil", aoVoltar)
                Spacer(Modifier.height(20.dp))

                // O nome, a funcao e o abrigo sempre vem dos dados recebidos, nao do desenho.
                Surface(color = Color.White, shape = RoundedCornerShape(22.dp), border = BorderStroke(1.dp, BordaPerfil)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(86.dp)) {
                            Box(Modifier.fillMaxSize().clip(CircleShape).background(CremePerfil)
                                .clickable { escolherFoto.launch(arrayOf("image/*")) }, contentAlignment = Alignment.Center) {
                                if (foto != null) {
                                    Image(foto, "Foto de ${dados.nome}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Text(dados.nome.trim().take(1).uppercase().ifBlank { "?" }, fontSize = 38.sp,
                                        fontWeight = FontWeight.Bold, color = MarromPerfil)
                                }
                            }
                            FilledIconButton(onClick = { escolherFoto.launch(arrayOf("image/*")) },
                                modifier = Modifier.align(Alignment.BottomEnd).size(36.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = LaranjaPerfil)) {
                                Icon(painterResource(R.drawable.ficha_camera), "Alterar foto do perfil", Modifier.size(19.dp), tint = MarromPerfil)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(dados.nome, fontSize = 21.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold, color = TextoPerfil)
                            Spacer(Modifier.height(4.dp))
                            Text(dados.funcao, fontSize = 14.sp, color = SecundarioPerfil)
                            Text(dados.abrigo, fontSize = 13.sp, lineHeight = 18.sp, color = MarromPerfil)
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(onClick = { editar = true }, shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                border = BorderStroke(1.dp, BordaPerfil)) {
                                IconeMeuPerfil("editar", 17)
                                Spacer(Modifier.width(6.dp))
                                Text("Editar perfil", fontSize = 12.sp, color = MarromPerfil)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Agrupa informacoes do abrigo e da equipe sem permitir alterar cargos por aqui.
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AtalhoMeuPerfil("Meu abrigo", "Dados e contatos", "casa", CremePerfil, Modifier.weight(1f)) {
                        abrir(aoAbrirAbrigo, "meu abrigo")
                    }
                    AtalhoMeuPerfil("Equipe do abrigo", "Membros e funções", "equipe", VerdePerfil, Modifier.weight(1f)) {
                        abrir(aoAbrirEquipe, "equipe do abrigo")
                    }
                }
                TituloMeuPerfil("MINHA CONTA")
                GrupoMeuPerfil {
                    ItemMeuPerfil("Dados pessoais", "Nome, foto e contato", "pessoa", CremePerfil) { editar = true }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = BordaPerfil)
                    ItemMeuPerfil("Segurança e acesso", "Senha e privacidade", "escudo", PessegoPerfil) {
                        abrir(aoSeguranca, "segurança e acesso")
                    }
                }
                TituloMeuPerfil("PREFERÊNCIAS")
                GrupoMeuPerfil {
                    ItemMeuPerfil("Notificações", "Lembretes e avisos", "sino", CremePerfil) { abrir(aoNotificacoes, "notificações") }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = BordaPerfil)
                    ItemMeuPerfil("Aparência e acessibilidade", "Tema, texto e leitura", "aparencia", VerdePerfil) {
                        abrir(aoAparencia, "aparência e acessibilidade")
                    }
                }
                TituloMeuPerfil("PRECISA DE AJUDA?")
                GrupoMeuPerfil {
                    ItemMeuPerfil("Ajuda e sobre o aplicativo", "", "ajuda", CremePerfil) { abrir(aoAjuda, "ajuda") }
                }
                Spacer(Modifier.height(20.dp))
                // Solicita confirmacao antes de sair, evitando toque acidental.
                Surface(onClick = { confirmarSaida = true }, modifier = Modifier.fillMaxWidth(),
                    color = CremePerfil, shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconeMeuPerfil("sair")
                        Spacer(Modifier.width(14.dp))
                        Text("Sair da conta", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MarromPerfil)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Text("GerenciaCão · Feito para quem acolhe", Modifier.fillMaxWidth(), fontSize = 11.sp,
                    color = SecundarioPerfil, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }

    // Salva os dados no estado da Activity para atualizar a saudacao da home.
    if (editar) EditarDadosMeuPerfil(dados, aoCancelar = { editar = false }) { atualizados ->
        aoAtualizarPerfil(atualizados)
        editar = false
    }
    if (confirmarSaida) AlertDialog(onDismissRequest = { confirmarSaida = false },
        title = { Text("Sair da conta?") }, text = { Text("Você voltará para a tela de login.") },
        confirmButton = { TextButton(onClick = { confirmarSaida = false; aoSair() }) { Text("Sair", color = MarromPerfil) } },
        dismissButton = { TextButton(onClick = { confirmarSaida = false }) { Text("Cancelar", color = MarromPerfil) } },
        containerColor = FundoPerfil)
    aviso?.let { mensagem -> AlertDialog(onDismissRequest = { aviso = null }, title = { Text("Meu perfil") },
        text = { Text(mensagem) }, confirmButton = { TextButton(onClick = { aviso = null }) { Text("Fechar", color = MarromPerfil) } },
        containerColor = FundoPerfil) }
}

// Valida os dados pessoais. Funcao e abrigo permanecem sob a gestao da equipe.
@Composable
private fun EditarDadosMeuPerfil(dados: DadosMeuPerfil, aoCancelar: () -> Unit, aoSalvar: (DadosMeuPerfil) -> Unit) {
    var nome by rememberSaveable { mutableStateOf(dados.nome) }
    var email by rememberSaveable { mutableStateOf(dados.email) }
    var telefone by rememberSaveable { mutableStateOf(dados.telefone) }
    var tentou by rememberSaveable { mutableStateOf(false) }
    val erroNome = nome.trim().none { it.isLetter() }
    val erroEmail = !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val erroTelefone = telefone.length !in 10..11 || telefone.any { it !in '0'..'9' }
    AlertDialog(onDismissRequest = aoCancelar, title = { Text("Editar perfil", color = TextoPerfil) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()).imePadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(nome, { nome = it }, label = { Text("Nome") }, singleLine = true, isError = tentou && erroNome)
                OutlinedTextField(email, { email = it }, label = { Text("E-mail") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), isError = tentou && erroEmail)
                OutlinedTextField(telefone, { telefone = it }, label = { Text("Telefone com DDD") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), isError = tentou && erroTelefone)
                if (tentou && (erroNome || erroEmail || erroTelefone)) {
                    Text("Preencha um nome, um e-mail válido e um telefone com DDD e 10 ou 11 números.",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
                Text("Nesta demonstração, as alterações ficam apenas na sessão do aplicativo.", fontSize = 12.sp, color = SecundarioPerfil)
            }
        },
        confirmButton = { TextButton(onClick = {
            tentou = true
            if (!erroNome && !erroEmail && !erroTelefone) aoSalvar(dados.copy(nome = nome.trim(), email = email.trim(), telefone = telefone))
        }) { Text("Salvar alterações", color = MarromPerfil) } },
        dismissButton = { TextButton(onClick = aoCancelar) { Text("Cancelar", color = MarromPerfil) } },
        containerColor = FundoPerfil)
}

// Cria titulos de secao consistentes e faceis de localizar visualmente.
@Composable
private fun TituloMeuPerfil(titulo: String) {
    Text(titulo, Modifier.padding(top = 24.dp, bottom = 10.dp), fontSize = 12.sp,
        letterSpacing = 1.sp, fontWeight = FontWeight.Bold, color = MarromPerfil)
}

// Mantem cada grupo em uma unica superficie branca com borda discreta.
@Composable
private fun GrupoMeuPerfil(conteudo: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, border = BorderStroke(1.dp, BordaPerfil)) {
        Column(Modifier.fillMaxWidth(), content = conteudo)
    }
}

// Exibe as opcoes com icone e texto, sem depender apenas de cores.
@Composable
private fun ItemMeuPerfil(titulo: String, descricao: String, icone: String, fundo: Color, aoClicar: () -> Unit) {
    Surface(onClick = aoClicar, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().heightIn(min = 70.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(fundo, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { IconeMeuPerfil(icone) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = TextoPerfil)
                if (descricao.isNotBlank()) Text(descricao, fontSize = 12.sp, lineHeight = 17.sp, color = SecundarioPerfil)
            }
            Icon(painterResource(R.drawable.inicio_seta), null, Modifier.size(15.dp), tint = MarromPerfil)
        }
    }
}

// Desenha os atalhos do abrigo e da equipe com o mesmo tamanho.
@Composable
private fun AtalhoMeuPerfil(titulo: String, descricao: String, icone: String, fundo: Color, modifier: Modifier, aoClicar: () -> Unit) {
    Surface(onClick = aoClicar, modifier = modifier, color = fundo, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.heightIn(min = 110.dp).padding(14.dp)) {
            IconeMeuPerfil(icone, 28)
            Spacer(Modifier.height(10.dp))
            Text(titulo, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = TextoPerfil)
            Text(descricao, fontSize = 11.sp, lineHeight = 16.sp, color = SecundarioPerfil)
        }
    }
}

// Desenha os icones extras em vetor no proprio Compose, sem novos arquivos ou bibliotecas.
@Composable
private fun IconeMeuPerfil(tipo: String, tamanho: Int = 24) {
    if (tipo == "sino" || tipo == "casa") {
        Icon(painterResource(if (tipo == "sino") R.drawable.inicio_sino else R.drawable.inicio_inicio),
            null, Modifier.size(tamanho.dp), tint = MarromPerfil)
        return
    }
    Canvas(Modifier.size(tamanho.dp)) {
        withTransform({ scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) }) {
            val estilo = Stroke(width = 1.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            val caminho = Path()
            when (tipo) {
                "pessoa", "equipe" -> {
                    drawCircle(MarromPerfil, 3.5f, Offset(12f, 6f), style = estilo)
                    caminho.moveTo(4f, 22f); caminho.cubicTo(4f, 11f, 20f, 11f, 20f, 22f)
                    if (tipo == "equipe") {
                        drawCircle(MarromPerfil, 2.2f, Offset(3f, 9f), style = estilo)
                        drawCircle(MarromPerfil, 2.2f, Offset(21f, 9f), style = estilo)
                        caminho.moveTo(0f, 20f); caminho.quadraticBezierTo(0f, 13f, 5f, 14f)
                        caminho.moveTo(24f, 20f); caminho.quadraticBezierTo(24f, 13f, 19f, 14f)
                    }
                }
                "escudo" -> {
                    caminho.moveTo(3f, 4f); caminho.lineTo(12f, 1f); caminho.lineTo(21f, 4f)
                    caminho.cubicTo(21f, 14f, 18f, 19f, 12f, 23f)
                    caminho.cubicTo(6f, 19f, 3f, 14f, 3f, 4f); caminho.close()
                }
                "aparencia" -> {
                    drawCircle(MarromPerfil, 5f, Offset(12f, 12f), style = estilo)
                    for (i in 0..7) {
                        val angulo = i * Math.PI / 4
                        val direcao = Offset(kotlin.math.cos(angulo).toFloat(), kotlin.math.sin(angulo).toFloat())
                        drawLine(MarromPerfil, Offset(12f, 12f) + direcao * 8f, Offset(12f, 12f) + direcao * 11f, 1.7f, cap = StrokeCap.Round)
                    }
                }
                "ajuda" -> {
                    drawCircle(MarromPerfil, 10f, Offset(12f, 12f), style = estilo)
                    caminho.moveTo(9f, 8f); caminho.cubicTo(9f, 3f, 18f, 6f, 14f, 10f)
                    caminho.quadraticBezierTo(12f, 11f, 12f, 14f)
                    drawCircle(MarromPerfil, 1f, Offset(12f, 18f))
                }
                "sair" -> {
                    caminho.moveTo(10f, 3f); caminho.lineTo(3f, 3f); caminho.lineTo(3f, 21f); caminho.lineTo(10f, 21f)
                    caminho.moveTo(9f, 12f); caminho.lineTo(23f, 12f)
                    caminho.moveTo(18f, 7f); caminho.lineTo(23f, 12f); caminho.lineTo(18f, 17f)
                }
                else -> {
                    caminho.moveTo(3f, 17f); caminho.lineTo(17f, 3f); caminho.lineTo(22f, 8f)
                    caminho.lineTo(8f, 22f); caminho.lineTo(2f, 23f); caminho.close()
                    caminho.moveTo(14f, 6f); caminho.lineTo(19f, 11f)
                }
            }
            drawPath(caminho, MarromPerfil, style = estilo)
        }
    }
}

// Permite conferir a tela no editor com dados ficticios, sem banco de dados.
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewMeuPerfil() {
    MaterialTheme {
        TelaMeuPerfil(DadosMeuPerfil(), {}, {}, {}, {}, {}, {}, {})
    }
}