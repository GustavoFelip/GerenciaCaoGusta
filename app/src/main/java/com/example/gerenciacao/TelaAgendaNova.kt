package com.example.gerenciacao

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.GregorianCalendar

// Mantem o calendario existente e passa a listar os compromissos realmente salvos.
@Composable
fun TelaAgendaIntegrada(ctx: ContextoAbrigo) {
    var ano by rememberSaveable { mutableIntStateOf(2026) }
    var mes by rememberSaveable { mutableIntStateOf(10) }
    var dia by rememberSaveable { mutableIntStateOf(7) }
    var todos by rememberSaveable { mutableStateOf(false) }
    // Ao criar ou reagendar, abre o dia do registro salvo em vez de parecer que ele sumiu.
    LaunchedEffect(ctx.dados.agendaDataAlvo) {
        val alvo = ctx.dados.agendaDataAlvo
        if (alvo != null && dataValidaAbrigo(alvo)) {
            ano = alvo.takeLast(4).toInt(); mes = alvo.substring(3, 5).toInt() - 1; dia = alvo.take(2).toInt(); todos = false
            ctx.atualizar(ctx.dados.copy(agendaDataAlvo = null))
        }
    }
    val meses = listOf("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro")
    val calendario = GregorianCalendar(ano, mes, 1)
    val quantidade = calendario.getActualMaximum(Calendar.DAY_OF_MONTH)
    val colunaInicial = calendario.get(Calendar.DAY_OF_WEEK) - 1
    val lista = ctx.dados.compromissos.filter { doPeriodoAbrigo(it.data, mes + 1, ano) && (todos || it.data.take(2).toInt() == dia) }
        .sortedBy { chaveDataAbrigo(it.data) + it.horario }
    BaseRestante(ctx, "Agenda", "Agenda") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("${meses[mes]} $ano", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            TextButton(onClick = { if (mes == 0) { mes = 11; ano-- } else mes--; dia = 1; todos = false }) { Text("‹", fontSize = 24.sp) }
            TextButton(onClick = { if (mes == 11) { mes = 0; ano++ } else mes++; dia = 1; todos = false }) { Text("›", fontSize = 24.sp) }
        }
        CartaoRestante {
            Row { listOf("D", "S", "T", "Q", "Q", "S", "S").forEach {
                Box(Modifier.weight(1f).height(30.dp), contentAlignment = Alignment.Center) { Text(it, fontSize = 12.sp) }
            } }
            repeat((colunaInicial + quantidade + 6) / 7) { semana ->
                Row {
                    repeat(7) { coluna ->
                        val numero = semana * 7 + coluna - colunaInicial + 1
                        Box(Modifier.weight(1f).height(44.dp), contentAlignment = Alignment.Center) {
                            if (numero in 1..quantidade) Box(Modifier.size(40.dp)
                                .background(if (dia == numero) LaranjaRestante else androidx.compose.ui.graphics.Color.Transparent, CircleShape)
                                .clickable { dia = numero; todos = false }, contentAlignment = Alignment.Center) {
                                Text(numero.toString(), fontSize = 12.sp, color = TextoRestante)
                            }
                        }
                    }
                }
            }
        }
        BotaoRestante("Novo compromisso") {
            ctx.atualizar(ctx.dados.copy(editarCompromissoId = null))
            ctx.abrir("novo_compromisso")
        }
        TextButton(onClick = { todos = !todos }) { Text(if (todos) "Ver dia selecionado" else "Ver todos do mês") }
        Text(if (todos) "Compromissos do mês" else "Compromissos de $dia/${mes + 1}/$ano", fontWeight = FontWeight.Bold)
        if (lista.isEmpty()) Text("Nenhum compromisso neste período.", color = MarromRestante)
        lista.forEach { compromisso ->
            AtalhoRestante(compromisso.titulo, "${compromisso.data} · ${compromisso.horario} · ${compromisso.tipo}") {
                ctx.atualizar(ctx.dados.copy(compromissoId = compromisso.id))
                ctx.abrir("detalhes_compromisso")
            }
        }
        Text("Agenda local. Nenhuma notificação automática será enviada.", fontSize = 12.sp)
    }
}