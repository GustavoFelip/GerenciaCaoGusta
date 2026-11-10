package com.example.gerenciacao

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Reune as cinco acoes do botao mais e encaminha o cadastro para a Nova ficha.
@Composable
fun TelaAdicionar(
    aoVoltar: () -> Unit,
    aoInicio: () -> Unit,
    // Abre a agenda pelo menu compartilhado.
    aoAgenda: () -> Unit,
    aoAnimais: () -> Unit,
    aoCadastrarBichinho: () -> Unit,
    aoRegistrarDoacao: () -> Unit,
    aoRegistrarDespesa: () -> Unit,
    aoAgendarCuidado: () -> Unit,
    aoCriarPostagem: () -> Unit,
    aoParcerias: () -> Unit,
    aoDivulgacao: () -> Unit
) {
    // Mantem as cores e o comportamento de avisos das outras telas do aplicativo.
    val fundo = Color(0xFFFFFCF7)
    val texto = Color(0xFF38291F)
    val marrom = Color(0xFF783E19)
    val creme = Color(0xFFFFEFC5)
    val amarelo = Color(0xFFFFB414)
    val avisos = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()
    val avisar: (String) -> Unit = { mensagem ->
        escopo.launch {
            avisos.currentSnackbarData?.dismiss()
            avisos.showSnackbar(mensagem)
        }
    }
    BackHandler(onBack = aoVoltar)

    // Fixa o menu inferior e deixa os cartoes rolarem em aparelhos menores.
    Column(Modifier.fillMaxSize().background(fundo)) {
        // A cor laranja pertence ao cabecalho compartilhado.
        MolduraPadraoApp(
            modifier = Modifier.weight(1f), containerColor = fundo,
            snackbarHost = { SnackbarHost(avisos) },
            bottomBar = {
                MenuInferiorInicio(avisar, selecionado = "", aoInicio = aoInicio, aoAnimais = aoAnimais, aoAdicionar = {}, aoAgenda = aoAgenda)
            }
        ) { espacos ->
            Column(
                Modifier.fillMaxSize().padding(espacos).verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp).padding(top = 22.dp, bottom = 28.dp)
            ) {
                // Repete o cabecalho da referencia com o logotipo e o sino.
                // O cabecalho compartilhado agora apresenta o perfil da pessoa.
                TituloPaginaPadrao("Ações rápidas")
                Spacer(Modifier.height(28.dp))
                Text("O que deseja adicionar?", fontSize = 18.sp, lineHeight = 22.sp,
                    color = texto, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                Text("Escolha uma ação para organizar sua rotina.", fontSize = 13.sp,
                    lineHeight = 18.sp, color = Color(0xFF7E7066))
                Spacer(Modifier.height(26.dp))

                // O cadastro de bichinhos ja possui formulario e abre diretamente a Nova ficha.
                AcaoAdicionar("Cadastrar bichinho", "Registre um animal no abrigo",
                    R.drawable.inicio_pata, creme, aoCadastrarBichinho)
                Spacer(Modifier.height(12.dp))

                // Os atalhos abrem os formularios locais das novas entregas.
                AcaoAdicionar("Registrar doação", "Anote o que o abrigo recebeu",
                    R.drawable.adicionar_doacao, Color(0xFFE1F3E8)) {
                    aoRegistrarDoacao()
                }
                Spacer(Modifier.height(12.dp))
                AcaoAdicionar("Registrar despesa", "Organize os gastos",
                    R.drawable.inicio_recibo, Color(0xFFFFEFE2)) {
                    aoRegistrarDespesa()
                }
                Spacer(Modifier.height(12.dp))
                AcaoAdicionar("Agendar cuidado", "Vacinas, visitas e serviços",
                    R.drawable.inicio_calendario, creme) {
                    aoAgendarCuidado()
                }
                Spacer(Modifier.height(12.dp))
                AcaoAdicionar("Criar postagem", "Divulgue um bichinho",
                    R.drawable.adicionar_postagem, Color(0xFFE1F3E8)) {
                    aoCriarPostagem()
                }
                Spacer(Modifier.height(16.dp))
                // Mantem os cinco atalhos originais e acrescenta acesso aos outros modulos.
                TextButton(onClick = aoParcerias) { Text("Parcerias", color = marrom) }
                TextButton(onClick = aoDivulgacao) { Text("Modelos de divulgação", color = marrom) }
            }
        }
    }
}

// Desenha cada container com icone, titulo, descricao e seta, como na imagem recebida.
@Composable
private fun AcaoAdicionar(
    titulo: String,
    descricao: String,
    icone: Int,
    fundoIcone: Color,
    aoClicar: () -> Unit
) {
    Surface(
        onClick = aoClicar, modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(20.dp)),
        color = Color.White, shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFEADFD2))
    ) {
        Row(Modifier.heightIn(min = 84.dp).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(fundoIcone, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center) {
                Icon(painterResource(icone), null, Modifier.size(24.dp), tint = Color(0xFF783E19))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontSize = 17.sp, lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFF38291F))
                Spacer(Modifier.height(6.dp))
                Text(descricao, fontSize = 12.sp, lineHeight = 16.sp, color = Color(0xFF7E7066))
            }
            Spacer(Modifier.width(8.dp))
            Icon(painterResource(R.drawable.inicio_seta), null, Modifier.size(15.dp), tint = Color(0xFF783E19))
        }
    }
}