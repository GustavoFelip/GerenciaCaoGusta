package com.example.gerenciacao

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.BorderStroke

import androidx.compose.foundation.Canvas

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

import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.Path

import androidx.compose.ui.graphics.drawscope.Stroke

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.semantics.contentDescription

import androidx.compose.ui.semantics.semantics

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.compose.ui.tooling.preview.Preview

import java.text.NumberFormat

import java.util.Locale

// Compartilha somente a acao de abrir Financas com o menu das telas existentes.

// O destino e definido na MainActivity, sem guardar estado global de navegacao.

internal val LocalAbrirFinancas = staticCompositionLocalOf<() -> Unit> { {} }

// Mantem a paleta harmonizada com o inicio do GerenciaCao.

private val FundoFin = Color(0xFFFFFCF7)

private val TextoFin = Color(0xFF38291F)

private val MarromFin = Color(0xFF6B3919)

private val LaranjaFin = Color(0xFFFFB21C)

private val CremeFin = Color(0xFFFFF0C9)

private val VerdeClaroFin = Color(0xFFE1F3E8)

private val VerdeFin = Color(0xFF2F7051)

private val PessegoFin = Color(0xFFFFE8DA)

private val DespesaFin = Color(0xFF9A4F2B)

private val BordaFin = Color(0xFFE8DFD4)

private val SecundarioFin = Color(0xFF8B7B6C)

// Representa um lancamento demonstrativo; produtos nao possuem valor monetario.

private data class MovimentoFin(

    val titulo: String,

    val detalhe: String,

    val tipo: String,

    val centavos: Long? = null,

    val quantidade: String? = null

)

// Formata centavos como reais, evitando usar ponto flutuante para valores financeiros.

private fun reaisFin(centavos: Long): String =

    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))

        .format(java.math.BigDecimal.valueOf(centavos, 2))

// Exibe o resumo financeiro com valores ficticios e controles locais funcionais.

@Composable

