package com.example.gerenciacao

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.font.FontWeight

// Edita os dados locais do abrigo e calcula a ocupacao pelas fichas existentes.
@Composable
fun TelaMeuAbrigo(ctx: ContextoAbrigo) {
    val original = ctx.dados.abrigo
    var nome by rememberSaveable { mutableStateOf(original.nome) }
    var telefone by rememberSaveable { mutableStateOf(original.telefone) }
    var rede by rememberSaveable { mutableStateOf(original.redeSocial) }
    var capacidade by rememberSaveable { mutableStateOf(original.capacidade.toString()) }
    var aviso by rememberSaveable { mutableStateOf<String?>(null) }
    val ocupacao = ctx.animais.count { !it.arquivado && it.situacao != "Adotado" }
    val alterado = nome != original.nome || telefone != original.telefone || rede != original.redeSocial || capacidade != original.capacidade.toString()
    BaseRestante(ctx, "Meu abrigo", alterado = alterado) {
        if (!administradorAbrigo(ctx.dados)) Text("Esta demonstração exige a função Administrador para editar o abrigo.")
        else {
            CartaoRestante(CremeRestante) { Text(original.nome, fontWeight = FontWeight.Bold); Text("Informações do lugar que acolhe") }
            CampoRestante("Nome do abrigo", nome, { nome = it })
            CampoRestante("Telefone · opcional", telefone, { telefone = it }, dica = "DDD e número")
            CampoRestante("Rede social · opcional", rede, { rede = it }, dica = "@nomedoabrigo")
            CampoRestante("Capacidade · número de espaços", capacidade, { capacidade = it })
            CartaoRestante(VerdeRestante) {
                Text("$ocupacao bichinhos acolhidos", fontWeight = FontWeight.Bold)
                val limite = capacidade.toIntOrNull()
                Text(if (limite != null && limite >= ocupacao) "${limite - ocupacao} espaços disponíveis" else "A capacidade deve comportar os bichinhos acolhidos.")
                Text("Ocupação calculada pelas fichas ativas, excluindo adotados e arquivados.")
            }
            // Revalida antes de salvar para nao permitir capacidade inferior a ocupacao.
            BotaoRestante("Salvar informações") {
                val limite = capacidade.toIntOrNull()
                val digitos = telefone.filter(Char::isDigit)
                when {
                    !administradorAbrigo(ctx.dados) -> aviso = "Acesso de administração necessário."
                    nome.trim().length < 3 -> aviso = "Informe o nome do abrigo com pelo menos 3 caracteres."
                    telefone.isNotBlank() && digitos.length !in 10..11 -> aviso = "Informe um telefone com DDD, totalizando 10 ou 11 dígitos."
                    limite == null || limite !in 1..100000 || limite < ocupacao -> aviso = "Informe uma capacidade válida, igual ou maior que a ocupação atual."
                    else -> {
                        ctx.atualizar(ctx.dados.copy(abrigo = InformacoesAbrigo(nome.trim(), telefone.trim(), rede.trim(), limite)))
                        aviso = "Informações salvas nesta sessão local."
                    }
                }
            }
        }
    }
    aviso?.let { MensagemRestante(it) { aviso = null } }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaMeuAbrigo() {
    androidx.compose.material3.MaterialTheme { TelaMeuAbrigo(contextoPreviewRestante()) }
}