package com.example.gerenciacao

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Reutiliza a mesma tela para qualquer abrigo selecionado, com dados resolvidos pelo ID.
@Composable
fun TelaInformacoesAbrigo(ctx: ContextoAbrigo, abrigo: AbrigoRede?, conectado: Boolean, aoDefinirConexao: (Boolean) -> Unit) {
    var confirmarRemocao by rememberSaveable(abrigo?.id) { mutableStateOf(false) }
    EstruturaRedes(ctx, "Informações do abrigo") {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item { TituloPaginaPadrao("Informações do abrigo", ctx.voltar) }
            if (abrigo == null) item {
                CartaoRestante(CremeRestante) {
                    Text("Este abrigo não está disponível.")
                    BotaoRestante("Voltar") { ctx.voltar() }
                }
            }
            else {
                item {
                    CapaAbrigoRede(abrigo, Modifier.fillMaxWidth().height(160.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(abrigo.nome, fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
                    Spacer(Modifier.height(7.dp))
                    Text("${abrigo.bairro} · ${abrigo.cidade}/SP", fontSize = 13.sp, color = MarromRestante)
                    Spacer(Modifier.height(5.dp))
                    Text(distanciaRede(abrigo), fontSize = 12.sp, color = MarromRestante)
                }
                item { Text(abrigo.descricao, fontSize = 14.sp, lineHeight = 21.sp, color = TextoRestante) }
                // Os tres indicadores usam os dados deste abrigo; vagas nao sao um numero independente.
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IndicadorInformacoesRede(abrigo.animais, "Animais", Modifier.weight(1f))
                        IndicadorInformacoesRede(abrigo.capacidade, "Capacidade", Modifier.weight(1f))
                        IndicadorInformacoesRede(vagasRede(abrigo), "Vagas", Modifier.weight(1f))
                    }
                }
                item {
                    CartaoRestante {
                        Text("Contato", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
                        // Contatos sao apenas exibidos e selecionaveis, sem telefonar ou enviar mensagens.
                        SelectionContainer {
                            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                                LinhaInformacoesRede("E-mail demonstrativo", abrigo.emailExemplo)
                                LinhaInformacoesRede("Telefone", abrigo.telefone)
                                LinhaInformacoesRede("Endereço", abrigo.endereco)
                            }
                        }
                    }
                }
                item {
                    Text("Equipe", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
                    Spacer(Modifier.height(12.dp))
                    if (abrigo.equipeExemplo.isEmpty()) Text("Equipe não informada.")
                    else Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        abrigo.equipeExemplo.forEach { pessoa ->
                            Column(Modifier.width(62.dp), horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.size(48.dp).clip(CircleShape).background(CremeRestante), contentAlignment = Alignment.Center) {
                                    Text(pessoa.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MarromRestante, fontSize = 21.sp)
                                }
                                Text(pessoa, fontSize = 12.sp, color = TextoRestante)
                            }
                        }
                    }
                }
                // Conectar atualiza a aba Suas conexoes, sem alterar a equipe do proprio abrigo.
                item {
                    if (conectado) {
                        CartaoRestante(VerdeRestante) {
                            Text("✓ Nas suas conexões", fontWeight = FontWeight.Bold, color = TextoRestante)
                            Text("Este abrigo aparece na aba Suas conexões.", fontSize = 13.sp, color = MarromRestante)
                        }
                        TextButton(onClick = { confirmarRemocao = true }) { Text("Remover conexão", color = MarromRestante) }
                    } else BotaoRestante("Conectar-se") { aoDefinirConexao(true) }
                    Text("Conexão demonstrativa, salva nesta sessão. Nenhuma solicitação ou mensagem é enviada ao abrigo.",
                        fontSize = 11.sp, lineHeight = 16.sp, color = MarromRestante)
                }
                item {
                    Text("Perfil, contatos, equipe e distâncias são exemplos. Os e-mails .example não são endereços reais de atendimento.",
                        fontSize = 11.sp, lineHeight = 16.sp, color = MarromRestante)
                }
            }
        }
    }
    // Exige confirmacao antes de retirar um abrigo das conexoes locais.
    if (confirmarRemocao && abrigo != null) AlertDialog(onDismissRequest = { confirmarRemocao = false },
        title = { Text("Remover conexão?") }, text = { Text("${abrigo.nome} deixará de aparecer na aba Suas conexões.") },
        confirmButton = { TextButton(onClick = { confirmarRemocao = false; aoDefinirConexao(false) }) { Text("Remover") } },
        dismissButton = { TextButton(onClick = { confirmarRemocao = false }) { Text("Cancelar") } })
}

// Apresenta valor e legenda sem uma altura fixa que corte fontes ampliadas.
@Composable
private fun IndicadorInformacoesRede(valor: Int, legenda: String, modifier: Modifier) {
    Surface(modifier = modifier, color = CremeRestante, shape = androidx.compose.foundation.shape.RoundedCornerShape(15.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 15.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(valor.toString(), fontSize = 25.sp, fontWeight = FontWeight.Bold, color = TextoRestante)
            Text(legenda, fontSize = 11.sp, color = MarromRestante)
        }
    }
}

// Distingue o rotulo do conteudo do contato e preserva a leitura por acessibilidade.
@Composable
private fun LinhaInformacoesRede(rotulo: String, valor: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(rotulo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MarromRestante)
        Text(valor, fontSize = 14.sp, lineHeight = 20.sp, color = TextoRestante)
    }
}

// Mostra os dados do Cantinho dos Focinhos e permite testar o botao de conexao no Preview interativo.
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewInformacoesAbrigo() {
    var conectado by remember { mutableStateOf(true) }
    MaterialTheme {
        TelaInformacoesAbrigo(contextoPreviewRestante(), AbrigosRedeExemplo.firstOrNull { it.id == "cantinho-focinhos" }, conectado) { conectado = it }
    }
}