fun TelaFinancas(

    aoInicio: () -> Unit,

    aoAnimais: () -> Unit,

    aoAdicionar: () -> Unit,

    aoAgenda: () -> Unit,

    aoNotificacoes: (() -> Unit)? = null,

    aoPerfil: (() -> Unit)? = null,

    aoRegistrarDoacao: (() -> Unit)? = null,

    aoRegistrarDespesa: (() -> Unit)? = null,

    aoRelatorios: (() -> Unit)? = null,
    lancamentos: List<LancamentoAbrigo> = emptyList()

) {

    // Preserva filtros, periodo e visibilidade ao recriar a tela.

    var mes by rememberSaveable { mutableIntStateOf(10) }

    var ano by rememberSaveable { mutableIntStateOf(2026) }
    var filtro by rememberSaveable { mutableStateOf("Tudo") }

    var ocultar by rememberSaveable { mutableStateOf(false) }

    var escolherMes by remember { mutableStateOf(false) }

    var mensagem by rememberSaveable { mutableStateOf<String?>(null) }

    var tituloDialogo by rememberSaveable { mutableStateOf("Finanças") }

    val meses = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",

        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")

    // A mesma fonte alimenta os formularios, o resumo e o relatorio.
    val periodo = lancamentos.filter { doPeriodoAbrigo(it.data, mes + 1, ano) }
    val movimentos = periodo.sortedByDescending { chaveDataAbrigo(it.data) }.map {
        MovimentoFin(it.descricao, "${it.data} · ${it.categoria.ifBlank { it.natureza.name.lowercase() }}",
            if (it.natureza == NaturezaAbrigo.DESPESA) "Despesas" else "Doações",
            centavos = if (it.natureza == NaturezaAbrigo.DINHEIRO || it.natureza == NaturezaAbrigo.DESPESA) it.centavos else null,
            quantidade = if (it.natureza == NaturezaAbrigo.PRODUTOS || it.natureza == NaturezaAbrigo.BICHINHO) it.quantidade else null)
    }
    val entradas = periodo.filter { it.natureza == NaturezaAbrigo.DINHEIRO }.sumOf { it.centavos }
    val saidas = periodo.filter { it.natureza == NaturezaAbrigo.DESPESA }.sumOf { it.centavos }
    val resultado = entradas - saidas
    val visiveis = movimentos.filter { filtro == "Tudo" || it.tipo == filtro }
    val produtos = periodo.filter { it.natureza == NaturezaAbrigo.PRODUTOS }

    // Mostra um retorno claro enquanto as telas de registro nao forem implementadas.

    val informar: (String, String) -> Unit = { titulo, texto ->

        tituloDialogo = titulo

        mensagem = texto

    }

    val valor: (Long) -> String = { if (ocultar) "••••" else reaisFin(it) }

    BackHandler(onBack = aoInicio)

    Column(Modifier.fillMaxSize().background(FundoFin)) {

        // Repete a faixa laranja do restante do aplicativo.

        // A cor laranja pertence ao cabecalho compartilhado.

        MolduraPadraoApp(

            modifier = Modifier.weight(1f),

            containerColor = FundoFin,

            bottomBar = {

                MenuInferiorInicio(

                    avisar = { informar("Finanças", it) },

                    selecionado = "Finanças", aoInicio = aoInicio,

                    aoAnimais = aoAnimais, aoAdicionar = aoAdicionar, aoAgenda = aoAgenda

                )

            }

        ) { espacos ->

            Column(Modifier.fillMaxSize().padding(espacos)

                .verticalScroll(rememberScrollState())

                .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp)) {

                // Posiciona notificacoes a esquerda e o perfil a direita, sem logo no topo.

                // Foto e menu agora sao apresentados pelo cabecalho compartilhado.

                Spacer(Modifier.height(22.dp))

                // Mantem o seletor abaixo do titulo para caber em celulares menores.

                TituloPaginaPadrao("Finanças")

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,

                    horizontalArrangement = Arrangement.SpaceBetween) {

                    Text("Visão do abrigo", fontSize = 13.sp, color = SecundarioFin)

                    Box {

                        TextButton(onClick = { escolherMes = true }) {

                            Text("${meses[mes]} $ano ▾", fontSize = 13.sp, color = MarromFin)

                        }

                        DropdownMenu(expanded = escolherMes, onDismissRequest = { escolherMes = false }) {

                            meses.forEachIndexed { indice, nome ->

                                DropdownMenuItem(text = { Text("$nome $ano") }, onClick = {

                                    mes = indice

                                    filtro = "Tudo"

                                    escolherMes = false

                                })

                            }

                        }

                    }

                }

                Row {
                    TextButton(onClick = { ano--; filtro = "Tudo" }) { Text("‹ Ano anterior", color = MarromFin) }
                    TextButton(onClick = { ano++; filtro = "Tudo" }) { Text("Próximo ano ›", color = MarromFin) }
                }
                Text("Registros locais · inclui exemplos iniciais", fontSize = 11.sp, color = SecundarioFin)

                Spacer(Modifier.height(18.dp))

                // Resultado do periodo nao representa saldo bancario nem inclui produtos.

                Surface(shape = RoundedCornerShape(22.dp), color = Color.White,

                    border = BorderStroke(1.dp, BordaFin)) {

                    Column(Modifier.fillMaxWidth().padding(18.dp)) {

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                            Text("Resultado do mês", Modifier.weight(1f), fontSize = 17.sp,

                                fontWeight = FontWeight.Bold, color = TextoFin)

                            IconButton(onClick = { ocultar = !ocultar }) {

                                OlhoFin(ocultar)

                            }

                        }

                        Text(if (ocultar) "••••" else "${if (resultado > 0) "+ " else ""}${reaisFin(resultado)}",

                            fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold, color = VerdeFin)

                        Spacer(Modifier.height(8.dp))

                        Text("Doações em dinheiro menos despesas", fontSize = 12.sp,

                            lineHeight = 17.sp, color = SecundarioFin)

                        HorizontalDivider(Modifier.padding(vertical = 18.dp), color = BordaFin)

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                            ResumoFin("Doações em dinheiro", valor(entradas), VerdeFin, Modifier.weight(1f))

                            ResumoFin("Despesas", valor(saidas), DespesaFin, Modifier.weight(1f))

                        }

                    }

                }

                Spacer(Modifier.height(20.dp))

                // Atalhos preservam pontos de integracao para as proximas telas.

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                    AtalhoFin("Registrar\ndoação", R.drawable.adicionar_doacao, CremeFin, Modifier.weight(1f)) {

                        aoRegistrarDoacao?.invoke() ?: informar("Registrar doação", "O formulário de doações ainda será criado.")

                    }

                    AtalhoFin("Registrar\ndespesa", R.drawable.inicio_recibo, PessegoFin, Modifier.weight(1f)) {

                        aoRegistrarDespesa?.invoke() ?: informar("Registrar despesa", "O formulário de despesas ainda será criado.")

                    }

                    AtalhoFin("Relatórios", R.drawable.inicio_carteira, CremeFin, Modifier.weight(1f)) {

                        aoRelatorios?.invoke() ?: informar("Relatórios", "A tela de relatórios ainda será criada.")

                    }

                }

                Spacer(Modifier.height(20.dp))

                // Mantem quantidades recebidas separadas do resumo em reais.

                Surface(onClick = {

                    informar("Doações em produtos", if (produtos.isEmpty()) "Nenhum produto recebido neste período." else produtos.joinToString("\n") { "${it.descricao}: ${it.quantidade}" } + "\n\nProdutos não entram nos totais em dinheiro.")

                }, shape = RoundedCornerShape(18.dp), color = VerdeClaroFin) {

                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

                        IconFin(R.drawable.adicionar_doacao, null, VerdeFin)

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {

                            Text("Doações em produtos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextoFin)

                            Text(if (ocultar) "••••" else if (produtos.isEmpty()) "Nenhum recebimento" else "${produtos.size} ${if (produtos.size == 1) "registro recebido" else "registros recebidos"}",

                                fontSize = 12.sp, lineHeight = 18.sp, color = SecundarioFin)

                        }

                        IconFin(R.drawable.inicio_seta, null)

                    }

                }

                Spacer(Modifier.height(22.dp))

                // O historico abaixo e um recorte do periodo, nao a soma de todos os lancamentos do mes.

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

                    Text("Movimentações", Modifier.weight(1f), fontSize = 20.sp,

                        fontWeight = FontWeight.Bold, color = TextoFin)

                    TextButton(onClick = {

                        informar("Movimentações · ${meses[mes]}", if (movimentos.isEmpty()) "Nenhuma movimentação neste período." else movimentos.joinToString("\n\n") {

                            val exibicao = if (ocultar) "••••" else it.quantidade ?: reaisFin(it.centavos ?: 0L)

                            "${it.titulo}\n${it.detalhe} · $exibicao"

                        })

                    }) { Text("Ver todas", fontSize = 12.sp, color = MarromFin) }

                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    listOf("Tudo", "Doações", "Despesas").forEach { opcao ->

                        FilterChip(selected = filtro == opcao, onClick = { filtro = opcao },

                            label = { Text(opcao, fontSize = 12.sp) }, shape = CircleShape,

                            border = null, colors = FilterChipDefaults.filterChipColors(

                                selectedContainerColor = CremeFin, selectedLabelColor = MarromFin,

                                containerColor = Color.White, labelColor = SecundarioFin))

                    }

                }

                Spacer(Modifier.height(10.dp))

                Surface(shape = RoundedCornerShape(20.dp), color = Color.White,

                    border = BorderStroke(1.dp, BordaFin)) {

                    Column(Modifier.fillMaxWidth()) {

                        if (visiveis.isEmpty()) {

                            Text("Nenhuma movimentação neste período.", Modifier.padding(20.dp),

                                fontSize = 14.sp, color = SecundarioFin)

                        }

                        visiveis.forEachIndexed { indice, movimento ->

                            val entrada = movimento.tipo == "Doações"

                            val quantia = if (ocultar) "••••" else movimento.quantidade

                                ?: "${if (entrada) "+ " else "− "}${reaisFin(movimento.centavos ?: 0L)}"

                            Surface(onClick = { informar(movimento.titulo, "${movimento.detalhe}\n$quantia\n\nRegistro local; pode ser um exemplo inicial.") }, color = Color.Transparent) {

                                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {

                                    Box(Modifier.size(40.dp).background(if (entrada) VerdeClaroFin else PessegoFin, RoundedCornerShape(12.dp)),

                                        contentAlignment = Alignment.Center) {

                                        IconFin(if (entrada) R.drawable.adicionar_doacao else R.drawable.inicio_recibo,

                                            null, if (entrada) VerdeFin else DespesaFin)

                                    }

                                    Spacer(Modifier.width(10.dp))

                                    Column(Modifier.weight(1f)) {

                                        Text(movimento.titulo, fontSize = 14.sp, lineHeight = 18.sp,

                                            fontWeight = FontWeight.SemiBold, color = TextoFin)

                                        Text(movimento.detalhe, fontSize = 11.sp, lineHeight = 16.sp, color = SecundarioFin)

                                    }

                                    Spacer(Modifier.width(8.dp))

                                    Text(quantia, fontSize = 13.sp, fontWeight = FontWeight.Bold,

                                        color = if (movimento.quantidade != null) TextoFin else if (entrada) VerdeFin else DespesaFin)

                                }

                            }

                            if (indice < visiveis.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = BordaFin)

                        }

                    }

                }

            }

        }

    }

    // Mostra detalhes e avisos sem simular registros que ainda nao foram implementados.

    mensagem?.let { texto ->

        AlertDialog(onDismissRequest = { mensagem = null }, title = { Text(tituloDialogo) },

            text = { Column(Modifier.verticalScroll(rememberScrollState())) { Text(texto) } },

            confirmButton = { TextButton(onClick = { mensagem = null }) { Text("Fechar", color = MarromFin) } },

            containerColor = FundoFin)

    }

}

