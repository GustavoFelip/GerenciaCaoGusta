package com.example.gerenciacao

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

// Exercita a navegacao real e as validacoes enquanto o aplicativo usa dados ficticios.
class NavegacaoLoginTest {
    // Inicia a Activity real para testar os mesmos caminhos usados no aplicativo.
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    // Aguarda o fim da animacao antes de interagir com o login.
    private fun abrirLogin() {
        compose.mainClock.advanceTimeBy(5_000)
        compose.waitForIdle()
        compose.onNodeWithText("Acesse sua conta").assertIsDisplayed()
    }

    // Preenche os campos visiveis sem depender das coordenadas do emulador.
    private fun preencherCampos(dados: List<String>) {
        val campos = compose.onAllNodes(hasSetTextAction())
        dados.forEachIndexed { indice, valor ->
            campos[indice].performScrollTo().performTextInput(valor)
        }
    }

    // Abre o cadastro e preenche um conjunto de dados para cada cenario de validacao.
    private fun preencherCadastro(
        telefone: String = "11999999999",
        email: String = "teste@example.com",
        confirmacao: String = "Senha1234"
    ) {
        abrirLogin()
        compose.onNodeWithText("Criar minha conta").performClick()
        preencherCampos(listOf("Pessoa Teste", telefone, email, "Senha1234", confirmacao))
        compose.onNodeWithText("Cadastrar").performScrollTo().performClick()
    }

    // Confirma que um cadastro valido volta ao login e permite acessar o inicio sem banco.
    @Test
    fun cadastroValidoVoltaAoLoginEAcessaInicio() {
        preencherCadastro()
        compose.onNodeWithText("Acesse sua conta").assertIsDisplayed()
        preencherCampos(listOf("ficticio@example.com", "Senha1234"))
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
        compose.onNodeWithText("Olá, Helena!").assertIsDisplayed()
        compose.onNodeWithText("Vacina da Lua").assertIsDisplayed()
        compose.onNodeWithText("Cadastrar\nanimal").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Registrar\ndoação").assertIsDisplayed()
        compose.onNodeWithText("Registrar\ndespesa").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ver\nbichinhos").assertIsDisplayed()
        compose.onNodeWithText("Agenda").performClick()
        compose.onNodeWithText("Novembro 2026").assertIsDisplayed()
        compose.onNodeWithText("Início").performClick()
        compose.onNodeWithContentDescription("Perfil").performScrollTo().performClick()
        compose.onNodeWithText("Sair").performClick()
        compose.onNodeWithText("Acesse sua conta").assertIsDisplayed()
    }

    // Impede que um telefone composto de letras conclua o cadastro.
    @Test
    fun cadastroRejeitaTelefoneComLetras() {
        preencherCadastro(telefone = "telefoneinvalido")
        compose.onNodeWithText("Cadastrar").assertIsDisplayed()
        compose.onNodeWithText("Acesse sua conta").assertDoesNotExist()
    }

    // Mantem a verificacao do formato de e-mail no formulario de cadastro.
    @Test
    fun cadastroRejeitaEmailInvalido() {
        preencherCadastro(email = "emailinvalido")
        compose.onNodeWithText("Cadastrar").assertIsDisplayed()
        compose.onNodeWithText("Acesse sua conta").assertDoesNotExist()
    }

    // Impede que senhas diferentes concluam o cadastro.
    @Test
    fun cadastroRejeitaSenhasDiferentes() {
        preencherCadastro(confirmacao = "OutraSenha123")
        compose.onNodeWithText("Cadastrar").assertIsDisplayed()
        compose.onNodeWithText("Acesse sua conta").assertDoesNotExist()
    }

    // Impede o acesso ao inicio quando o e-mail do login tem formato invalido.
    @Test
    fun loginRejeitaEmailInvalido() {
        abrirLogin()
        preencherCampos(listOf("emailinvalido", "Senha1234"))
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
        compose.onNodeWithText("Início").assertDoesNotExist()
        compose.onNodeWithText("Acessar").assertIsDisplayed()
    }

    // Mantem o requisito de oito caracteres tambem para as senhas ficticias.
    @Test
    fun loginRejeitaSenhaCurta() {
        abrirLogin()
        preencherCampos(listOf("teste@example.com", "123"))
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
        compose.onNodeWithText("Início").assertDoesNotExist()
        compose.onNodeWithText("Acessar").assertIsDisplayed()
    }

