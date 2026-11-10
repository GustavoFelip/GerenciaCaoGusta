package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Conserva o destino Loja que ja e usado pela navegacao do aplicativo.
val LocalAbrirLoja = staticCompositionLocalOf<() -> Unit> { {} }

// EDITE AQUI AS IMAGENS: troque null por R.drawable.nome_da_sua_imagem.
// Salve cada arquivo individual na pasta app/src/main/res/drawable.
private val ImagensIndividuaisLoja: Map<String, Int?> = mapOf(
    "vitrine-demo-racao" to null,        // Ex.: R.drawable.racao_premium
    "vitrine-demo-umido" to null,        // Ex.: R.drawable.alimento_umido
    "vitrine-demo-petisco" to R.drawable.pedigree, // Sua imagem ja existente.
    "vitrine-demo-banho" to null,        // Ex.: R.drawable.banho_tosa
    "vitrine-demo-spa" to null           // Ex.: R.drawable.spa_bichinhos
)

// Exemplos ilustrativos do molde; seus valores nao representam ofertas comerciais reais.
private val OfertasDemonstrativasLoja = listOf(
    OfertaAbrigo(id = "vitrine-demo-racao", titulo = "Ração Premium", categoria = "Alimentação", parceiro = "Parceiro de exemplo", centavos = 6990L,
        descricao = "Produto demonstrativo com valor ilustrativo. Consulte embalagem, composição, disponibilidade e preço real com o parceiro."),
    OfertaAbrigo(id = "vitrine-demo-umido", titulo = "Alimento úmido", categoria = "Alimentação", parceiro = "Parceiro de exemplo", centavos = 599L,
        descricao = "Embalagem ilustrativa sem marca real. Este preço é apenas um exemplo para visualizar a vitrine."),
    OfertaAbrigo(id = "vitrine-demo-petisco", titulo = "Alimento úmido Pedigree", categoria = "Alimentação", parceiro = "Exemplo de catálogo", centavos = 499L,
        descricao = "Usa a imagem Pedigree adicionada ao projeto. O preço é ilustrativo, não uma oferta real da marca ou de um varejista."),
    OfertaAbrigo(id = "vitrine-demo-banho", titulo = "Banho e tosa", categoria = "Serviços", parceiro = "Prestador de exemplo", tipo = "Serviço", centavos = 7999L,
        descricao = "Serviço demonstrativo. Confirme o atendimento, o preço real e as condições com o prestador."),
    OfertaAbrigo(id = "vitrine-demo-spa", titulo = "Spa para bichinhos", categoria = "Serviços", parceiro = "Prestador de exemplo", tipo = "Serviço", centavos = 7999L,
        descricao = "Serviço demonstrativo com imagem ilustrativa. Confirme condições e disponibilidade com o prestador.")
)

// Os detalhes recebem exatamente os dados do cartao, incluindo os exemplos apresentados primeiro.
fun catalogoLoja(ofertasCadastradas: List<OfertaAbrigo>): List<OfertaAbrigo> =
    OfertasDemonstrativasLoja.filter { exemplo -> ofertasCadastradas.none { it.id == exemplo.id } } + ofertasCadastradas

// Preserva a assinatura anterior e combina busca, categoria e tipo.
fun filtrarCatalogoLoja(ofertas: List<OfertaAbrigo>, busca: String, tipo: String, categoria: String): List<OfertaAbrigo> = ofertas.filter {
    (busca.isBlank() || "${it.titulo} ${it.parceiro} ${it.categoria}".contains(busca.trim(), true)) &&
            (tipo == "Todos" || if (tipo == "Serviços") it.tipo == "Serviço" else it.tipo != "Serviço") &&
            (categoria == "Todas" || it.categoria == categoria)
}

