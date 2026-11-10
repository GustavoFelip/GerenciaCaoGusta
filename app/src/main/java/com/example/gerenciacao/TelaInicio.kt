package com.example.gerenciacao

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

// Mantem o creme e o marrom do aplicativo e acrescenta laranja apenas ao cabecalho da home.
private val FundoInicio = Color(0xFFFFFCF7)
private val TextoInicio = Color(0xFF38291F)
private val MarromInicio = Color(0xFF783E19)
private val AmareloInicio = Color(0xFFFFB414)
private val CremeInicio = Color(0xFFFFEFC5)
private val BordaInicio = Color(0xFFEADFD2)
private val CabecalhoInicio = Color(0xFFFFAE49)
private val BannerInicio = Color(0xFFFFD600)

// Reorganiza somente a tela inicial e conserva os callbacks utilizados no projeto atual.
@Composable
fun TelaInicio(
    aoSair: () -> Unit,
    aoAbrirAnimais: () -> Unit,
    aoNovaFicha: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit = {},
    nomePessoa: String = "Helena",
    categoriaPessoa: String = "Administradora · Abrigo Esperança",
    fotoPerfil: ImageBitmap? = null,
    aoAbrirPerfil: () -> Unit = {},
    aoNotificacoes: (() -> Unit)? = null,
    aoRegistrarDoacao: (() -> Unit)? = null,
    aoRegistrarDespesa: (() -> Unit)? = null,
    compromissos: List<CompromissoAbrigo> = emptyList(),
    aoAbrirParcerias: (() -> Unit)? = null,
    aoEncontrarCuidadores: (() -> Unit)? = null,
    imagemParceria: Int? = null
) {
    // Mantem o menu e os avisos locais durante a rotacao do celular.
    var confirmarSaida by rememberSaveable { mutableStateOf(false) }
    var mensagem by rememberSaveable { mutableStateOf<String?>(null) }
    val avisar: (String) -> Unit = { mensagem = it }
    val abrirParcerias = {
        aoAbrirParcerias?.invoke() ?: avisar("As parcerias estarão disponíveis em breve.")
    }
    // O botao Voltar usa a mesma confirmacao da opcao Sair, evitando saida acidental.
    BackHandler { confirmarSaida = true }

    // Atualiza a referencia de horario para que compromissos passados deixem o destaque.
    val instante by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000)
        }
    }
    val hoje = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(Date(instante))
    val horarioAtual = SimpleDateFormat("HH:mm", Locale.ROOT).format(Date(instante))
    val proximo = compromissos.asSequence()
        .filter { dataValidaAbrigo(it.data) && Regex("(?:[01][0-9]|2[0-3]):[0-5][0-9]").matches(it.horario) }
        .filter { chaveDataAbrigo(it.data) + it.horario >= chaveDataAbrigo(hoje) + horarioAtual }
        .minByOrNull { chaveDataAbrigo(it.data) + it.horario }

    // Animacao curta de entrada; o Compose respeita a escala de animacoes do Android.
    var conteudoVisivel by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { conteudoVisivel = true }
    val entrada by animateFloatAsState(if (conteudoVisivel) 1f else 0f,
        animationSpec = tween(280), label = "entradaInicio")

    // A home e as outras telas internas agora usam a mesma moldura e o mesmo cabecalho.
    val topoAtual = LocalCabecalhoApp.current
    CompositionLocalProvider(LocalCabecalhoApp provides topoAtual.copy(
        nome = nomePessoa, categoria = categoriaPessoa, foto = fotoPerfil,
        aoPerfil = aoAbrirPerfil, aoParcerias = { abrirParcerias() }, aoSair = aoSair,
        aoNotificacoes = aoNotificacoes ?: topoAtual.aoNotificacoes
    )) {
        // Liga o botão ao espaço flutuante da moldura, separado da barra inferior.
        MolduraPadraoApp(
            floatingActionButton = {
                BotaoAdicionarFlutuante(aoAdicionar = aoAdicionar)
            },
            bottomBar = {
                MenuInferiorInicio(
                    avisar = avisar,
                    aoAnimais = aoAbrirAnimais,
                    aoAdicionar = aoAdicionar,
                    aoAgenda = aoAgenda
                )
            }
        ) { espacos ->
            Column(Modifier.fillMaxSize().padding(espacos).graphicsLayer {
                alpha = entrada
                translationY = (1f - entrada) * 12.dp.toPx()
            }.verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp)) {
                BannerPedigreeInicio(imagemParceria)
                Spacer(Modifier.height(22.dp))
                Text("Ações rápidas", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoInicio)
                Spacer(Modifier.height(12.dp))

                // Atalhos menores: icone a esquerda e texto ao lado, com rolagem horizontal.
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val largura = (maxWidth * 0.47f).coerceIn(142.dp, 166.dp)
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AtalhoCompactoInicio("Cadastrar animal", R.drawable.inicio_pata, Color(0xFFFFD16D), largura) { aoNovaFicha() }
                        AtalhoCompactoInicio("Registrar doação", R.drawable.inicio_coracao, Color(0xFFC6E9B9), largura) {
                            aoRegistrarDoacao?.invoke() ?: avisar("O registro de doações estará disponível em breve.")
                        }
                        AtalhoCompactoInicio("Registrar despesa", R.drawable.inicio_recibo, Color(0xFFFFC8AB), largura) {
                            aoRegistrarDespesa?.invoke() ?: avisar("O registro de despesas estará disponível em breve.")
                        }
                        AtalhoCompactoInicio("Ver bichinhos", R.drawable.inicio_busca, Color(0xFFBEE8E2), largura) { aoAbrirAnimais() }
                    }
                }
                Spacer(Modifier.height(18.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Próximos cuidados", Modifier.weight(1f), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextoInicio)
                    TextButton(onClick = aoAgenda, contentPadding = PaddingValues(horizontal = 0.dp)) {
                        Text("Ver todos", fontSize = 12.sp, color = MarromInicio, fontWeight = FontWeight.SemiBold)
                    }
                }
                // Exibe o proximo compromisso real ou o estado vazio; nao inventa agendamentos.
                Surface(onClick = aoAgenda, color = CremeInicio, shape = RoundedCornerShape(19.dp)) {
                    Row(Modifier.fillMaxWidth().heightIn(min = 86.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(Color.White, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            IconeInicio(R.drawable.inicio_calendario, null, 20)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(proximo?.titulo ?: "Nenhum cuidado agendado", fontSize = 15.sp, lineHeight = 20.sp,
                                fontWeight = FontWeight.Bold, color = TextoInicio)
                            Text(proximo?.let { "${if (it.data == hoje) "Hoje" else it.data} · ${it.horario}" }
                                ?: "Abra a agenda para adicionar", fontSize = 12.sp, lineHeight = 16.sp, color = MarromInicio)
                        }
                        Spacer(Modifier.width(6.dp))
                        IconeInicio(R.drawable.inicio_seta, null, 15)
                    }
                }
                Spacer(Modifier.height(20.dp))

                // Reserva o acesso solicitado, sem consultar localizacao ou mostrar resultados ficticios.
                Surface(onClick = {
                    aoEncontrarCuidadores?.invoke() ?: avisar("Em breve você poderá encontrar cuidadores e abrigos na sua região.")
                }, color = Color.White, shape = RoundedCornerShape(19.dp), border = BorderStroke(1.dp, BordaInicio)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(38.dp).background(Color(0xFFE1F3E8), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                            IconeInicio(R.drawable.inicio_busca, null, 20)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Encontre cuidadores perto de você", fontSize = 15.sp, lineHeight = 20.sp,
                                fontWeight = FontWeight.Bold, color = TextoInicio)
                            Text("Conheça outros abrigos e quem acolhe na sua região.", fontSize = 12.sp, lineHeight = 17.sp, color = MarromInicio)
                        }
                        Spacer(Modifier.width(8.dp))
                        IconeInicio(R.drawable.inicio_seta, null, 15)
                    }
                }
            }
        }
    }
    // Confirma a saida da conta em vez de sair imediatamente por um toque acidental.
    if (confirmarSaida) AlertDialog(onDismissRequest = { confirmarSaida = false }, title = { Text("Sair da conta?") },
        text = { Text("Você voltará para a tela de login.") },
        confirmButton = { TextButton(onClick = { confirmarSaida = false; aoSair() }) { Text("Sair", color = MarromInicio) } },
        dismissButton = { TextButton(onClick = { confirmarSaida = false }) { Text("Cancelar", color = MarromInicio) } }, containerColor = FundoInicio)
    mensagem?.let { texto -> AlertDialog(onDismissRequest = { mensagem = null }, text = { Text(texto) },
        confirmButton = { TextButton(onClick = { mensagem = null }) { Text("Entendi", color = MarromInicio) } }, containerColor = FundoInicio) }
}

