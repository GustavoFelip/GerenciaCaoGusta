package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Usa o laranja do novo inicio e deixa o fundo creme para os resultados.
private val LaranjaCabecalhoRedes = Color(0xFFFFAE49)

// A lista Todos e a lista Suas conexoes sao dois estados da mesma tela, nao telas duplicadas.
@Composable
fun TelaRedesConexoes(ctx: ContextoAbrigo, abrigos: List<AbrigoRede>, conexoes: Set<String>, aoAbrirAbrigo: (String) -> Unit) {
    var busca by rememberSaveable { mutableStateOf("") }
    var aba by rememberSaveable { mutableStateOf("Todos") }
    var cidade by rememberSaveable { mutableStateOf("Todas") }
    var raio by rememberSaveable { mutableIntStateOf(0) }
    var vagas by rememberSaveable { mutableStateOf(false) }
    var ordem by rememberSaveable { mutableStateOf("Distância") }
    var filtrosAbertos by rememberSaveable { mutableStateOf(false) }
    val quantidadeFiltros = (if (cidade != "Todas") 1 else 0) + (if (raio > 0) 1 else 0) + (if (vagas) 1 else 0)
    val resultados = filtrarRedes(abrigos, conexoes, aba == "Suas conexões", busca, cidade, raio, vagas, ordem)
    EstruturaRedes(ctx, "Redes e Conexões") {
        // A lista e virtualizada para nao montar todos os cartoes ao mesmo tempo.
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                TituloPaginaPadrao("Redes e Conexões", ctx.voltar)
                Spacer(Modifier.height(12.dp))
                Text("Encontre quem também acolhe", color = TextoRestante, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("Conheça outros abrigos e organize suas conexões.", fontSize = 13.sp, color = MarromRestante)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = busca, onValueChange = { busca = it.take(120) }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, label = { Text("Buscar por nome ou cidade", fontSize = 13.sp) },
                    leadingIcon = { Icon(painterResource(R.drawable.inicio_busca), null, Modifier.size(20.dp), tint = MarromRestante) },
                    trailingIcon = {
                        IconButton(onClick = { filtrosAbertos = true }, modifier = Modifier.semantics { contentDescription = "Abrir filtros de redes" }) {
                            IconeFiltroRedes()
                        }
                    }, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LaranjaRestante, unfocusedBorderColor = BordaRestante,
                        focusedContainerColor = Color.White, unfocusedContainerColor = Color.White))
                Spacer(Modifier.height(10.dp))
                FiltrosRestantes(listOf("Todos", "Suas conexões"), aba) { aba = it }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${resultados.size} ${if (resultados.size == 1) "abrigo encontrado" else "abrigos encontrados"}",
                        Modifier.weight(1f), fontSize = 12.sp, color = MarromRestante)
                    TextButton(onClick = { filtrosAbertos = true }) {
                        Text(if (quantidadeFiltros == 0) "Filtrar" else "Filtrar ($quantidadeFiltros)", color = MarromRestante)
                    }
                }
                Text("Dados de exemplo · distâncias ilustrativas", fontSize = 11.sp, color = MarromRestante)
            }
            items(resultados, key = { it.id }) { abrigo ->
                CartaoAbrigoRede(abrigo, abrigo.id in conexoes) { aoAbrirAbrigo(abrigo.id) }
            }
            if (resultados.isEmpty()) item {
                CartaoRestante(CremeRestante) {
                    Text(if (aba == "Suas conexões" && conexoes.isEmpty()) "Você ainda não adicionou uma conexão." else "Nenhum abrigo corresponde à busca e aos filtros.", fontWeight = FontWeight.SemiBold)
                    Text(if (aba == "Suas conexões" && conexoes.isEmpty()) "Abra Todos, escolha um abrigo e toque em Conectar-se." else "Tente outro nome ou limpe os filtros.")
                    TextButton(onClick = {
                        busca = ""; cidade = "Todas"; raio = 0; vagas = false
                        if (conexoes.isEmpty()) aba = "Todos"
                    }) { Text("Limpar busca e filtros", color = MarromRestante) }
                }
            }
            item { Text("Esta demonstração não consulta a localização do aparelho.", fontSize = 11.sp, color = MarromRestante) }
        }
    }
    // O painel trabalha em um rascunho; cancelar nao muda os filtros aplicados.
    if (filtrosAbertos) FiltrosConexoesRede(cidade, raio, vagas, ordem, abrigos.map { it.cidade }.distinct().sorted(),
        aoFechar = { filtrosAbertos = false },
        aoAplicar = { novaCidade, novoRaio, novasVagas, novaOrdem ->
            cidade = novaCidade; raio = novoRaio; vagas = novasVagas; ordem = novaOrdem; filtrosAbertos = false
        })
}