// Exibe os dois totais com a mesma hierarquia e sem misturar suas naturezas.

@Composable

private fun ResumoFin(titulo: String, valor: String, cor: Color, modifier: Modifier) {

    Column(modifier) {

        Text(titulo, fontSize = 11.sp, lineHeight = 16.sp, color = SecundarioFin)

        Spacer(Modifier.height(5.dp))

        Text(valor, fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold, color = cor)

    }

}

// Reutiliza os icones ja presentes no projeto, sem novas dependencias.

@Composable

private fun IconFin(recurso: Int, descricao: String?, cor: Color = MarromFin) {

    Icon(painterResource(recurso), descricao, Modifier.size(22.dp), tint = cor)

}

// Organiza os tres atalhos em uma linha, com cores suaves e areas de toque completas.

@Composable

private fun AtalhoFin(titulo: String, icone: Int, fundo: Color, modifier: Modifier, aoClicar: () -> Unit) {

    Surface(onClick = aoClicar, modifier = modifier, color = Color.Transparent,

        shape = RoundedCornerShape(16.dp)) {

        Column(Modifier.heightIn(min = 100.dp).padding(vertical = 8.dp),

            horizontalAlignment = Alignment.CenterHorizontally) {

            Box(Modifier.size(50.dp).background(fundo, RoundedCornerShape(16.dp)),

                contentAlignment = Alignment.Center) { IconFin(icone, null) }

            Spacer(Modifier.height(8.dp))

            Text(titulo, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold,

                color = TextoFin, textAlign = androidx.compose.ui.text.style.TextAlign.Center)

        }

    }

}

