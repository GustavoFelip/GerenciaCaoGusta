package com.example.gerenciacao

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Representa as etapas sem depender do texto usado para exibi-las.
enum class EtapaAdocao(val titulo: String) {
    INTERESSE("Pessoa interessada"), ENTREVISTA("Entrevista"),
    VISITA("Visita marcada"), ACOMPANHAMENTO("Acompanhamento")
}

// Liga cada processo ao ID do animal, preservando o vinculo se o nome mudar.
data class ProcessoAdocao(
    val id: String,
    val animalId: String,
    val pessoa: String,
    val etapa: EtapaAdocao,
    val proximaAcao: String
) : java.io.Serializable

// Cria apenas os quatro exemplos do prototipo; novos animais nao ganham processos ficticios.
fun criarProcessosAdocaoExemplo(animais: List<AnimalExemplo>): ArrayList<ProcessoAdocao> {
    val exemplos = listOf(
        Triple("Lua", EtapaAdocao.ENTREVISTA, "Próxima etapa: agendar visita"),
        Triple("Mel", EtapaAdocao.VISITA, "Horário de exemplo: 16h"),
        Triple("Bento", EtapaAdocao.INTERESSE, "Próxima etapa: conversar"),
        Triple("Tico", EtapaAdocao.ACOMPANHAMENTO, "Acompanhar adaptação")
    )
    return ArrayList(exemplos.mapNotNull { (nome, etapa, acao) ->
        animais.firstOrNull { it.nome == nome }?.let { animal ->
            ProcessoAdocao("demo-${animal.id}", animal.id, "Pessoa de exemplo", etapa, acao)
        }
    })
}

// Centraliza a paleta aprovada, sem modificar as cores das outras telas.
private val FundoAdocao = Color(0xFFFFFCF7)
private val TextoAdocao = Color(0xFF38291F)
private val MarromAdocao = Color(0xFF6B3919)
private val LaranjaAdocao = Color(0xFFFFB21C)
private val CremeAdocao = Color(0xFFFFF0C9)
private val VerdeAdocao = Color(0xFFE1F3E8)
private val PessegoAdocao = Color(0xFFFFE8DA)
private val BordaAdocao = Color(0xFFE8DFD4)

// Exibe os processos com busca, filtros, indicadores e navegacao compartilhada.
@Composable
fun TelaAdocoes(
    animais: List<AnimalExemplo>,
    processos: List<ProcessoAdocao>,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit,
    aoAbrirProcesso: (String) -> Unit
) {
    // Conserva busca, filtro e selecao durante a rotacao do aparelho.
    var busca by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todas") }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val animaisPorId = animais.filterNot { it.arquivado }.associateBy { it.id }
    val ativos = processos.filter { it.animalId in animaisPorId }
    val visiveis = ativos.filter { processo ->
        val animal = animaisPorId.getValue(processo.animalId)
        val termo = busca.trim()
        (animal.nome.contains(termo, ignoreCase = true) || processo.pessoa.contains(termo, ignoreCase = true)) &&
                when (filtro) {
                    "Interessados" -> processo.etapa == EtapaAdocao.INTERESSE
                    "Visitas" -> processo.etapa == EtapaAdocao.VISITA
                    else -> true
                }
    }
    BackHandler(onBack = aoVoltar)

    Column(Modifier.fillMaxSize().background(FundoAdocao)) {
        // A cor laranja pertence ao cabecalho compartilhado.
        MolduraPadraoApp(
            modifier = Modifier.weight(1f), containerColor = FundoAdocao,
            bottomBar = {
                MenuInferiorInicio(avisar = { aviso = it }, selecionado = "Animais",
                    aoInicio = aoInicio, aoAnimais = aoAnimais, aoAdicionar = aoAdicionar, aoAgenda = aoAgenda)
            }
        ) { espacos ->
            LazyColumn(Modifier.fillMaxSize().padding(espacos).imePadding(),
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    Column {
                        TituloPaginaPadrao("Adoções", aoVoltar)
                        Text("Acompanhe cada novo começo", fontSize = 13.sp, color = MarromAdocao)
                    }
                    Spacer(Modifier.height(22.dp))
                    // Os indicadores consideram todos os processos ativos, sem depender da busca.
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IndicadorAdocao(ativos.size, "Em andamento", CremeAdocao, Modifier.weight(1f))
                        val visitas = ativos.count { it.etapa == EtapaAdocao.VISITA }
                        IndicadorAdocao(visitas, if (visitas == 1) "Visita marcada" else "Visitas marcadas", VerdeAdocao, Modifier.weight(1f))
                    }
                }
                item {
                    OutlinedTextField(value = busca, onValueChange = { busca = it },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        placeholder = { Text("Buscar bichinho ou pessoa", fontSize = 14.sp) },
                        leadingIcon = { Icon(painterResource(R.drawable.inicio_busca), "Buscar", Modifier.size(22.dp), tint = MarromAdocao) },
                        shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LaranjaAdocao, unfocusedBorderColor = BordaAdocao,
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                            focusedTextColor = TextoAdocao, unfocusedTextColor = TextoAdocao))
                }
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Todas", "Interessados", "Visitas").forEach { opcao ->
                            FilterChip(selected = filtro == opcao, onClick = { filtro = opcao },
                                label = { Text(opcao) }, shape = RoundedCornerShape(24.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CremeAdocao,
                                    selectedLabelColor = MarromAdocao, labelColor = MarromAdocao))
                        }
                    }
                }
                item {
                    Text("Processos em andamento", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextoAdocao)
                }
                items(visiveis, key = { it.id }) { processo ->
                    CartaoProcessoAdocao(animaisPorId.getValue(processo.animalId), processo) { aoAbrirProcesso(processo.id) }
                }
                if (visiveis.isEmpty()) item {
                    Surface(color = CremeAdocao, shape = RoundedCornerShape(18.dp)) {
                        Text(if (ativos.isEmpty()) "Nenhum processo de adoção disponível." else "Nenhum processo encontrado. Tente outro nome ou filtro.",
                            Modifier.fillMaxWidth().padding(20.dp), color = MarromAdocao)
                    }
                }
                item { Text("Os processos usam dados de exemplo.", fontSize = 12.sp, color = MarromAdocao) }
            }
        }
    }

    aviso?.let { mensagem ->
        AlertDialog(onDismissRequest = { aviso = null }, title = { Text("Adoções") },
            text = { Text(mensagem) }, confirmButton = { TextButton(onClick = { aviso = null }) { Text("Fechar") } })
    }
}