    // Impede que o login vazio abra a tela inicial.
    @Test
    fun loginRejeitaCamposVazios() {
        abrirLogin()
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
        compose.onNodeWithText("Início").assertDoesNotExist()
        compose.onNodeWithText("Acessar").assertIsDisplayed()
    }

    // Preserva a navegacao da recuperacao de senha e o retorno ao login.
    @Test
    fun recuperarSenhaEVoltarAoLogin() {
        abrirLogin()
        compose.onNodeWithText("Esqueci minha senha").performClick()
        compose.onNodeWithText("Recuperar senha").assertIsDisplayed()
        compose.onNodeWithText("Voltar para entrar").performClick()
        compose.onNodeWithText("Acesse sua conta").assertIsDisplayed()
    }

    // Preserva a volta do cadastro pelo botao Voltar do Android.
    @Test
    fun criarContaEVoltarAoLogin() {
        abrirLogin()
        compose.onNodeWithText("Criar minha conta").performClick()
        compose.onNodeWithText("Criar sua conta").assertIsDisplayed()
        compose.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        compose.onNodeWithText("Acesse sua conta").assertIsDisplayed()
    }

    // Confirma pesquisa combinada com filtro, resultado vazio e abertura de uma ficha.
    @Test
    fun animaisPesquisaFiltraEAbreFicha() {
        abrirLogin()
        preencherCampos(listOf("teste@example.com", "Senha1234"))
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
        compose.onNodeWithText("Animais").performClick()
        compose.onNodeWithText("Bichinhos").assertIsDisplayed()
        compose.onNodeWithText("4 fichas").assertIsDisplayed()
        compose.onNodeWithText("Lua").assertIsDisplayed()
        compose.onNodeWithText("Tico").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Em adoção").performScrollTo().performClick()
        compose.onNodeWithText("1 ficha").assertIsDisplayed()
        compose.onNodeWithText("Bento").assertDoesNotExist()
        compose.onNodeWithText("Lua").performClick()
        compose.onNodeWithText("Ficha do bichinho").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Adotados").performClick()
        compose.onNodeWithText("0 fichas").assertIsDisplayed()
        compose.onNodeWithText("Nenhum bichinho encontrado.").assertIsDisplayed()
        compose.onNodeWithText("Todos").performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("bEn")
        compose.onNodeWithText("Bento").assertIsDisplayed()
        compose.onNodeWithText("1 ficha").assertIsDisplayed()
        compose.onNodeWithText("Lua").assertDoesNotExist()
        compose.onAllNodes(hasSetTextAction())[0].performTextClearance()
        compose.onNodeWithText("4 fichas").assertIsDisplayed()
        compose.onNodeWithText("Início").performClick()
        compose.onNodeWithText("Olá, Helena!").assertIsDisplayed()
    }

    // Garante que o atalho Ver bichinhos usa a mesma tela e permite voltar pelo Android.
    @Test
    fun atalhoBichinhosAbreAnimaisEVolta() {
        abrirLogin()
        preencherCampos(listOf("teste@example.com", "Senha1234"))
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
        compose.onNodeWithText("Ver\nbichinhos").performScrollTo().performClick()
        compose.onNodeWithText("Bichinhos").assertIsDisplayed()
        compose.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        compose.onNodeWithText("Olá, Helena!").assertIsDisplayed()
    }

