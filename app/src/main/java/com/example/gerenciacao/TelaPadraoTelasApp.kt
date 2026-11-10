package com.example.gerenciacao

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Disponibiliza a ação compartilhada e o posicionamento do botão flutuante.
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.FabPosition

// Uma unica origem para as cores, margens e medidas do cabecalho de todas as telas internas.
internal val LaranjaPadraoApp = Color(0xFFFFAE49)
internal val FundoPadraoApp = Color(0xFFFFFCF7)
internal val TextoPadraoApp = Color(0xFF38291F)
internal val MarromPadraoApp = Color(0xFF783E19)
internal val CremePadraoApp = Color(0xFFFFEFC5)

// A Activity compartilha a pessoa atual e os destinos; a foto nao e duplicada por tela.
data class DadosCabecalhoApp(
    val nome: String = "Helena",
    val categoria: String = "Administradora · Abrigo Esperança",
    val foto: ImageBitmap? = null,
    val aoPerfil: () -> Unit = {},
    val aoParcerias: () -> Unit = {},
    val aoSair: () -> Unit = {},
    val aoNotificacoes: (() -> Unit)? = null,
    val aoDescartarRascunho: () -> Unit = {}
)

// O valor padrao permite abrir Previews sem uma Activity, sem fixar dados no aplicativo real.
val LocalCabecalhoApp = staticCompositionLocalOf { DadosCabecalhoApp() }

// Compartilha a ação padrão do botão adicionar entre as telas internas.
val LocalAdicionarFlutuanteApp = staticCompositionLocalOf<() -> Unit> { {} }

// Repete exatamente o padrao da home: avatar de 48 dp, saudacao, categoria e menu de tres pontos.
@Composable
fun CabecalhoPadraoApp(interceptarNavegacao: (() -> Unit) -> Unit = { it() }) {
    val dados = LocalCabecalhoApp.current
    var menu by rememberSaveable { mutableStateOf(false) }
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val executar: (() -> Unit) -> Unit = { acao -> menu = false; interceptarNavegacao(acao) }
    Row(Modifier.fillMaxWidth().background(LaranjaPadraoApp).statusBarsPadding()
        .padding(horizontal = 22.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(CremePadraoApp)
            .semantics { contentDescription = "Abrir perfil de ${dados.nome}" }
            .clickable { executar(dados.aoPerfil) }, contentAlignment = Alignment.Center) {
            val foto = dados.foto
            if (foto != null) Image(foto, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Text(dados.nome.trim().take(1).uppercase().ifBlank { "?" }, fontSize = 23.sp,
                fontWeight = FontWeight.Bold, color = MarromPadraoApp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val primeiroNome = dados.nome.trim().substringBefore(" ")
            Text(if (primeiroNome.isBlank()) "Olá!" else "Olá, $primeiroNome!", fontSize = 21.sp,
                lineHeight = 25.sp, fontWeight = FontWeight.Bold, color = TextoPadraoApp)
            Text(dados.categoria, fontSize = 11.sp, lineHeight = 15.sp, color = MarromPadraoApp)
        }
        Spacer(Modifier.width(6.dp))
        Box {
            IconButton(onClick = { menu = true }, modifier = Modifier.size(48.dp)
                .semantics { contentDescription = "Abrir menu" }) {
                Canvas(Modifier.size(24.dp)) {
                    listOf(0.23f, 0.5f, 0.77f).forEach { y -> drawCircle(MarromPadraoApp, 2.dp.toPx(), Offset(size.width / 2, size.height * y)) }
                }
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Meu perfil") }, onClick = { executar(dados.aoPerfil) })
                DropdownMenuItem(text = { Text("Notificações") }, onClick = {
                    menu = false
                    val abrir = dados.aoNotificacoes
                    if (abrir != null) executar(abrir) else aviso = "A tela de notificações será incluída em breve."
                })
                DropdownMenuItem(text = { Text("Parcerias") }, onClick = { executar(dados.aoParcerias) })
                HorizontalDivider()
                DropdownMenuItem(text = { Text("Sair da conta") }, onClick = { menu = false; confirmarSaida = true })
            }
        }
    }
    // Mantem a mesma confirmacao de saida em qualquer tela interna.
    if (confirmarSaida) AlertDialog(onDismissRequest = { confirmarSaida = false }, title = { Text("Sair da conta?") },
        text = { Text("Você voltará para a tela de login.") },
        confirmButton = { TextButton(onClick = { confirmarSaida = false; executar(dados.aoSair) }) { Text("Sair", color = MarromPadraoApp) } },
        dismissButton = { TextButton(onClick = { confirmarSaida = false }) { Text("Cancelar", color = MarromPadraoApp) } }, containerColor = FundoPadraoApp)
    aviso?.let { texto -> AlertDialog(onDismissRequest = { aviso = null }, text = { Text(texto) },
        confirmButton = { TextButton(onClick = { aviso = null }) { Text("Entendi") } }, containerColor = FundoPadraoApp) }
}

// Padroniza cabeçalho, conteúdo, navegação e botão adicionar nas telas internas.
@Composable
fun MolduraPadraoApp(
    modifier: Modifier = Modifier,
    containerColor: Color = FundoPadraoApp,
    snackbarHost: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    interceptarCabecalho: (() -> Unit) -> Unit = { it() },
    floatingActionButton: @Composable () -> Unit = {
        // Usa a ação compartilhada quando a tela não definir um botão próprio.
        val abrirAdicionar = LocalAdicionarFlutuanteApp.current
        BotaoAdicionarFlutuante(
            aoAdicionar = {
                // Reaproveita a proteção de rascunho nas telas que já a configuram.
                interceptarCabecalho(abrirAdicionar)
            }
        )
    },
    conteudo: @Composable (PaddingValues) -> Unit
) {
    // Posiciona o mais sobre o conteúdo, acima da barra, sem criar uma faixa extra.
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LaranjaPadraoApp,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { CabecalhoPadraoApp(interceptarCabecalho) },
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = FabPosition.End
    ) { espacos ->
        // Preserva os cantos arredondados e evita aplicar o espaçamento duas vezes.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacos)
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(containerColor)
        ) {
            conteudo(PaddingValues(0.dp))
        }
    }
}

