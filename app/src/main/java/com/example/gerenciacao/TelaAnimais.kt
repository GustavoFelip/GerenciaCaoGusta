package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Abre e reduz a imagem escolhida na ficha para usá-la como miniatura.
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Representa as informacoes de uma ficha de exemplo, sem depender de banco de dados.
data class AnimalExemplo(
    val nome: String,
    val idade: String,
    val situacao: String,
    val raca: String = "Não informada",
    val sexo: String = "Não informado",
    val castrado: Boolean? = null,
    val fotoUri: String? = null,
    val id: String = java.util.UUID.randomUUID().toString(),
    val sobre: String = "",
    // Guarda o texto informado na edicao e a situacao de arquivamento local.
    val historicoMedico: String = "",
    val arquivado: Boolean = false
) : java.io.Serializable

// Preserva os quatro animais e as situacoes apresentados na referencia enviada.
val AnimaisExemplo = listOf(
    AnimalExemplo("Lua", "2 anos", "Em adoção", raca = "SRD", sexo = "Fêmea", castrado = true, sobre = "Carinhosa, brincalhona e gosta de companhia."),
    AnimalExemplo("Bento", "5 anos", "No abrigo"),
    AnimalExemplo("Mel", "1 ano", "Visita marcada"),
    AnimalExemplo("Tico", "3 anos", "No abrigo")
)

// Define a mesma identidade visual utilizada no inicio do aplicativo.
// Usa o laranja da tela Início somente na faixa superior.
private val LaranjaCabecalhoAnimais = Color(0xFFFFB347)
private val FundoAnimais = Color(0xFFFFFCF7)
private val TextoAnimais = Color(0xFF38291F)
private val MarromAnimais = Color(0xFF783E19)
private val CremeAnimais = Color(0xFFFFEFC5)
private val AmareloAnimais = Color(0xFFFFB414)
private val BordaAnimais = Color(0xFFEADFD2)

// Exibe a lista com pesquisa e filtros locais, mantendo o menu inferior fixo.
@Composable
fun TelaAnimais(aoVoltarInicio: () -> Unit, animais: List<AnimalExemplo>, aoAdicionar: () -> Unit, aoAbrirFicha: (String) -> Unit, aoAgenda: () -> Unit, aoAdocoes: () -> Unit) {
    // Mantem a busca e o filtro ao girar o dispositivo.
    var busca by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf("Todos") }
    val avisos = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    val avisar: (String) -> Unit = { mensagem ->
        escopo.launch {
            avisos.currentSnackbarData?.dismiss()
            avisos.showSnackbar(mensagem)
        }
    }
    BackHandler(onBack = aoVoltarInicio)

    // Combina os dois criterios e calcula o contador com base nos resultados reais.
    val animaisVisiveis = animais.filter { animal ->
        !animal.arquivado && animal.nome.contains(busca.trim(), ignoreCase = true) && when (filtro) {
            "Em adoção" -> animal.situacao == "Em adoção"
            "Adotados" -> animal.situacao == "Adotado"
            else -> true
        }
    }

    Column(Modifier.fillMaxSize().background(FundoAnimais)) {
        MolduraPadraoApp(
            modifier = Modifier.weight(1f),
            containerColor = FundoPadraoApp,
            // Mantém o cabeçalho laranja visível enquanto a lista rola.
            // O espaço da barra de status recebe a mesma cor da faixa superior.

            snackbarHost = { SnackbarHost(avisos) },
            bottomBar = {
                MenuInferiorInicio(avisar, selecionado = "Animais", aoInicio = aoVoltarInicio, aoAnimais = {}, aoAdicionar = aoAdicionar, aoAgenda = aoAgenda)
            }
        ) { espacos ->
            // A lista pode rolar sem esconder os controles de navegacao.
            LazyColumn(
                modifier = Modifier// Arredonda o topo do conteúdo sobre o fundo laranja, igual à tela Início.
                    .fillMaxSize()
                    .padding(espacos)

                    .background(FundoAnimais)
                    .imePadding(),
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Abre o painel de adocoes sem mudar os filtros da lista de bichinhos.
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Bichinhos", modifier = Modifier.weight(1f), fontSize = 24.sp, lineHeight = 29.sp,
                            fontWeight = FontWeight.Bold, color = TextoAnimais)
                        TextButton(onClick = aoAdocoes) {
                            Text("Adoções", color = MarromAnimais, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    // Pesquisa por nome sem distinguir letras maiusculas de minusculas.
                    OutlinedTextField(
                        value = busca, onValueChange = { busca = it }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar pelo nome", fontSize = 14.sp, color = Color(0xFF7E7066)) },
                        leadingIcon = { Icon(painterResource(R.drawable.inicio_busca), "Buscar pelo nome",
                            Modifier.size(20.dp), tint = Color(0xFF7E7066)) },
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmareloAnimais, unfocusedBorderColor = BordaAnimais,
                            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                            focusedTextColor = TextoAnimais, unfocusedTextColor = TextoAnimais,
                            cursorColor = MarromAnimais)
                    )
                    Spacer(Modifier.height(8.dp))

                    // Os filtros mostram apenas as fichas que correspondem a situacao escolhida.
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Todos", "Em adoção", "Adotados").forEach { opcao ->
                            FilterChip(
                                selected = filtro == opcao, onClick = { filtro = opcao },
                                label = { Text(opcao, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold) },
                                shape = CircleShape, border = null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AmareloAnimais, selectedLabelColor = MarromAnimais,
                                    containerColor = if (opcao == "Adotados") Color(0xFFE1F3E8) else CremeAnimais,
                                    labelColor = MarromAnimais)
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))

                    // O total acompanha a busca e os filtros; a demonstracao possui quatro fichas.
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Fichas do abrigo", fontSize = 18.sp, lineHeight = 22.sp,
                            fontWeight = FontWeight.Bold, color = TextoAnimais)
                        val total = animaisVisiveis.size
                        Text("$total ${if (total == 1) "ficha" else "fichas"}", fontSize = 12.sp,
                            lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = MarromAnimais)
                    }
                    Spacer(Modifier.height(16.dp))
                }
                items(animaisVisiveis, key = { it.id }) { animal ->
                    FichaAnimal(animal, aoAbrir = { aoAbrirFicha(animal.id) })
                }
                // Explica o resultado vazio em vez de manter fichas que nao correspondem ao filtro.
                if (animaisVisiveis.isEmpty()) {
                    item {
                        Text("Nenhum bichinho encontrado.", modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            fontSize = 15.sp, color = Color(0xFF7E7066))
                    }
                }
            }
        }
    }

}