    // Valida erros, opcoes e inclusao local, inclusive quando ja existe um animal com o nome.
    @Test
    fun novaFichaValidaEAdicionaAnimalNaDemonstracao() {
        abrirLogin()
        preencherCampos(listOf("teste@example.com", "Senha1234"))
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
        compose.onNodeWithText("Cadastrar\nanimal").performScrollTo().performClick()
        compose.onNodeWithText("Nova ficha").assertIsDisplayed()
        compose.onNodeWithText("Continuar").performScrollTo().performClick()
        compose.onNodeWithText("Revisar ficha").assertDoesNotExist()
        compose.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextInput("Lua")
        compose.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextInput("Sem raça definida")
        compose.onAllNodes(hasSetTextAction())[2].performScrollTo().performTextInput("dois")
        compose.onNodeWithText("Continuar").performScrollTo().performClick()
        compose.onNodeWithText("Informe uma idade válida. Ex.: 2 anos ou 6 meses.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Revisar ficha").assertDoesNotExist()
        compose.onAllNodes(hasSetTextAction())[2].performScrollTo().performTextClearance()
        compose.onAllNodes(hasSetTextAction())[2].performTextInput("6 meses")
        compose.onNodeWithText("Macho").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithText("Não").performScrollTo().performClick().assertIsSelected()
        compose.onNodeWithText("Continuar").performScrollTo().performClick()
        compose.onNodeWithText("Revisar ficha").assertIsDisplayed()
        compose.onNodeWithText("Adicionar à lista").performClick()
        compose.onNodeWithText("Bichinhos").assertIsDisplayed()
        compose.onNodeWithText("5 fichas").assertIsDisplayed()
        compose.onNodeWithText("6 meses · No abrigo").performScrollTo().assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("5 fichas").performScrollTo().assertIsDisplayed()
        // O ID permite abrir e editar a segunda Lua sem alterar a Lua original.
        compose.onNodeWithText("6 meses · No abrigo").performScrollTo().performClick()
        compose.onNodeWithText("6 meses · Sem raça definida · Macho · Não castrado").assertIsDisplayed()
        compose.onNodeWithText("Editar ficha").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextClearance()
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("Lua pequena")
        compose.onNodeWithText("Continuar").performScrollTo().performClick()
        compose.onNodeWithText("Salvar alterações").performClick()
        compose.onNodeWithText("5 fichas").assertIsDisplayed()
        compose.onNodeWithText("Lua pequena").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Lua").performScrollTo().assertIsDisplayed()
    }

    // Confirma os cinco atalhos, o destino do cadastro e o retorno a tela de origem.
    @Test
    fun botaoMaisAbreAcoesRapidas() {
        abrirLogin()
        preencherCampos(listOf("teste@example.com", "Senha1234"))
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Adicionar").performClick()
        compose.onNodeWithText("Ações rápidas").assertIsDisplayed()
        compose.onNodeWithText("O que deseja adicionar?").assertIsDisplayed()
        compose.onNodeWithText("Cadastrar bichinho").assertIsDisplayed()
        compose.onNodeWithText("Registrar doação").assertIsDisplayed()
        compose.onNodeWithText("Registrar despesa").assertIsDisplayed()
        compose.onNodeWithText("Agendar cuidado").assertIsDisplayed()
        compose.onNodeWithText("Criar postagem").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Registrar doação").performScrollTo().performClick()
        compose.onNodeWithText("A tela de registrar doações ainda será criada.").assertIsDisplayed()
        compose.onNodeWithText("Cadastrar bichinho").performScrollTo().performClick()
        compose.onNodeWithText("Nova ficha").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Ações rápidas").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Olá, Helena!").assertIsDisplayed()
        compose.onNodeWithText("Animais").performClick()
        compose.onNodeWithContentDescription("Adicionar").performClick()
        compose.onNodeWithText("Ações rápidas").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Bichinhos").assertIsDisplayed()
        compose.onNodeWithContentDescription("Adicionar").performClick()
        compose.onNodeWithText("Cadastrar bichinho").performClick()
        compose.onNodeWithText("Nova ficha").assertIsDisplayed()
        compose.onNodeWithContentDescription("Adicionar").performClick()
        compose.onNodeWithText("Ações rápidas").assertIsDisplayed()
        compose.onNodeWithText("Animais").performClick()
        compose.onNodeWithText("4 fichas").assertIsDisplayed()
    }

    // Reutiliza a mesma tela para os quatro IDs e conserva o animal ao recriar a Activity.
    @Test
    fun cadaAnimalAbreSuaPropriaFicha() {
        abrirLogin()
        preencherCampos(listOf("teste@example.com", "Senha1234"))
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
        compose.onNodeWithText("Animais").performClick()
        listOf("Lua", "Bento", "Mel", "Tico").forEach { nome ->
            compose.onNodeWithText(nome).performScrollTo().performClick()
            compose.onNodeWithText("Ficha do bichinho").assertIsDisplayed()
            compose.onNodeWithText(nome).assertIsDisplayed()
            compose.onNodeWithText(if (nome == "Lua") "Sobre a Lua" else "Sobre $nome").assertIsDisplayed()
            if (nome == "Lua") {
                compose.onNodeWithText("2 anos · SRD · Fêmea · Castrada").assertIsDisplayed()
                compose.onNodeWithText("Carinhosa, brincalhona e gosta de companhia.").assertIsDisplayed()
                compose.activityRule.scenario.recreate()
                compose.onNodeWithText("Lua").assertIsDisplayed()
            }
            compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithText("Bichinhos").assertIsDisplayed()
        }
    }
}