// Aplica os filtros do painel sem misturar valores ausentes com precos iguais a zero.
fun aplicarFiltrosPrecoLoja(ofertas: List<OfertaAbrigo>, parceiro: String, minimo: Long?, maximo: Long?, ordem: String): List<OfertaAbrigo> {
    val resultado = ofertas.filter {
        (parceiro == "Todos" || it.parceiro == parceiro) &&
                (minimo == null || it.centavos?.let { valor -> valor >= minimo } == true) &&
                (maximo == null || it.centavos?.let { valor -> valor <= maximo } == true)
    }
    return when (ordem) {
        "Menor preço" -> resultado.sortedBy { it.centavos ?: Long.MAX_VALUE }
        "Maior preço" -> resultado.sortedByDescending { it.centavos ?: Long.MIN_VALUE }
        else -> resultado
    }
}

// Reproduz a composicao da referencia: busca, tres filtros, banner e duas fileiras de cartoes.
@Composable
fun TelaLoja(ctx: ContextoAbrigo) {
    var busca by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf("Todos") }
    var categoria by rememberSaveable { mutableStateOf("Todas") }
    var parceiro by rememberSaveable { mutableStateOf("Todos") }
    var minimo by rememberSaveable { mutableStateOf<Long?>(null) }
    var maximo by rememberSaveable { mutableStateOf<Long?>(null) }
    var ordem by rememberSaveable { mutableStateOf("Destaques") }
    var mostrarFiltros by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val catalogo = catalogoLoja(ctx.dados.ofertas)
    val visiveis = aplicarFiltrosPrecoLoja(filtrarCatalogoLoja(catalogo, busca, tipo, categoria), parceiro, minimo, maximo, ordem)
    val destaque = visiveis.firstOrNull { it.id == "vitrine-demo-racao" } ?: visiveis.firstOrNull()
    val produtos = visiveis.filter { it.tipo != "Serviço" && it.id != "vitrine-demo-racao" }
    val servicos = visiveis.filter { it.tipo == "Serviço" }
    val filtrosAtivos = categoria != "Todas" || parceiro != "Todos" || minimo != null || maximo != null || ordem != "Destaques"
    val abrir: (OfertaAbrigo) -> Unit = { ctx.atualizar(ctx.dados.copy(ofertaId = it.id)); ctx.abrir("oferta_parceiro") }
    BackHandler { ctx.principal("inicio") }
    MolduraPadraoApp(bottomBar = {
        MenuInferiorInicio(avisar = { aviso = it }, selecionado = "Loja", aoInicio = { ctx.principal("inicio") },
            aoAnimais = { ctx.principal("animais") }, aoAdicionar = { ctx.principal("adicionar") }, aoAgenda = { ctx.principal("agenda") })
    }) { espacos ->
        LazyColumn(Modifier.fillMaxSize().padding(espacos).imePadding(),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Parcerias", color = TextoRestante, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(14.dp))
                // O botao de filtros fica dentro da busca, como no molde enviado.
                OutlinedTextField(value = busca, onValueChange = { busca = it.take(120) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    placeholder = { Text("Buscar por produto, serviço…", fontSize = 12.sp) },
                    leadingIcon = { Icon(painterResource(R.drawable.inicio_busca), null, Modifier.size(19.dp), tint = MarromRestante) },
                    trailingIcon = {
                        FilledIconButton(onClick = { mostrarFiltros = true }, modifier = Modifier.size(38.dp)
                            .semantics { contentDescription = "Abrir filtros da loja" },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MarromRestante)) {
                            Canvas(Modifier.size(18.dp)) {
                                listOf(0.3f to 0.85f, 0.5f to 0.6f, 0.7f to 0.3f).forEach { (y, largura) ->
                                    drawLine(Color(0xFFFFC563), Offset(size.width * (1f - largura) / 2, size.height * y),
                                        Offset(size.width * (1f + largura) / 2, size.height * y), 1.8.dp.toPx())
                                }
                            }
                        }
                    }, shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LaranjaRestante, unfocusedBorderColor = BordaRestante,
                        focusedContainerColor = FundoRestante, unfocusedContainerColor = FundoRestante))
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Todos", "Produtos", "Serviços").forEach { opcao ->
                        FilterChip(selected = tipo == opcao, onClick = { tipo = opcao },
                            label = { Text(opcao, fontSize = 11.sp) }, shape = CircleShape, border = null,
                            colors = FilterChipDefaults.filterChipColors(containerColor = MarromRestante, labelColor = Color.White,
                                selectedContainerColor = Color(0xFF315F85), selectedLabelColor = Color.White))
                    }
                }
                if (filtrosAtivos) TextButton(onClick = { categoria = "Todas"; parceiro = "Todos"; minimo = null; maximo = null; ordem = "Destaques" }) {
                    Text("Limpar filtros aplicados", fontSize = 11.sp, color = MarromRestante)
                }
            }
            if (destaque != null) item { BannerModeloLoja(destaque) { abrir(destaque) } }
            if (produtos.isNotEmpty()) item { FileiraModeloLoja("Produtos em destaque", produtos) { abrir(it) } }
            if (servicos.isNotEmpty()) item { FileiraModeloLoja("Serviços", servicos) { abrir(it) } }
            if (visiveis.isEmpty()) item { CartaoRestante { Text("Nenhuma oferta corresponde à busca e aos filtros.") } }
            item { Text("Vitrine demonstrativa · imagens ilustrativas e preços de exemplo.", fontSize = 10.sp, color = MarromRestante) }
        }
    }
    if (mostrarFiltros) PainelFiltrosModeloLoja(catalogo, categoria, parceiro, minimo, maximo, ordem,
        fechar = { mostrarFiltros = false }, aplicar = { c, p, min, max, o ->
            categoria = c; parceiro = p; minimo = min; maximo = max; ordem = o; mostrarFiltros = false
        })
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Mantem foto a esquerda e valor/titulo a direita, sem transformar o banner em um cartao comum.
@Composable
private fun BannerModeloLoja(oferta: OfertaAbrigo, abrir: () -> Unit) {
    Surface(onClick = abrir, color = Color.White, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BordaRestante)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FotoModeloLoja(oferta, Modifier.weight(0.53f).height(136.dp), true)
            Column(Modifier.weight(0.47f).padding(end = 13.dp, top = 12.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(oferta.centavos?.let(::reaisAbrigo) ?: "Consulte", fontSize = 25.sp, fontWeight = FontWeight.SemiBold,
                    color = MarromRestante, textAlign = TextAlign.End)
                Text(oferta.titulo, fontSize = 19.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium,
                    color = MarromRestante, textAlign = TextAlign.End)
            }
        }
    }
}

