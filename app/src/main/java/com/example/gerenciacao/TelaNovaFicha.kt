package com.example.gerenciacao

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Compartilha as cores do formulario da referencia sem alterar as demais telas.
private val FundoFicha = Color(0xFFFFFCF7)
private val MarromFicha = Color(0xFF783E19)
private val TextoFicha = Color(0xFF38291F)
private val AmareloFicha = Color(0xFFFFB414)
private val CremeFicha = Color(0xFFFFEFC5)

// Recebe os dados da nova ficha e pede revisao antes de adicionar a demonstracao local.
@Composable
fun TelaNovaFicha(
    // Reutiliza o formulario tambem para editar a ficha selecionada.
    animalInicial: AnimalExemplo? = null,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    // Abre a agenda pelo menu compartilhado.
    aoAgenda: () -> Unit,
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit,
    aoSalvar: (AnimalExemplo) -> Unit
) {
    // Preserva o preenchimento durante rotacoes; a foto e opcional.
    var nome by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.nome ?: "") }
    var raca by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.raca ?: "") }
    var idade by rememberSaveable(animalInicial?.id) { mutableStateOf(animalInicial?.idade ?: "") }
    var sexo by rememberSaveable { mutableStateOf(animalInicial?.sexo?.takeIf { it == "Fêmea" || it == "Macho" } ?: "Fêmea") }
    var castrado by rememberSaveable { mutableStateOf(animalInicial?.castrado ?: true) }
    var fotoUri by rememberSaveable { mutableStateOf(animalInicial?.fotoUri) }
    var tentouContinuar by rememberSaveable { mutableStateOf(false) }
    var revisando by rememberSaveable { mutableStateOf(false) }
    val contexto = LocalContext.current
    val avisos = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    val avisar: (String) -> Unit = { mensagem -> escopo.launch { avisos.showSnackbar(mensagem) } }
    BackHandler(onBack = aoVoltar)

    // Usa o seletor do Android e somente a imagem escolhida pelo usuario.
    val escolherFoto = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { contexto.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            fotoUri = uri.toString()
        }
    }
    // Carrega uma miniatura fora da interface para nao travar ao abrir fotos grandes.
    val miniatura by produceState<ImageBitmap?>(null, fotoUri) {
        value = null
        val uri = fotoUri?.let(Uri::parse)
        if (uri != null) {
            value = withContext(Dispatchers.IO) {
                runCatching {
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
    }

    // Rejeita nomes e racas sem letras e idades que nao representem um numero com unidade.
    val erroNome = if (nome.trim().none { it.isLetter() }) "Informe o nome do bichinho." else null
    val erroRaca = if (raca.trim().none { it.isLetter() }) "Informe a raça ou sem raça definida." else null
    val idadeValida = Regex("^[0-9]{1,2}\\s*(anos?|m[eê]s(?:es)?|dias?)?$", RegexOption.IGNORE_CASE).matches(idade.trim())
    val erroIdade = if (!idadeValida) "Informe uma idade válida. Ex.: 2 anos ou 6 meses." else null

    // Mantem o rodape igual ao das telas de inicio e animais, com o formulario rolavel.
    Column(Modifier.fillMaxSize().background(FundoFicha)) {
        // A cor laranja pertence ao cabecalho compartilhado.
        MolduraPadraoApp(
            interceptarCabecalho = protegerRascunhoNoCabecalho(nome != (animalInicial?.nome ?: "") || raca != (animalInicial?.raca ?: "") ||
                    idade != (animalInicial?.idade ?: "") || fotoUri != animalInicial?.fotoUri ||
                    sexo != (animalInicial?.sexo?.takeIf { it == "Fêmea" || it == "Macho" } ?: "Fêmea") ||
                    castrado != (animalInicial?.castrado ?: true)),
            modifier = Modifier.weight(1f), containerColor = FundoFicha,
            snackbarHost = { SnackbarHost(avisos) },
            bottomBar = {
                MenuInferiorInicio(avisar, selecionado = "Animais", aoInicio = aoInicio,
                    aoAnimais = aoAnimais, aoAdicionar = aoAdicionar, aoAgenda = aoAgenda)
            }
        ) { espacos ->
            Column(
                Modifier.fillMaxSize().padding(espacos).imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp)
            ) {
                // A seta volta para a tela que abriu o formulario.
                // O cabecalho compartilhado agora apresenta o perfil da pessoa.
                TituloPaginaPadrao(if (animalInicial == null) "Nova ficha" else "Editar ficha", aoVoltar)
                Spacer(Modifier.height(24.dp))

                // Abre a galeria e mostra a miniatura quando uma foto foi selecionada.
                Surface(
                    onClick = { escolherFoto.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth(), color = CremeFicha, shape = RoundedCornerShape(22.dp)
                ) {
                    Row(Modifier.heightIn(min = 80.dp).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        val foto = miniatura
                        if (foto != null) {
                            Image(foto, "Foto selecionada do bichinho", Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                        } else {
                            Icon(painterResource(R.drawable.ficha_camera), null, Modifier.size(30.dp), tint = MarromFicha)
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text(if (fotoUri == null) "Adicionar foto do bichinho" else "Alterar foto do bichinho",
                                fontSize = 15.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold, color = MarromFicha)
                            Spacer(Modifier.height(6.dp))
                            Text("Uma foto ajuda na identificação.", fontSize = 10.sp, lineHeight = 14.sp, color = MarromFicha)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))

                // Mostra os erros junto ao campo e mantem a digitacao livre para corrigir os dados.
                CampoNovaFicha("Nome", "Ex.: Lua", nome, { nome = it }, if (tentouContinuar) erroNome else null)
                Spacer(Modifier.height(12.dp))
                CampoNovaFicha("Raça", "Ex.: sem raça definida", raca, { raca = it }, if (tentouContinuar) erroRaca else null)
                Spacer(Modifier.height(12.dp))
                CampoNovaFicha("Idade", "Ex.: 2 anos", idade, { idade = it }, if (tentouContinuar) erroIdade else null)
                Spacer(Modifier.height(18.dp))

                // As opcoes iniciam como no modelo e podem ser alteradas antes de continuar.
                Text("Sexo", color = MarromFicha, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OpcaoFicha("Fêmea", sexo == "Fêmea") { sexo = "Fêmea" }
                    OpcaoFicha("Macho", sexo == "Macho") { sexo = "Macho" }
                }
                Spacer(Modifier.height(12.dp))
                Text("Castrado?", color = MarromFicha, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OpcaoFicha("Sim", castrado) { castrado = true }
                    OpcaoFicha("Não", !castrado) { castrado = false }
                }
                Spacer(Modifier.height(18.dp))

                // Impede o avanco ate que os tres campos obrigatorios estejam validos.
                Button(
                    onClick = {
                        tentouContinuar = true
                        if (erroNome == null && erroRaca == null && erroIdade == null) revisando = true
                    }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmareloFicha, contentColor = TextoFicha)
                ) { Text("Continuar", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    // Permite revisar e confirmar uma ficha temporaria sem alegar persistencia no banco.
    if (revisando) {
        AlertDialog(
            onDismissRequest = { revisando = false }, title = { Text("Revisar ficha", color = TextoFicha) },
            text = { Text("${nome.trim()} · ${idade.trim()}\n${raca.trim()}\n$sexo · Castrado: ${if (castrado) "Sim" else "Não"}\n\nA ficha ficará apenas na demonstração, sem banco de dados.") },
            confirmButton = {
                TextButton(onClick = {
                    revisando = false
                    val idadeFormatada = idade.trim().toIntOrNull()?.let { "$it ${if (it == 1) "ano" else "anos"}" } ?: idade.trim()
                    // Preserva ID, situacao e descricao ao editar o animal existente.
                    val ficha = animalInicial?.copy(nome = nome.trim(), idade = idadeFormatada,
                        raca = raca.trim(), sexo = sexo, castrado = castrado, fotoUri = fotoUri)
                        ?: AnimalExemplo(nome.trim(), idadeFormatada, "No abrigo", raca.trim(), sexo, castrado, fotoUri)
                    aoSalvar(ficha)
                }) { Text(if (animalInicial == null) "Adicionar à lista" else "Salvar alterações", color = MarromFicha) }
            },
            dismissButton = { TextButton(onClick = { revisando = false }) { Text("Revisar", color = MarromFicha) } },
            containerColor = FundoFicha
        )
    }
}

// Exibe um campo com rotulo externo e mensagem de validacao acessivel.
@Composable
private fun CampoNovaFicha(titulo: String, exemplo: String, valor: String, aoAlterar: (String) -> Unit, erro: String?) {
    Text(titulo, color = MarromFicha, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = valor, onValueChange = aoAlterar, modifier = Modifier.fillMaxWidth().height(50.dp),
        singleLine = true, placeholder = { Text(exemplo, fontSize = 14.sp, lineHeight = 18.sp, color = Color(0xFF7E7066)) },
        textStyle = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, color = TextoFicha),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        isError = erro != null,

        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AmareloFicha, unfocusedBorderColor = Color(0xFFEADFD2),
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, cursorColor = MarromFicha)
    )
    // Mantem o erro fora da altura do campo para que a mensagem nao seja cortada.
    if (erro != null) {
        Text(erro, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
    }
}

// Destaca a opcao selecionada e disponibiliza a semantica de selecao do Compose.
@Composable
private fun OpcaoFicha(texto: String, selecionada: Boolean, aoSelecionar: () -> Unit) {
    FilterChip(
        selected = selecionada, onClick = aoSelecionar, modifier = Modifier.width(104.dp),
        label = { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text(texto, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold)
        } },
        shape = CircleShape, border = null,
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CremeFicha,
            selectedLabelColor = MarromFicha, containerColor = Color(0xFFFFF7EB), labelColor = MarromFicha)
    )
}