// Renderiza a inicial, o nome, a idade e a situacao no cartao arredondado da referencia.
// Mostra a foto salva na ficha e mantém a inicial quando não houver imagem disponível.
@Composable
private fun FichaAnimal(animal: AnimalExemplo, aoAbrir: () -> Unit) {
    // Atualiza a miniatura quando o endereço da foto deste animal mudar.
    val foto = lembrarFotoListaAnimal(animal.fotoUri)

    // Mantém o cartão clicável e o mesmo destino da ficha já configurado.
    Surface(
        onClick = aoAbrir,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BordaAnimais)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 86.dp)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recorta somente a miniatura em círculo, preservando o arquivo original.
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CremeAnimais),
                contentAlignment = Alignment.Center
            ) {
                if (foto != null) {
                    // O nome já é lido no cartão; a imagem é decorativa para o leitor de tela.
                    Image(
                        bitmap = foto,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Exibe a inicial durante o carregamento ou quando não houver foto legível.
                    Text(
                        text = animal.nome.trim().take(1).uppercase().ifBlank { "?" },
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MarromAnimais
                    )
                }
            }

            // Preserva o alinhamento e as informações do cartão existente.
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = animal.nome,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoAnimais
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${animal.idade} · ${animal.situacao}",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFF7E7066)
                )
            }
            Icon(
                painter = painterResource(R.drawable.inicio_seta),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = MarromAnimais
            )
        }
    }
}

// Carrega uma miniatura local fora da interface, sem depender de uma biblioteca de imagens.
@Composable
private fun lembrarFotoListaAnimal(endereco: String?): ImageBitmap? {
    val contexto = LocalContext.current

    // Associa o resultado ao endereço para não mostrar a foto anterior enquanto outra carrega.
    val resultado by produceState<Pair<String?, ImageBitmap?>>(
        initialValue = null to null,
        key1 = endereco,
        key2 = contexto
    ) {
        value = endereco to null
        if (!endereco.isNullOrBlank()) {
            val imagem = withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(endereco)
                    val resolver = contexto.contentResolver

                    // Lê primeiro as dimensões, sem carregar a foto inteira na memória.
                    val limites = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    resolver.openInputStream(uri)?.use { entrada ->
                        BitmapFactory.decodeStream(entrada, null, limites)
                    }

                    if (limites.outWidth <= 0 || limites.outHeight <= 0) {
                        null
                    } else {
                        // Reduz a imagem antes de decodificá-la para o pequeno círculo da lista.
                        val opcoes = BitmapFactory.Options().apply {
                            inSampleSize = 1
                            while (maxOf(limites.outWidth, limites.outHeight) / inSampleSize > 256) {
                                inSampleSize *= 2
                            }
                        }
                        resolver.openInputStream(uri)?.use { entrada ->
                            BitmapFactory.decodeStream(entrada, null, opcoes)?.asImageBitmap()
                        }
                    }
                } catch (_: java.io.IOException) {
                    // Um arquivo removido ou ilegível mantém a inicial no cartão.
                    null
                } catch (_: SecurityException) {
                    // Se o Android retirar o acesso à imagem, mantém a inicial.
                    null
                } catch (_: IllegalArgumentException) {
                    // Um endereço inválido não impede a abertura da lista.
                    null
                }
            }
            value = endereco to imagem
        }
    }

    // Aceita somente o resultado correspondente à foto atual do animal.
    return if (resultado.first == endereco) resultado.second else null
}