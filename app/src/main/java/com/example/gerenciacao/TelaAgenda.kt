package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.GregorianCalendar
import java.util.Calendar

// Define as cores da agenda seguindo a referência.
private val FundoAgenda = Color(0xFFFFFCF7)
private val TextoAgenda = Color(0xFF38291F)
private val MarromAgenda = Color(0xFF783E19)
private val AmareloAgenda = Color(0xFFFFB414)
private val CremeAgenda = Color(0xFFFFEFC5)
private val BordaAgenda = Color(0xFFEADFD2)

// Exibe o calendário e os compromissos fictícios.
// Os callbacks permitem navegar para as outras telas.
@Composable
fun TelaAgenda(
    aoInicio: () -> Unit,
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit
) {
    // Começa na data demonstrativa da imagem.
    var ano by rememberSaveable { mutableIntStateOf(2026) }
    var mes by rememberSaveable { mutableIntStateOf(10) }
    var dia by rememberSaveable { mutableIntStateOf(7) }
    var mostrarTodos by rememberSaveable { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }

    // Volta ao início pelo botão Voltar do Android.
    BackHandler(onBack = aoInicio)

    // Os meses do Calendar começam em zero: janeiro = 0.
    val nomesMeses = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril",
        "Maio", "Junho", "Julho", "Agosto",
        "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    // Calcula quantos dias existem no mês e em qual coluna ele começa.
    val calendario = GregorianCalendar(ano, mes, 1)
    val quantidadeDias = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    val primeiraColuna = calendario.get(Calendar.DAY_OF_WEEK) - 1
    val quantidadeSemanas = (primeiraColuna + quantidadeDias + 6) / 7

    // Os dois compromissos são exemplos de 7 de novembro de 2026.
    val mesComExemplos = ano == 2026 && mes == 10
    val exibirExemplos = mesComExemplos && (dia == 7 || mostrarTodos)

    Column(
        Modifier.fillMaxSize().background(FundoAgenda)
    ) {
        // Mantém a faixa laranja presente nas outras telas.
        Box(
            Modifier.fillMaxWidth()
                .height(4.dp)
                .background(AmareloAgenda)
        )

        Scaffold(
            modifier = Modifier.weight(1f).statusBarsPadding(),
            containerColor = FundoAgenda,
            bottomBar = {
                MenuInferiorInicio(
                    avisar = { aviso = it },
                    selecionado = "Agenda",
                    aoInicio = aoInicio,
                    aoAnimais = aoAnimais,
                    aoAdicionar = aoAdicionar,
                    aoAgenda = {}
                )
            }
        ) { espacos ->
            Column(
                Modifier.fillMaxSize()
                    .padding(espacos)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 12.dp, bottom = 24.dp)
            ) {
                // Cabeçalho com o logotipo e as notificações.
                Row(
                    Modifier.fillMaxWidth().height(60.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painterResource(R.drawable.tipografialogin),
                        contentDescription = "GerenciaCÃO",
                        modifier = Modifier.width(132.dp).height(56.dp),
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = {
                            aviso = "Nenhuma nova notificação."
                        }
                    ) {
                        Icon(
                            painterResource(R.drawable.inicio_sino),
                            contentDescription = "Notificações",
                            tint = MarromAgenda,
                            modifier = Modifier
                                .background(CremeAgenda, CircleShape)
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    "Agenda",
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoAgenda
                )

                Spacer(Modifier.height(14.dp))

                // As setas avançam e voltam os meses, inclusive entre anos.
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${nomesMeses[mes]} $ano",
                        modifier = Modifier.weight(1f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoAgenda
                    )

                    TextButton(
                        onClick = {
                            if (mes == 0) {
                                mes = 11
                                ano--
                            } else {
                                mes--
                            }
                            dia = 1
                            mostrarTodos = false
                        }
                    ) {
                        Text("‹", color = MarromAgenda, fontSize = 24.sp)
                    }

                    TextButton(
                        onClick = {
                            if (mes == 11) {
                                mes = 0
                                ano++
                            } else {
                                mes++
                            }
                            dia = 1
                            mostrarTodos = false
                        }
                    ) {
                        Text("›", color = MarromAgenda, fontSize = 24.sp)
                    }
                }

                // Calendário com sete colunas e seleção de dia.
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BordaAgenda)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(10.dp)
                    ) {
                        Row {
                            listOf("D", "S", "T", "Q", "Q", "S", "S")
                                .forEach { letra ->
                                    Box(
                                        Modifier.weight(1f).height(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            letra,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MarromAgenda
                                        )
                                    }
                                }
                        }

                        repeat(quantidadeSemanas) { semana ->
                            Row {
                                repeat(7) { coluna ->
                                    val numero =
                                        semana * 7 + coluna - primeiraColuna + 1

                                    Box(
                                        Modifier.weight(1f).height(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (numero in 1..quantidadeDias) {
                                            Box(
                                                Modifier.size(36.dp)
                                                    .background(
                                                        if (dia == numero) {
                                                            AmareloAgenda
                                                        } else {
                                                            Color.Transparent
                                                        },
                                                        CircleShape
                                                    )
                                                    .clickable {
                                                        dia = numero
                                                        mostrarTodos = false
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "$numero",
                                                    fontSize = 12.sp,
                                                    color = TextoAgenda
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(22.dp))

                // O botão alterna entre o dia selecionado e todo o mês.
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Próximos compromissos",
                        modifier = Modifier.weight(1f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoAgenda
                    )

                    TextButton(
                        onClick = { mostrarTodos = !mostrarTodos },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            if (mostrarTodos) "Ver dia" else "Ver todos",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MarromAgenda
                        )
                    }
                }

                // Mostra os exemplos apenas na data correspondente.
                if (exibirExemplos) {
                    CompromissoAgenda(
                        titulo = "Vacina da Lua",
                        descricao = "7 nov. · 14h00 · Cuidados",
                        icone = R.drawable.inicio_calendario,
                        fundoIcone = CremeAgenda
                    ) {
                        aviso = "Vacina da Lua\n7 de novembro, às 14h.\nCompromisso de exemplo."
                    }

                    Spacer(Modifier.height(10.dp))

                    CompromissoAgenda(
                        titulo = "Visita ao Bento",
                        descricao = "7 nov. · 16h00 · Adoção",
                        icone = R.drawable.inicio_coracao,
                        fundoIcone = Color(0xFFE1F3E8)
                    ) {
                        aviso = "Visita ao Bento\n7 de novembro, às 16h.\nCompromisso de exemplo."
                    }
                } else {
                    Text(
                        if (mostrarTodos) {
                            "Nenhum compromisso neste mês."
                        } else {
                            "Nenhum compromisso em $dia/${mes + 1}/$ano."
                        },
                        modifier = Modifier.padding(vertical = 20.dp),
                        fontSize = 14.sp,
                        color = Color(0xFF7E7066)
                    )
                }
            }
        }
    }

    // Apresenta informações ao tocar em um compromisso ou notificação.
    aviso?.let { mensagem ->
        AlertDialog(
            onDismissRequest = { aviso = null },
            title = { Text("Agenda", color = TextoAgenda) },
            text = { Text(mensagem) },
            confirmButton = {
                TextButton(onClick = { aviso = null }) {
                    Text("Fechar", color = MarromAgenda)
                }
            },
            containerColor = FundoAgenda
        )
    }
}

// Desenha um cartão de compromisso seguindo a referência.
@Composable
private fun CompromissoAgenda(
    titulo: String,
    descricao: String,
    icone: Int,
    fundoIcone: Color,
    aoClicar: () -> Unit
) {
    Surface(
        onClick = aoClicar,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BordaAgenda),
        shadowElevation = 1.dp
    ) {
        Row(
            Modifier.heightIn(min = 78.dp).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(46.dp)
                    .background(fundoIcone, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painterResource(icone),
                    contentDescription = null,
                    modifier = Modifier.size(23.dp),
                    tint = MarromAgenda
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    titulo,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoAgenda
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    descricao,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Color(0xFF7E7066)
                )
            }

            Icon(
                painterResource(R.drawable.inicio_seta),
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = MarromAgenda
            )
        }
    }
}
