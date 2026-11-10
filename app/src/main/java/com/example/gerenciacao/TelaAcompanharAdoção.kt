package com.example.gerenciacao

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Reutiliza a identidade visual das telas de animais e adocoes.
private val FundoAcompanhamento = Color(0xFFFFFCF7)
private val TextoAcompanhamento = Color(0xFF38291F)
private val MarromAcompanhamento = Color(0xFF6B3919)
private val LaranjaAcompanhamento = Color(0xFFFFB21C)
private val CremeAcompanhamento = Color(0xFFFFF0C9)
private val VerdeAcompanhamento = Color(0xFFE1F3E8)
private val BordaAcompanhamento = Color(0xFFE8DFD4)

// Atualiza a orientacao junto com a etapa, sem inventar agendamentos ou horarios.
fun proximaAcaoAdocao(etapa: EtapaAdocao): String = when (etapa) {
    EtapaAdocao.INTERESSE -> "Converse com a pessoa interessada."
    EtapaAdocao.ENTREVISTA -> "Conheça o perfil e combine a visita."
    EtapaAdocao.VISITA -> "Confirme a data e o horário da visita com a pessoa interessada."
    EtapaAdocao.ACOMPANHAMENTO -> "Acompanhe a adaptação no novo lar."
}

// A mesma tela recebe o processo e o animal escolhidos, sem fixar o nome Lua.
@Composable
fun TelaAcompanharAdocao(
    animal: AnimalExemplo,
    processo: ProcessoAdocao,
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    aoAnimais: () -> Unit,
    aoAdicionar: () -> Unit,
    aoAgenda: () -> Unit,
    aoVerFicha: () -> Unit,
    aoAlterarEtapa: (EtapaAdocao) -> Unit,
    aoConfirmarAdocao: () -> Unit
) {
    // Os dialogos pertencem a este processo e sobrevivem a rotacao.
    var escolherEtapa by rememberSaveable(processo.id) { mutableStateOf(false) }
    var confirmarAdocao by rememberSaveable(processo.id) { mutableStateOf(false) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val adotado = animal.situacao == "Adotado"
    val foto = fotoMiniaturaAdocao(animal.fotoUri)
    val etapas = EtapaAdocao.values().toList()
    BackHandler(onBack = aoVoltar)

    Column(Modifier.fillMaxSize().background(FundoAcompanhamento)) {
        // A cor laranja pertence ao cabecalho compartilhado.
        MolduraPadraoApp(modifier = Modifier.weight(1f), containerColor = FundoAcompanhamento,
            bottomBar = {
                MenuInferiorInicio(avisar = { aviso = it }, selecionado = "Animais",
                    aoInicio = aoInicio, aoAnimais = aoAnimais, aoAdicionar = aoAdicionar, aoAgenda = aoAgenda)
            }
        ) { espacos ->
            Column(Modifier.fillMaxSize().padding(espacos).verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                TituloPaginaPadrao("Adoção do bichinho", aoVoltar)

                // A foto e os dados vem da ficha atualizada, inclusive depois da edicao.
                Surface(color = Color.White, shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BordaAcompanhamento), shadowElevation = 1.dp) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(86.dp).clip(RoundedCornerShape(16.dp)).background(CremeAcompanhamento), contentAlignment = Alignment.Center) {
                            if (foto != null) Image(foto, "Foto de ${animal.nome}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Text(animal.nome.take(1).uppercase(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = MarromAcompanhamento)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(animal.nome, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = TextoAcompanhamento)
                            Text("${animal.idade} · ${animal.raca}", fontSize = 13.sp, color = MarromAcompanhamento)
                            Surface(color = VerdeAcompanhamento, shape = RoundedCornerShape(20.dp)) {
                                Text(animal.situacao, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = MarromAcompanhamento, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Text("Etapas da adoção", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextoAcompanhamento)
                // Representa a progressao atual, sem inventar datas ou registros de etapas passadas.
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    etapas.forEachIndexed { indice, etapa ->
                        LinhaEtapaAdocao(etapa, indice < processo.etapa.ordinal,
                            etapa == processo.etapa, indice != etapas.lastIndex)
                    }
                }

                Surface(color = Color.White, shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, BordaAcompanhamento)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Surface(color = CremeAcompanhamento, shape = RoundedCornerShape(12.dp)) {
                            Text("Próxima ação", Modifier.fillMaxWidth().padding(14.dp), fontWeight = FontWeight.Bold,
                                color = MarromAcompanhamento, fontSize = 18.sp)
                        }
                        Text(processo.proximaAcao, color = TextoAcompanhamento, fontSize = 15.sp)
                        Text(processo.pessoa, color = MarromAcompanhamento, fontSize = 13.sp)
                        // A mudanca de etapa atualiza a colecao compartilhada e os filtros da lista.
                        Button(onClick = { escolherEtapa = true }, enabled = !adotado,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LaranjaAcompanhamento, contentColor = TextoAcompanhamento)) {
                            Text(if (adotado) "Em acompanhamento" else "Alterar etapa", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(onClick = { confirmarAdocao = true }, enabled = !adotado,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BordaAcompanhamento)) {
                            Text(if (adotado) "Adoção confirmada" else "Marcar como adotado", color = MarromAcompanhamento)
                        }
                        Text(if (adotado) "Continue acompanhando a adaptação no novo lar." else "Confirme a adoção antes de atualizar a ficha.",
                            fontSize = 12.sp, color = MarromAcompanhamento)
                        TextButton(onClick = aoVerFicha, modifier = Modifier.fillMaxWidth()) { Text("Ver ficha do bichinho", color = MarromAcompanhamento) }
                    }
                }
                Text("Demonstração com dados locais. Nenhuma mensagem será enviada.", fontSize = 12.sp, color = MarromAcompanhamento)
            }
        }
    }

    // Permite corrigir etapas anteriores; acompanhamento exige confirmar a adocao.
    if (escolherEtapa) AlertDialog(onDismissRequest = { escolherEtapa = false },
        title = { Text("Alterar etapa") },
        text = {
            Column {
                etapas.forEach { etapa ->
                    TextButton(onClick = {
                        escolherEtapa = false
                        if (etapa == EtapaAdocao.ACOMPANHAMENTO && !adotado) confirmarAdocao = true
                        else aoAlterarEtapa(etapa)
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (etapa == processo.etapa) "${etapa.titulo} · atual" else etapa.titulo,
                            Modifier.fillMaxWidth(), color = MarromAcompanhamento)
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = { escolherEtapa = false }) { Text("Cancelar") } }, containerColor = FundoAcompanhamento)
    // Uma confirmacao explicita atualiza o animal e seu processo ao mesmo tempo.
    if (confirmarAdocao) AlertDialog(onDismissRequest = { confirmarAdocao = false },
        title = { Text("Confirmar adoção de ${animal.nome}?") },
        text = { Text("A ficha será marcada como Adotado e o processo passará para Acompanhamento. Confirme somente depois da adoção realizada.") },
        confirmButton = { TextButton(onClick = { confirmarAdocao = false; aoConfirmarAdocao() }) { Text("Confirmar adoção", color = MarromAcompanhamento) } },
        dismissButton = { TextButton(onClick = { confirmarAdocao = false }) { Text("Cancelar") } }, containerColor = FundoAcompanhamento)
    aviso?.let { mensagem ->
        AlertDialog(onDismissRequest = { aviso = null }, text = { Text(mensagem) },
            confirmButton = { TextButton(onClick = { aviso = null }) { Text("Fechar") } })
    }
}

// Desenha cada etapa com texto acessivel e marcadores de progresso no padrao do prototipo.
@Composable
private fun LinhaEtapaAdocao(etapa: EtapaAdocao, anterior: Boolean, atual: Boolean, temProxima: Boolean) {
    val descricao = when (etapa) {
        EtapaAdocao.INTERESSE -> "Registro do interesse na adoção."
        EtapaAdocao.ENTREVISTA -> "Conversa para conhecer o perfil e a rotina."
        EtapaAdocao.VISITA -> "Visita ao abrigo para conhecer o animal."
        EtapaAdocao.ACOMPANHAMENTO -> "Contato após a adoção para acompanhar a adaptação."
    }
    Surface(color = if (atual) Color(0xFFFFE8DA) else Color.Transparent, shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape,
                    color = when { atual -> LaranjaAcompanhamento; anterior -> Color(0xFF45854A); else -> Color.White },
                    border = BorderStroke(2.dp, if (atual || anterior) Color.Transparent else BordaAcompanhamento)) {
                    Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        Text(when { anterior -> "✓"; atual -> "●"; else -> "" }, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                if (temProxima) Box(Modifier.padding(top = 5.dp).width(2.dp).height(42.dp)
                    .background(if (anterior) LaranjaAcompanhamento else BordaAcompanhamento))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(etapa.titulo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextoAcompanhamento)
                if (atual) Text("Etapa atual", color = MarromAcompanhamento, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(descricao, color = MarromAcompanhamento, fontSize = 13.sp)
            }
        }
    }
}

// Mostra a entrevista da Lua no Preview sem precisar iniciar a Activity.
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewAcompanharAdocao() {
    MaterialTheme {
        val animal = remember { AnimalExemplo("Lua", "2 anos", "Em adoção", raca = "SRD") }
        TelaAcompanharAdocao(animal, ProcessoAdocao("preview", animal.id, "Pessoa de exemplo",
            EtapaAdocao.ENTREVISTA, "Combine a visita com a pessoa interessada."),
            aoVoltar = {}, aoInicio = {}, aoAnimais = {}, aoAdicionar = {}, aoAgenda = {},
            aoVerFicha = {}, aoAlterarEtapa = {}, aoConfirmarAdocao = {})
    }
}