// Duas opcoes aparecem lado a lado e a fileira desliza para mostrar os outros anuncios.
@Composable
private fun FileiraModeloLoja(titulo: String, ofertas: List<OfertaAbrigo>, abrir: (OfertaAbrigo) -> Unit) {
    val fonteAmpliada = LocalDensity.current.fontScale > 1.2f
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MarromRestante)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val largura = ((maxWidth - 12.dp) / 2).coerceAtLeast(if (fonteAmpliada) 156.dp else 118.dp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 3.dp)) {
                items(ofertas, key = { it.id }) { oferta ->
                    Surface(onClick = { abrir(oferta) }, modifier = Modifier.width(largura), color = Color.White,
                        shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, BordaRestante), shadowElevation = 1.dp) {
                        Column {
                            FotoModeloLoja(oferta, Modifier.fillMaxWidth().aspectRatio(if (oferta.tipo == "Serviço") 1.7f else 1.3f))
                            Column(Modifier.padding(horizontal = 7.dp, vertical = 7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(oferta.titulo, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextoRestante)
                                Text(oferta.centavos?.let(::reaisAbrigo) ?: "Consultar valor", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextoRestante)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Exibe somente a imagem individual escolhida para este item, sempre inteira e sem recortar.
@Composable
private fun FotoModeloLoja(oferta: OfertaAbrigo, modifier: Modifier, banner: Boolean = false) {
    val fotoCadastrada = fotoMiniaturaAdocao(oferta.imagemUri)
    val recursoIndividual = ImagensIndividuaisLoja[oferta.id]
    val espacoImagem = if (banner) 4.dp else 6.dp
    Box(modifier.background(Color.White), contentAlignment = Alignment.Center) {
        when {
            // Imagem escolhida no formulario de anuncio, quando existir.
            fotoCadastrada != null -> Image(fotoCadastrada, oferta.titulo,
                Modifier.fillMaxSize().padding(espacoImagem), contentScale = ContentScale.Fit)
            // Imagem que voce vinculou manualmente na tabela acima.
            recursoIndividual != null -> Image(painterResource(recursoIndividual), oferta.titulo,
                Modifier.fillMaxSize().padding(espacoImagem), contentScale = ContentScale.Fit)
            // Mantem o espaco do cartao enquanto a imagem individual ainda nao foi adicionada.
            else -> Column(Modifier.fillMaxSize().padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Icon(painterResource(R.drawable.inicio_pata), null, Modifier.size(25.dp), tint = MarromRestante.copy(alpha = 0.45f))
                Spacer(Modifier.height(7.dp))
                Text(if (oferta.tipo == "Serviço") "Imagem do serviço" else "Imagem do produto",
                    fontSize = 10.sp, color = MarromRestante, textAlign = TextAlign.Center)
            }
        }
    }
}

// Trabalha com valores de rascunho; somente Aplicar altera a lista e os precos ausentes nao passam no intervalo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PainelFiltrosModeloLoja(catalogo: List<OfertaAbrigo>, categoriaAtual: String, parceiroAtual: String,
                                    minimoAtual: Long?, maximoAtual: Long?, ordemAtual: String, fechar: () -> Unit,
                                    aplicar: (String, String, Long?, Long?, String) -> Unit) {
    var categoria by rememberSaveable { mutableStateOf(categoriaAtual) }
    var parceiro by rememberSaveable { mutableStateOf(parceiroAtual) }
    var minTexto by rememberSaveable { mutableStateOf(minimoAtual?.let { java.math.BigDecimal.valueOf(it, 2).toPlainString().replace('.', ',') } ?: "") }
    var maxTexto by rememberSaveable { mutableStateOf(maximoAtual?.let { java.math.BigDecimal.valueOf(it, 2).toPlainString().replace('.', ',') } ?: "") }
    var ordem by rememberSaveable { mutableStateOf(ordemAtual) }
    var erro by rememberSaveable { mutableStateOf<String?>(null) }
    ModalBottomSheet(onDismissRequest = fechar, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = FundoRestante) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 22.dp).padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Filtrar ofertas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
            SeletorRestante("Categoria", categoria, listOf("Todas" to "Todas") + catalogo.map { it.categoria }.distinct().sorted().map { it to it }) { categoria = it }
            SeletorRestante("Parceiro", parceiro, listOf("Todos" to "Todos") + catalogo.map { it.parceiro }.distinct().sorted().map { it to it }) { parceiro = it }
            CampoRestante("Preço mínimo · opcional", minTexto, { minTexto = it }, dica = "0,00")
            CampoRestante("Preço máximo · opcional", maxTexto, { maxTexto = it }, dica = "100,00")
            SeletorRestante("Ordenar", ordem, listOf("Destaques", "Menor preço", "Maior preço").map { it to it }) { ordem = it }
            erro?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            BotaoRestante("Aplicar filtros") {
                val min = if (minTexto.trim() in listOf("0", "0,0", "0,00")) 0L else centavosAbrigo(minTexto)
                val max = if (maxTexto.trim() in listOf("0", "0,0", "0,00")) 0L else centavosAbrigo(maxTexto)
                erro = when {
                    minTexto.isNotBlank() && min == null -> "Informe um preço mínimo válido, sem ponto de milhar."
                    maxTexto.isNotBlank() && max == null -> "Informe um preço máximo válido, sem ponto de milhar."
                    min != null && max != null && min > max -> "O preço mínimo não pode ser maior que o máximo."
                    else -> null
                }
                if (erro == null) aplicar(categoria, parceiro, min, max, ordem)
            }
            TextButton(onClick = { categoria = "Todas"; parceiro = "Todos"; minTexto = ""; maxTexto = ""; ordem = "Destaques"; erro = null }) { Text("Limpar filtros") }
        }
    }
}

// Previa da composicao baseada no molde enviado.
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewTelaLoja() { MaterialTheme { TelaLoja(contextoPreviewRestante()) } }