// Desenha o olho de visibilidade sem baixar uma biblioteca de icones.

@Composable

private fun OlhoFin(oculto: Boolean) {

    val descricao = if (oculto) "Mostrar valores" else "Ocultar valores"

    Canvas(Modifier.size(24.dp).then(Modifier.semanticsDescricaoFin(descricao))) {

        val forma = Path().apply {

            moveTo(0f, size.height / 2)

            quadraticBezierTo(size.width / 2, 0f, size.width, size.height / 2)

            quadraticBezierTo(size.width / 2, size.height, 0f, size.height / 2)

        }

        drawPath(forma, MarromFin, style = Stroke(width = 1.6.dp.toPx()))

        drawCircle(MarromFin, radius = size.width * 0.13f, center = center, style = Stroke(width = 1.6.dp.toPx()))

        if (oculto) drawLine(MarromFin, Offset(0f, size.height), Offset(size.width, 0f), strokeWidth = 1.6.dp.toPx())

    }

}

// Informa ao leitor de tela a funcao do controle de visibilidade.

private fun Modifier.semanticsDescricaoFin(descricao: String): Modifier =

    this.then(Modifier.semantics { contentDescription = descricao })

// Permite visualizar o financeiro no editor sem conectar banco de dados.

@Preview(showBackground = true, showSystemUi = true)

@Composable

private fun PreviewTelaFinancas() {

    MaterialTheme {

        TelaFinancas(aoInicio = {}, aoAnimais = {}, aoAdicionar = {}, aoAgenda = {})

    }

}