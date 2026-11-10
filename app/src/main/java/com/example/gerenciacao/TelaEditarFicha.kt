package com.example.gerenciacao

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Mantem as cores da home e dos prototipos aprovados.
private val FundoEditar = Color(0xFFFFFCF7)
private val TextoEditar = Color(0xFF38291F)
private val MarromEditar = Color(0xFF6B3919)
private val LaranjaEditar = Color(0xFFFFB21C)
private val CremeEditar = Color(0xFFFFF0C9)
private val VerdeEditar = Color(0xFFE1F3E8)
private val PessegoEditar = Color(0xFFFFE8DA)
private val BordaEditar = Color(0xFFE8DFD4)

// Edita qualquer bichinho recebido, preservando seu identificador e sua situacao.
@Composable
fun TelaEditarFicha(
    animal: AnimalExemplo,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit,
    aoSalvar: (AnimalExemplo) -> Unit,
    aoArquivar: (String) -> Unit
) {
    // Trabalha em um rascunho: os dados originais so mudam ao salvar.
    var nome by rememberSaveable(animal.id) { mutableStateOf(animal.nome) }
    var raca by rememberSaveable(animal.id) { mutableStateOf(animal.raca) }
    var idade by rememberSaveable(animal.id) { mutableStateOf(animal.idade) }
    var sexo by rememberSaveable(animal.id) { mutableStateOf(animal.sexo) }
    var castrado by rememberSaveable(animal.id) { mutableStateOf(animal.castrado) }
    var fotoUri by rememberSaveable(animal.id) { mutableStateOf(animal.fotoUri) }
    var descricao by rememberSaveable(animal.id) { mutableStateOf(animal.sobre) }
    var historico by rememberSaveable(animal.id) { mutableStateOf(animal.historicoMedico) }
    var tentouSalvar by rememberSaveable(animal.id) { mutableStateOf(false) }
    var confirmarArquivo by rememberSaveable(animal.id) { mutableStateOf(false) }
    var confirmarSaida by rememberSaveable(animal.id) { mutableStateOf(false) }
    var destinoSaida by rememberSaveable(animal.id) { mutableStateOf("voltar") }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val contexto = LocalContext.current

    // Evita perder alteracoes por toque acidental na seta ou no menu inferior.
    val alterado = nome != animal.nome || raca != animal.raca || idade != animal.idade ||
            sexo != animal.sexo || castrado != animal.castrado || fotoUri != animal.fotoUri ||
            descricao != animal.sobre || historico != animal.historicoMedico
    val abrirFinancas = LocalAbrirFinancas.current
    val abrirLoja = LocalAbrirLoja.current
    val navegar: (String) -> Unit = { destino ->
        when (destino) {
            "inicio" -> aoInicio()
            "animais" -> aoAnimais()
            "adicionar" -> aoAdicionar()
            "agenda" -> aoAgenda()
            "financas" -> abrirFinancas()
            "loja" -> abrirLoja()
            else -> aoVoltar()
        }
    }
    val pedirSaida: (String) -> Unit = { destino ->
        if (alterado) {
            destinoSaida = destino
            confirmarSaida = true
        } else navegar(destino)
    }
    BackHandler { pedirSaida("voltar") }

    // Recebe a imagem escolhida no seletor do Android, sem pedir acesso amplo a galeria.
    val seletorFoto = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { contexto.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            fotoUri = uri.toString()
        }
    }
    val foto = lembrarFotoEditarFicha(fotoUri)
    val erroNome = if (nome.trim().none { it.isLetter() }) "Informe o nome do bichinho." else null
    val erroRaca = if (raca.trim().none { it.isLetter() }) "Informe a raça ou sem raça definida." else null
    val erroIdade = if (!Regex("^[0-9]{1,2}\\s*(anos?|m[eê]s(?:es)?|dias?)?$", RegexOption.IGNORE_CASE)
            .matches(idade.trim())) "Use uma idade válida, como 2 anos ou 6 meses." else null

    // Compartilha o menu normal, mas protege o rascunho ao abrir o financeiro.
    CompositionLocalProvider(
        LocalAbrirFinancas provides { pedirSaida("financas") },
        LocalAbrirLoja provides { pedirSaida("loja") }
    ) {
        Column(Modifier.fillMaxSize().background(FundoEditar)) {
            // A cor laranja pertence ao cabecalho compartilhado.
            MolduraPadraoApp(
                interceptarCabecalho = protegerRascunhoNoCabecalho(alterado),
                modifier = Modifier.weight(1f), containerColor = FundoEditar,
                bottomBar = {
                    MenuInferiorInicio(avisar = { aviso = it }, selecionado = "Animais",
                        aoInicio = { pedirSaida("inicio") }, aoAnimais = { pedirSaida("animais") },
                        aoAdicionar = { pedirSaida("adicionar") }, aoAgenda = { pedirSaida("agenda") })
                }
            ) { espacos ->
                Column(Modifier.fillMaxSize().padding(espacos).imePadding()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)
                    .padding(top = 22.dp, bottom = 28.dp)) {
                    // O titulo descreve a edicao, sem repetir a logo no cabecalho.
                    TituloPaginaPadrao("Editar ficha", { pedirSaida("voltar") })
                    Spacer(Modifier.height(18.dp))

                    // Identifica o animal original enquanto o nome do rascunho e editado.
                    Surface(color = CremeEditar, shape = RoundedCornerShape(20.dp)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(56.dp).clip(CircleShape).background(PessegoEditar), contentAlignment = Alignment.Center) {
                                if (foto != null) Image(foto, "Foto de ${animal.nome}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                else Text(animal.nome.take(1).uppercase(), fontSize = 28.sp, color = MarromEditar, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(animal.nome, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = TextoEditar)
                                Text("Ficha do bichinho", fontSize = 12.sp, color = MarromEditar)
                            }
                            IconButton(onClick = { seletorFoto.launch(arrayOf("image/*")) }) {
                                Icon(painterResource(R.drawable.ficha_camera), "Alterar foto", tint = MarromEditar)
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))

                    // Separa os dados basicos em uma superficie com campos rotulados.
                    BlocoEditarFicha {
                        CampoEditarFicha("Nome", nome, { nome = it }, if (tentouSalvar) erroNome else null)
                        CampoEditarFicha("Raça", raca, { raca = it }, if (tentouSalvar) erroRaca else null)
                        CampoEditarFicha("Idade", idade, { idade = it }, if (tentouSalvar) erroIdade else null)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SeletorEditarFicha("Sexo", sexo, listOf("Fêmea", "Macho", "Não informado"), Modifier.weight(1f)) { sexo = it }
                            SeletorEditarFicha("Castrado?", when (castrado) { true -> "Sim"; false -> "Não"; null -> "Não informado" },
                                listOf("Sim", "Não", "Não informado"), Modifier.weight(1f)) {
                                castrado = when (it) { "Sim" -> true; "Não" -> false; else -> null }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // Descricao e historico sao texto livre e opcionais, sem diagnosticos automaticos.
                    BlocoEditarFicha(VerdeEditar) {
                        Text("Sobre o bichinho", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoEditar)
                        CampoEditarFicha("Descrição", descricao, { descricao = it }, multiline = true,
                            exemplo = "Conte sobre o jeito e a história do bichinho.")
                    }
                    Spacer(Modifier.height(16.dp))
                    BlocoEditarFicha(PessegoEditar) {
                        Text("Saúde", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoEditar)
                        CampoEditarFicha("Histórico médico", historico, { historico = it }, multiline = true,
                            exemplo = "Registre cuidados e informações fornecidas pelo veterinário.")
                    }
                    Spacer(Modifier.height(22.dp))

                    // Atualiza apenas esta ficha e preserva ID, situacao e arquivamento.
                    Button(onClick = {
                        tentouSalvar = true
                        if (erroNome == null && erroRaca == null && erroIdade == null) {
                            val idadeFinal = idade.trim().toIntOrNull()?.let { "$it ${if (it == 1) "ano" else "anos"}" } ?: idade.trim()
                            aoSalvar(animal.copy(nome = nome.trim(), raca = raca.trim(), idade = idadeFinal,
                                sexo = sexo, castrado = castrado, fotoUri = fotoUri,
                                sobre = descricao.trim(), historicoMedico = historico.trim()))
                        }
                    }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LaranjaEditar, contentColor = TextoEditar)) {
                        Text("Salvar alterações", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { confirmarArquivo = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Arquivar ficha", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MarromEditar)
                    }
                    Text("A ficha arquivada sai da lista ativa.", Modifier.fillMaxWidth(),
                        fontSize = 12.sp, color = Color(0xFF807165), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }

    // Exige confirmacao antes de ocultar a ficha da lista ativa, sem excluir seus dados.
    if (confirmarArquivo) AlertDialog(onDismissRequest = { confirmarArquivo = false },
        title = { Text("Arquivar ${animal.nome}?") },
        text = { Text("A ficha sairá da lista ativa, mas os dados serão mantidos. Alterações não salvas neste formulário serão descartadas.") },
        confirmButton = { TextButton(onClick = { confirmarArquivo = false; aoArquivar(animal.id) }) { Text("Arquivar", color = MarromEditar) } },
        dismissButton = { TextButton(onClick = { confirmarArquivo = false }) { Text("Cancelar", color = MarromEditar) } }, containerColor = FundoEditar)
    if (confirmarSaida) AlertDialog(onDismissRequest = { confirmarSaida = false },
        title = { Text("Descartar alterações?") }, text = { Text("Você tem alterações que ainda não foram salvas.") },
        confirmButton = { TextButton(onClick = { confirmarSaida = false; if (destinoSaida == "financas") abrirFinancas() else navegar(destinoSaida) }) { Text("Descartar e sair", color = MarromEditar) } },
        dismissButton = { TextButton(onClick = { confirmarSaida = false }) { Text("Continuar editando", color = MarromEditar) } }, containerColor = FundoEditar)
    aviso?.let { texto -> AlertDialog(onDismissRequest = { aviso = null }, title = { Text("Editar ficha") },
        text = { Text(texto) }, confirmButton = { TextButton(onClick = { aviso = null }) { Text("Fechar") } }) }
}

// Reserva uma superficie consistente para cada grupo do formulario.
@Composable
private fun BlocoEditarFicha(cor: Color = Color.White, conteudo: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = cor, border = BorderStroke(1.dp, BordaEditar)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = conteudo)
    }
}

// Oferece campos multiline para descricao e historico, sem cortar erros de validacao.
@Composable
private fun CampoEditarFicha(titulo: String, valor: String, aoAlterar: (String) -> Unit,
                             erro: String? = null, multiline: Boolean = false, exemplo: String = "") {
    OutlinedTextField(value = valor, onValueChange = aoAlterar, modifier = Modifier.fillMaxWidth(),
        label = { Text(titulo) }, placeholder = { Text(exemplo, fontSize = 13.sp) },
        singleLine = !multiline, minLines = if (multiline) 3 else 1,
        isError = erro != null, supportingText = if (erro != null) { { Text(erro) } } else null,
        shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = LaranjaEditar, unfocusedBorderColor = BordaEditar,
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            focusedTextColor = TextoEditar, unfocusedTextColor = TextoEditar, cursorColor = MarromEditar))
}

// Permite escolher sexo e castracao sem converter valores desconhecidos automaticamente.
@Composable
private fun SeletorEditarFicha(titulo: String, valor: String, opcoes: List<String>, modifier: Modifier, aoSelecionar: (String) -> Unit) {
    var aberto by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MarromEditar)
        Spacer(Modifier.height(6.dp))
        Box {
            OutlinedButton(onClick = { aberto = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                contentPadding = PaddingValues(horizontal = 10.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BordaEditar)) {
                Text(valor, Modifier.weight(1f), color = TextoEditar, fontSize = 13.sp)
                Text("⌄", color = MarromEditar)
            }
            DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                opcoes.forEach { opcao -> DropdownMenuItem(text = { Text(opcao) }, onClick = { aoSelecionar(opcao); aberto = false }) }
            }
        }
    }
}

// Decodifica uma miniatura com limite de tamanho fora da interface.
@Composable
private fun lembrarFotoEditarFicha(endereco: String?): ImageBitmap? {
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

// Exibe o formulario completo no Preview com um animal demonstrativo.
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewEditarFicha() {
    MaterialTheme {
        TelaEditarFicha(animal = AnimalExemplo("Lua", "2 anos", "Em adoção", raca = "SRD", sexo = "Fêmea", castrado = true,
            sobre = "Carinhosa, brincalhona e gosta de companhia.", historicoMedico = "Vacinação em dia. Acompanhamento veterinário registrado."),
            aoVoltar = {}, aoInicio = {}, aoAnimais = {}, aoAdicionar = {}, aoAgenda = {}, aoSalvar = {}, aoArquivar = {})
    }
}