// Mostra a quantidade calculada, permitindo crescimento de texto sem altura fixa.
@Composable
private fun IndicadorAdocao(quantidade: Int, titulo: String, cor: Color, modifier: Modifier) {
    Surface(modifier = modifier, color = cor, shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(quantidade.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextoAdocao)
            Text(titulo, fontSize = 13.sp, color = MarromAdocao)
        }
    }
}

// Reutiliza a foto real da ficha ou apresenta a inicial quando ela nao existe.
@Composable
private fun CartaoProcessoAdocao(animal: AnimalExemplo, processo: ProcessoAdocao, aoAbrir: () -> Unit) {
    val cor = when (processo.etapa) {
        EtapaAdocao.VISITA -> VerdeAdocao
        EtapaAdocao.INTERESSE, EtapaAdocao.ENTREVISTA -> PessegoAdocao
        EtapaAdocao.ACOMPANHAMENTO -> CremeAdocao
    }
    val foto = fotoMiniaturaAdocao(animal.fotoUri)
    Surface(onClick = aoAbrir, shape = RoundedCornerShape(18.dp), color = Color.White,
        border = BorderStroke(1.dp, BordaAdocao), shadowElevation = 1.dp) {
        Row(Modifier.fillMaxWidth().heightIn(min = 116.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(5.dp).height(90.dp).background(cor))
            Box(Modifier.padding(start = 12.dp).size(70.dp).clip(RoundedCornerShape(14.dp)).background(CremeAdocao), contentAlignment = Alignment.Center) {
                if (foto != null) Image(foto, "Foto de ${animal.nome}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Text(animal.nome.take(1).uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MarromAdocao)
            }
            Column(Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(animal.nome, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextoAdocao)
                Surface(color = cor, shape = RoundedCornerShape(20.dp)) {
                    Text(processo.etapa.titulo, Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MarromAdocao)
                }
                Text(processo.proximaAcao, fontSize = 12.sp, color = MarromAdocao)
            }
            Icon(painterResource(R.drawable.inicio_seta), null, Modifier.padding(end = 12.dp).size(16.dp), tint = MarromAdocao)
        }
    }
}

// Limita a decodificacao da foto e executa a leitura fora da interface.
@Composable
internal fun fotoMiniaturaAdocao(endereco: String?): ImageBitmap? {
    val contexto = LocalContext.current
    val foto by produceState<ImageBitmap?>(null, endereco) {
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
                contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcoes)?.asImageBitmap() }
            }.getOrNull()
        }
    }
    return foto
}

// Permite visualizar a tela sem iniciar o aplicativo ou conectar banco de dados.
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaAdocoes() {
    MaterialTheme {
        val animais = remember { AnimaisExemplo }
        val processos = remember { criarProcessosAdocaoExemplo(animais) }
        TelaAdocoes(animais, processos, aoVoltar = {}, aoInicio = {}, aoAnimais = {},
            aoAdicionar = {}, aoAgenda = {}, aoAbrirProcesso = {})
    }
}