// Apresenta uma proposta de divulgacao da marca com texto separado da fotografia.
@Composable
private fun BannerPedigreeInicio(imagem: Int?) {
    val navegador = LocalUriHandler.current
    var erro by remember { mutableStateOf(false) }
    Surface(color = BannerInicio, shape = RoundedCornerShape(21.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(0.58f).padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Usa o nome da marca em texto, mantendo a leitura acessivel.
                Surface(color = Color(0xFFB5132A), shape = RoundedCornerShape(8.dp)) {
                    Text("PEDIGREE®", Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        fontSize = 17.sp, lineHeight = 20.sp, fontWeight = FontWeight.ExtraBold,
                        color = Color.White)
                }
                Text("Para quem ama\ncuidar de cães.", fontSize = 18.sp, lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold, color = TextoInicio)
                // Abre o site oficial no navegador; nao existe compra ou pagamento dentro do app.
                Button(onClick = {
                    runCatching { navegador.openUri("https://www.pedigree.com.br/") }
                        .onFailure { erro = true }
                }, modifier = Modifier.heightIn(min = 44.dp), contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3C231B), contentColor = Color.White)) {
                    Text("Conheça a marca", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            // Reutiliza a imagem escolhida no projeto, incluindo o nome cachorro_inicio do usuario.
            Box(Modifier.weight(0.42f).height(174.dp), contentAlignment = Alignment.BottomCenter) {
                if (imagem != null) Image(painterResource(imagem), null, Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit, alignment = Alignment.BottomCenter)
                else Icon(painterResource(R.drawable.inicio_pata), null,
                    Modifier.size(72.dp).padding(8.dp), tint = MarromInicio)
            }
        }
    }
    // Retorna um aviso caso o aparelho nao consiga abrir o navegador.
    if (erro) AlertDialog(onDismissRequest = { erro = false },
        text = { Text("Não foi possível abrir o site agora. Tente novamente mais tarde.") },
        confirmButton = { TextButton(onClick = { erro = false }) { Text("Entendi") } },
        containerColor = FundoInicio)
}