// Padroniza os titulos de pagina; a seta de voltar fica no conteudo, sem deslocar o avatar.
@Composable
fun TituloPaginaPadrao(titulo: String, aoVoltar: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        if (aoVoltar != null) IconButton(onClick = aoVoltar,
            modifier = Modifier.semantics { contentDescription = "Voltar" }) {
            Text("←", fontSize = 27.sp, color = MarromPadraoApp)
        }
        Text(titulo, Modifier.weight(1f), fontSize = 24.sp, lineHeight = 29.sp,
            fontWeight = FontWeight.Bold, color = TextoPadraoApp)
    }
}

// Protege rascunhos quando o novo cabecalho e usado para sair de um formulario.
@Composable
fun protegerRascunhoNoCabecalho(alterado: Boolean): (() -> Unit) -> Unit {
    var acaoPendente by remember { mutableStateOf<(() -> Unit)?>(null) }
    val dados = LocalCabecalhoApp.current
    if (acaoPendente != null) AlertDialog(onDismissRequest = { acaoPendente = null },
        title = { Text("Descartar alterações?") }, text = { Text("As alterações deste formulário ainda não foram salvas.") },
        confirmButton = { TextButton(onClick = {
            val acao = acaoPendente; acaoPendente = null
            dados.aoDescartarRascunho(); acao?.invoke()
        }) { Text("Descartar e sair") } },
        dismissButton = { TextButton(onClick = { acaoPendente = null }) { Text("Continuar editando") } }, containerColor = FundoPadraoApp)
    return { acao -> if (alterado) acaoPendente = acao else acao() }
}