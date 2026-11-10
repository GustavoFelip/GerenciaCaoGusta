package com.example.gerenciacao

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

// Apresenta os modelos e encaminha a escolha para o editor da postagem.
@Composable
fun TelaDivulgacao(ctx: ContextoAbrigo) {
    BaseRestante(ctx, "Divulgação") {
        Text("Ajude um bichinho a encontrar um lar", fontWeight = FontWeight.Bold, color = TextoRestante)
        Text("Crie publicações com os dados da ficha e um modelo pronto.", color = MarromRestante)
        listOf("Procura um lar" to "Apresente um bichinho para adoção", "Foi adotado!" to "Compartilhe uma boa notícia",
            "Conheça o bichinho" to "Conte a história de quem você acolhe").forEach { (modelo, ajuda) ->
            AtalhoRestante(modelo, ajuda, if (modelo == "Foi adotado!") VerdeRestante else CremeRestante) {
                ctx.atualizar(ctx.dados.copy(modeloPostagem = modelo))
                ctx.abrir("criar_postagem")
            }
        }
        CartaoRestante {
            Text("Como funciona", fontWeight = FontWeight.Bold)
            Text("1. Escolha o bichinho")
            Text("2. Personalize o modelo")
            Text("3. Revise e compartilhe")
        }
        BotaoRestante("Criar postagem") { ctx.abrir("criar_postagem") }
        Text("Você escolhe onde compartilhar. O aplicativo não publica automaticamente em redes sociais.")
    }
}

// Exibe esta tela no editor sem executar a Activity ou conectar banco de dados.
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTelaDivulgacao() {
    androidx.compose.material3.MaterialTheme { TelaDivulgacao(contextoPreviewRestante()) }
}