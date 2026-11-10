package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Compartilha dados e acoes; nenhuma tela guarda callbacks dentro do estado salvo.
class ContextoAbrigo(val dados: DadosRestantes, val animais: List<AnimalExemplo>,
                     val atualizar: (DadosRestantes) -> Unit, val abrir: (String) -> Unit,
                     val voltar: () -> Unit, val principal: (String) -> Unit, val verAnimal: (String) -> Unit)

// Mantem cores comuns para todas as telas restantes.
internal val FundoRestante = Color(0xFFFFFCF7)
internal val TextoRestante = Color(0xFF38291F)
internal val MarromRestante = Color(0xFF6B3919)
internal val LaranjaRestante = Color(0xFFFFB21C)
internal val CremeRestante = Color(0xFFFFF0C9)
internal val VerdeRestante = Color(0xFFE1F3E8)
internal val PessegoRestante = Color(0xFFFFE8DA)
internal val BordaRestante = Color(0xFFE8DFD4)

// Oferece rolagem, menu fixo e confirmacao de descarte para formularios alterados.
@Composable
fun BaseRestante(ctx: ContextoAbrigo, titulo: String, aba: String = "", alterado: Boolean = false,
                 conteudo: @Composable ColumnScope.() -> Unit) {
    var destino by rememberSaveable { mutableStateOf<String?>(null) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val ir: (String) -> Unit = { if (it == "voltar") ctx.voltar() else ctx.principal(it) }
    val pedir: (String) -> Unit = { if (alterado) destino = it else ir(it) }
    BackHandler { pedir("voltar") }
    CompositionLocalProvider(
        LocalAbrirFinancas provides { pedir("financas") },
        LocalAbrirLoja provides { pedir("loja") }
    ) {
        Column(Modifier.fillMaxSize().background(FundoRestante)) {
            // A cor laranja pertence ao cabecalho compartilhado.
            MolduraPadraoApp(
                interceptarCabecalho = protegerRascunhoNoCabecalho(alterado),modifier = Modifier.weight(1f), containerColor = FundoRestante,
                bottomBar = { MenuInferiorInicio(avisar = { aviso = it }, selecionado = aba,
                    aoInicio = { pedir("inicio") }, aoAnimais = { pedir("animais") },
                    aoAdicionar = { pedir("adicionar") }, aoAgenda = { pedir("agenda") }) }
            ) { espacos ->
                Column(Modifier.fillMaxSize().padding(espacos).imePadding().verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TituloPaginaPadrao(titulo, { pedir("voltar") })
                    conteudo()
                }
            }
        }
    }
    if (destino != null) AlertDialog(onDismissRequest = { destino = null }, title = { Text("Descartar alterações?") },
        text = { Text("As alterações deste formulário ainda não foram salvas.") },
        confirmButton = { TextButton(onClick = { val alvo = destino; destino = null; alvo?.let(ir) }) { Text("Descartar e sair") } },
        dismissButton = { TextButton(onClick = { destino = null }) { Text("Continuar editando") } })
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Padroniza grupos de informacoes e permite variar apenas o fundo.
@Composable
fun CartaoRestante(cor: Color = Color.White, conteudo: @Composable ColumnScope.() -> Unit) {
    Surface(color = cor, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, BordaRestante)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = conteudo)
    }
}

// Rotula campos e limita entradas longas para manter o estado local pequeno.
@Composable
fun CampoRestante(rotulo: String, valor: String, mudar: (String) -> Unit, longo: Boolean = false, dica: String = "") {
    OutlinedTextField(value = valor, onValueChange = { mudar(it.take(if (longo) 2000 else 160)) },
        modifier = Modifier.fillMaxWidth(), singleLine = !longo, minLines = if (longo) 3 else 1,
        label = { Text(rotulo) }, placeholder = { Text(dica, fontSize = 12.sp) }, shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LaranjaRestante, unfocusedBorderColor = BordaRestante,
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
            focusedTextColor = TextoRestante, unfocusedTextColor = TextoRestante))
}

// Usa botoes e menu em vez de texto que parece selecionavel mas nao responde.
@Composable
fun SeletorRestante(rotulo: String, valor: String, opcoes: List<Pair<String, String>>, selecionar: (String) -> Unit) {
    var aberto by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(rotulo, color = MarromRestante, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Box {
            OutlinedButton(onClick = { aberto = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = RoundedCornerShape(14.dp)) {
                Text(valor, Modifier.weight(1f), color = TextoRestante)
                Text("⌄", color = MarromRestante)
            }
            DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
                opcoes.forEach { (id, nome) -> DropdownMenuItem(text = { Text(nome) }, onClick = { aberto = false; selecionar(id) }) }
            }
        }
    }
}

// Mantem filtros legiveis em telas estreitas com rolagem horizontal.
@Composable
fun FiltrosRestantes(opcoes: List<String>, atual: String, mudar: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opcoes.forEach { FilterChip(selected = atual == it, onClick = { mudar(it) }, label = { Text(it) },
            shape = RoundedCornerShape(22.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CremeRestante)) }
    }
}

// Um botao principal com tamanho de toque consistente.
@Composable
fun BotaoRestante(texto: String, ativo: Boolean = true, acao: () -> Unit) {
    Button(onClick = acao, enabled = ativo, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = LaranjaRestante, contentColor = TextoRestante)) {
        Text(texto, fontWeight = FontWeight.Bold)
    }
}

// Um cartao navegavel explica para onde a acao leva.
@Composable
fun AtalhoRestante(titulo: String, descricao: String, cor: Color = Color.White, acao: () -> Unit) {
    Surface(onClick = acao, color = cor, shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, BordaRestante)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(titulo, color = TextoRestante, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(descricao, color = MarromRestante, fontSize = 13.sp)
            }
            Text("›", color = MarromRestante, fontSize = 24.sp)
        }
    }
}

// Apresenta retorno claro para validacao e erros recuperaveis.
@Composable
fun MensagemRestante(texto: String, fechar: () -> Unit) {
    AlertDialog(onDismissRequest = fechar, text = { Text(texto) },
        confirmButton = { TextButton(onClick = fechar) { Text("Entendi") } }, containerColor = FundoRestante)
}