// Destaca a categoria e responde ao toque com uma pequena compressao e retorno suave.
@Composable
private fun AtalhoCompactoInicio(titulo: String, icone: Int, fundo: Color, largura: androidx.compose.ui.unit.Dp, acao: () -> Unit) {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val escala by animateFloatAsState(if (pressionado) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 550f), label = "escalaAtalho")
    val destaque = when (icone) {
        R.drawable.inicio_pata -> Color(0xFF8C4700)
        R.drawable.inicio_coracao -> Color(0xFF285B2B)
        R.drawable.inicio_recibo -> Color(0xFF943A21)
        else -> Color(0xFF176257)
    }
    Surface(onClick = acao, interactionSource = interacao,
        modifier = Modifier.width(largura).graphicsLayer { scaleX = escala; scaleY = escala },
        color = fundo, shape = RoundedCornerShape(17.dp),
        border = BorderStroke(1.dp, destaque.copy(alpha = 0.18f)), shadowElevation = 1.dp) {
        Row(Modifier.heightIn(min = 70.dp).padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).background(destaque, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                IconeInicio(icone, null, 20, Color.White)
            }
            Spacer(Modifier.width(9.dp))
            Text(titulo, Modifier.weight(1f), fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = TextoInicio)
        }
    }
}

// Renderiza os icones vetoriais existentes sem exigir novos arquivos para o menu.
@Composable
private fun IconeInicio(recurso: Int, descricao: String?, tamanho: Int = 24, cor: Color = MarromInicio) {
    Icon(painterResource(recurso), descricao, Modifier.size(tamanho.dp), tint = cor)
}

