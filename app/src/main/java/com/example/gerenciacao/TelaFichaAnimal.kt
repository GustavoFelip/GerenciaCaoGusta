package com.example.gerenciacao

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Apresenta os dados do animal recebido; a mesma tela atende qualquer ID da lista.
@Composable
fun TelaFichaAnimal(
    animal: AnimalExemplo,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    // Abre a agenda pelo menu compartilhado.
    aoAgenda: () -> Unit,
    aoAdicionar: () -> Unit,
    aoEditar: () -> Unit,
    aoAcompanharAdocao: () -> Unit
) {
    // Usa a identidade visual compartilhada pelas telas de animais e cadastro.
    val fundo = Color(0xFFFFFCF7)
    val marrom = Color(0xFF783E19)
    val texto = Color(0xFF38291F)
    val creme = Color(0xFFFFEFC5)
    val amarelo = Color(0xFFFFB414)
    // Mantem a abertura do historico durante a rotacao do dispositivo.
    var mostrarHistorico by rememberSaveable(animal.id) { mutableStateOf(false) }
    val avisos = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    val avisar: (String) -> Unit = { mensagem ->
        escopo.launch {
            avisos.currentSnackbarData?.dismiss()
            avisos.showSnackbar(mensagem)
        }
    }
    BackHandler(onBack = aoVoltar)

    // Abre somente a foto vinculada a esta ficha e reduz imagens grandes antes de exibir.
    val contexto = LocalContext.current
    val foto by produceState<ImageBitmap?>(null, animal.id, animal.fotoUri) {
        value = null
        animal.fotoUri?.let { endereco ->
            value = withContext(Dispatchers.IO) {
                runCatching {
                    val uri = Uri.parse(endereco)
                    val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
                    val opcoes = BitmapFactory.Options().apply {
                        inSampleSize = 1
                        while (maxOf(limites.outWidth, limites.outHeight) / inSampleSize > 1024) inSampleSize *= 2
                    }
                    contexto.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, opcoes)?.asImageBitmap()
                    }
                }.getOrNull()
            }
        }
    }

    // Mantem o menu fixo e oferece rolagem para as fichas com textos maiores.
    Column(Modifier.fillMaxSize().background(fundo)) {
        // A cor laranja pertence ao cabecalho compartilhado.
        MolduraPadraoApp(
            modifier = Modifier.weight(1f), containerColor = fundo,
            snackbarHost = { SnackbarHost(avisos) },
            bottomBar = { MenuInferiorInicio(avisar, selecionado = "Animais", aoInicio = aoInicio,
                aoAnimais = aoVoltar, aoAdicionar = aoAdicionar, aoAgenda = aoAgenda) }
        ) { espacos ->
            Column(Modifier.fillMaxSize().padding(espacos).verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp)) {
                // Volta a lista preservando a busca e o filtro usados para abrir o animal.
                // O cabecalho compartilhado agora apresenta o perfil da pessoa.
                TituloPaginaPadrao("Ficha do bichinho", aoVoltar)
                Spacer(Modifier.height(18.dp))

                // Usa a foto do cadastro ou a marca original quando a ficha nao tem imagem.
                Surface(color = creme, shape = RoundedCornerShape(24.dp)) {
                    Box(Modifier.fillMaxWidth().height(158.dp), contentAlignment = Alignment.Center) {
                        val imagem = foto
                        if (imagem != null) {
                            Image(imagem, "Foto de ${animal.nome}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Image(painterResource(R.drawable.logolaranja), "Imagem de ${animal.nome}",
                                Modifier.size(100.dp), contentScale = ContentScale.Fit)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))

                // O nome e a situacao sao lidos da ficha selecionada, sem valores fixos da Lua.
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(animal.nome, modifier = Modifier.weight(1f), fontSize = 28.sp,
                        lineHeight = 32.sp, fontWeight = FontWeight.Bold, color = texto)
                    Box(Modifier.background(if (animal.situacao == "Em adoção") Color(0xFFE1F3E8) else creme, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 7.dp)) {
                        Text(animal.situacao, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = marrom)
                    }
                }
                Spacer(Modifier.height(10.dp))
                val castracao = when (animal.castrado) {
                    true -> if (animal.sexo == "Fêmea") "Castrada" else "Castrado"
                    false -> if (animal.sexo == "Fêmea") "Não castrada" else "Não castrado"
                    null -> "Castração não informada"
                }
                Text("${animal.idade} · ${animal.raca} · ${animal.sexo} · $castracao",
                    fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFF7E7066))
                Spacer(Modifier.height(20.dp))

                // Mostra apenas a descricao cadastrada; fichas sem descricao usam uma mensagem neutra.
                val tituloSobre = when (animal.sexo) {
                    "Fêmea" -> "Sobre a ${animal.nome}"
                    "Macho" -> "Sobre o ${animal.nome}"
                    else -> "Sobre ${animal.nome}"
                }
                Text(tituloSobre, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, color = texto)
                Spacer(Modifier.height(12.dp))
                Text(animal.sobre.ifBlank { "Sem descrição cadastrada." }, fontSize = 14.sp, lineHeight = 20.sp, color = texto)
                Spacer(Modifier.height(24.dp))
                Text("Saúde e cuidados", fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, color = texto)
                Spacer(Modifier.height(14.dp))

                // Abre o historico informado nesta ficha, com estado vazio quando necessario.
                Surface(onClick = { mostrarHistorico = true },
                    modifier = Modifier.fillMaxWidth(), color = Color.White, shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFEADFD2))) {
                    Row(Modifier.heightIn(min = 76.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).background(Color(0xFFFFEFE2), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.inicio_coracao), null, Modifier.size(23.dp), tint = marrom)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Histórico de saúde", fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, color = texto)
                            Spacer(Modifier.height(5.dp))
                            Text("Vacinas, consultas e tratamentos", fontSize = 11.sp, lineHeight = 15.sp, color = Color(0xFF7E7066))
                        }
                        Icon(painterResource(R.drawable.inicio_seta), null, Modifier.size(15.dp), tint = marrom)
                    }
                }
                Spacer(Modifier.height(20.dp))
                // Abre o processo vinculado ao ID deste animal.
                Button(onClick = aoAcompanharAdocao,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = amarelo, contentColor = texto)) {
                    Text("Acompanhar adoção", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                // Edita o mesmo registro pelo formulario existente, preservando seu ID.
                Button(onClick = aoEditar, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = marrom)) {
                    Text("Editar ficha", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Mostra somente o texto cadastrado, sem preencher informacoes clinicas ficticias.
    if (mostrarHistorico) AlertDialog(
        onDismissRequest = { mostrarHistorico = false },
        title = { Text("Histórico de ${animal.nome}") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(animal.historicoMedico.ifBlank { "Nenhum histórico médico cadastrado." })
            }
        },
        confirmButton = {
            TextButton(onClick = { mostrarHistorico = false }) { Text("Fechar", color = marrom) }
        },
        containerColor = fundo
    )
}