// Compartilha o cabecalho e o menu inferior das duas telas de redes.
@Composable
internal fun EstruturaRedes(ctx: ContextoAbrigo, titulo: String, conteudo: @Composable () -> Unit) {
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(onBack = ctx.voltar)
    CompositionLocalProvider(LocalAbrirFinancas provides { ctx.principal("financas") }) {
        MolduraPadraoApp(
            bottomBar = { MenuInferiorInicio(avisar = { aviso = it }, selecionado = "",
                aoInicio = { ctx.principal("inicio") }, aoAnimais = { ctx.principal("animais") },
                aoAdicionar = { ctx.principal("adicionar") }, aoAgenda = { ctx.principal("agenda") }) }
        ) { conteudo() }
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Mostra um cartao clicavel por abrigo e identifica as conexoes ja adicionadas.
@Composable
private fun CartaoAbrigoRede(abrigo: AbrigoRede, conectado: Boolean, abrir: () -> Unit) {
    Surface(onClick = abrir, color = Color.White, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, BordaRestante)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CapaAbrigoRede(abrigo, Modifier.size(58.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(abrigo.nome, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
                Text("${abrigo.bairro} · ${abrigo.cidade}", fontSize = 11.sp, lineHeight = 15.sp, color = MarromRestante)
                Text(distanciaRede(abrigo), fontSize = 11.sp, color = MarromRestante)
                if (conectado) Text("Sua conexão", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2F7051))
            }
            Spacer(Modifier.width(6.dp))
            Icon(painterResource(R.drawable.inicio_seta), null, Modifier.size(15.dp), tint = MarromRestante)
        }
    }
}

// Usa a foto opcional do abrigo; sem foto, mostra uma capa de patinha da identidade existente.
@Composable
internal fun CapaAbrigoRede(abrigo: AbrigoRede, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(15.dp)).background(CremeRestante), contentAlignment = Alignment.Center) {
        val foto = abrigo.fotoRecurso
        if (foto != null) Image(painterResource(foto), "Foto de ${abrigo.nome}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(painterResource(R.drawable.inicio_pata), null, Modifier.fillMaxSize().padding(14.dp), tint = MarromRestante)
    }
}

// Desenha o controle de filtros sem depender de uma nova biblioteca de icones.
@Composable
private fun IconeFiltroRedes() {
    Canvas(Modifier.size(22.dp)) {
        listOf(0.25f to 0.35f, 0.5f to 0.7f, 0.75f to 0.45f).forEach { (y, x) ->
            drawLine(MarromRestante, Offset(0f, size.height * y), Offset(size.width, size.height * y), strokeWidth = 1.6.dp.toPx())
            drawCircle(FundoRestante, 3.dp.toPx(), Offset(size.width * x, size.height * y))
            drawCircle(MarromRestante, 2.dp.toPx(), Offset(size.width * x, size.height * y))
        }
    }
}

// Aplica cidade, distancia de exemplo, vagas e ordenacao apenas ao confirmar o painel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltrosConexoesRede(cidadeAtual: String, raioAtual: Int, vagasAtuais: Boolean, ordemAtual: String,
                                cidades: List<String>, aoFechar: () -> Unit, aoAplicar: (String, Int, Boolean, String) -> Unit) {
    var cidade by rememberSaveable { mutableStateOf(cidadeAtual) }
    var raio by rememberSaveable { mutableIntStateOf(raioAtual) }
    var vagas by rememberSaveable { mutableStateOf(vagasAtuais) }
    var ordem by rememberSaveable { mutableStateOf(ordemAtual) }
    ModalBottomSheet(onDismissRequest = aoFechar, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = FundoRestante) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Filtrar redes", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
            SeletorRestante("Cidade", cidade, listOf("Todas" to "Todas") + cidades.map { it to it }) { cidade = it }
            SeletorRestante("Distância ilustrativa", if (raio == 0) "Qualquer distância" else "Até $raio km",
                listOf("0" to "Qualquer distância", "5" to "Até 5 km", "10" to "Até 10 km", "25" to "Até 25 km")) { raio = it.toInt() }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Somente abrigos com vagas", Modifier.weight(1f), fontSize = 14.sp, color = TextoRestante)
                Switch(checked = vagas, onCheckedChange = { vagas = it })
            }
            SeletorRestante("Ordenar por", ordem, listOf("Distância" to "Menor distância de exemplo", "Nome" to "Nome")) { ordem = it }
            Text("As distâncias são exemplos. Não há consulta de GPS nesta etapa.", fontSize = 12.sp, color = MarromRestante)
            BotaoRestante("Aplicar filtros") { aoAplicar(cidade, raio, vagas, ordem) }
            TextButton(onClick = { cidade = "Todas"; raio = 0; vagas = false; ordem = "Distância" }) { Text("Limpar filtros", color = MarromRestante) }
        }
    }
}

// Mostra a primeira tela do fluxo com uma conexao previamente marcada no exemplo.
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewRedesConexoes() {
    MaterialTheme { TelaRedesConexoes(contextoPreviewRestante(), AbrigosRedeExemplo, setOf("cantinho-focinhos"), {}) }
}