// Distribui os cinco destinos igualmente, sem reservar uma faixa para o botão mais.
@Composable
fun MenuInferiorInicio(
    avisar: (String) -> Unit,
    selecionado: String = "Início",
    aoInicio: () -> Unit = {},
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit
) {
    // Reutiliza os destinos compartilhados que já estão ligados no aplicativo.
    val abrirFinancas = LocalAbrirFinancas.current
    val abrirLoja = LocalAbrirLoja.current

    // Desenha somente a barra de navegação e a área dos gestos do Android.
    Surface(color = Color.White, border = BorderStroke(1.dp, BordaInicio)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Todos os itens recebem a mesma largura e o mesmo alinhamento.
            ItemMenuInicio(
                "Início", R.drawable.inicio_inicio,
                selecionado == "Início", Modifier.weight(1f), aoInicio
            )
            ItemMenuInicio(
                "Loja", R.drawable.inicio_loja,
                selecionado == "Loja", Modifier.weight(1f), abrirLoja
            )
            ItemMenuInicio(
                "Animais", R.drawable.inicio_pata,
                selecionado == "Animais", Modifier.weight(1f), aoAnimais
            )
            ItemMenuInicio(
                "Finanças", R.drawable.inicio_carteira,
                selecionado == "Finanças", Modifier.weight(1f), abrirFinancas
            )
            ItemMenuInicio(
                "Agenda", R.drawable.inicio_calendario,
                selecionado == "Agenda", Modifier.weight(1f), aoAgenda
            )
        }
    }
}

// Mostra o botão mais no espaço flutuante do Scaffold, com descrição acessível.
@Composable
fun BotaoAdicionarFlutuante(aoAdicionar: () -> Unit) {
    FloatingActionButton(
        onClick = aoAdicionar,
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        containerColor = AmareloInicio,
        contentColor = TextoInicio
    ) {
        IconeInicio(R.drawable.inicio_mais, "Adicionar", 28, TextoInicio)
    }
}

// Anima a selecao do menu e mantem os mesmos destinos e rotulos.
@Composable
private fun ItemMenuInicio(titulo: String, icone: Int, selecionado: Boolean, modifier: Modifier, aoClicar: () -> Unit) {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val corFundo by animateColorAsState(if (selecionado) CremeInicio else Color.Transparent, tween(160), label = "fundoMenu")
    val escala by animateFloatAsState(if (pressionado) 0.95f else 1f, spring(stiffness = 600f), label = "escalaMenu")
    Column(modifier.padding(horizontal = 2.dp).graphicsLayer { scaleX = escala; scaleY = escala }
        .clip(RoundedCornerShape(17.dp)).background(corFundo)
        .clickable(interactionSource = interacao, indication = null, onClick = aoClicar)
        .heightIn(min = 52.dp).padding(vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val cor = if (selecionado) MarromInicio else Color(0xFF7E7066)
        IconeInicio(icone, null, 21, cor)
        Spacer(Modifier.height(4.dp))
        Text(titulo, fontSize = 11.sp, lineHeight = 14.sp, color = cor,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Normal)
    }
}

// Mostra a composicao com o cachorro do banner no tamanho habitual de um celular.
@Preview(name = "Inicio · 360 dp", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewInicioRefinado() {
    MaterialTheme {
        TelaInicio(aoSair = {}, aoAbrirAnimais = {}, aoNovaFicha = {}, aoAdicionar = {},
            imagemParceria = R.drawable.pedigree)
    }
}

// Permite conferir que fontes ampliadas continuam legiveis em uma tela estreita.
@Preview(name = "Inicio · fonte ampliada", showBackground = true, widthDp = 320, heightDp = 800, fontScale = 1.3f)
@Composable
private fun PreviewInicioRefinadoFonteMaior() {
    MaterialTheme {
        TelaInicio(aoSair = {}, aoAbrirAnimais = {}, aoNovaFicha = {}, aoAdicionar = {},
            imagemParceria = R.drawable.pedigree)
    }
}