package com.example.gerenciacao

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

// Verifica a agenda e sua integracao com as telas que compartilham o menu.
class AgendaTest {
    // Usa a Activity real do aplicativo para conferir os destinos do menu inferior.
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    // Abre o inicio com credenciais ficticias validas.
    private fun abrirInicio() {
        compose.mainClock.advanceTimeBy(5_000)
        compose.waitForIdle()
        compose.onAllNodes(hasSetTextAction())[0].performScrollTo().performTextInput("teste@example.com")
        compose.onAllNodes(hasSetTextAction())[1].performScrollTo().performTextInput("Senha1234")
        compose.onNodeWithText("Acessar").performScrollTo().performClick()
    }

    // O ultimo texto Agenda corresponde ao menu, mesmo quando o titulo tambem esta visivel.
    private fun abrirAgenda() {
        val itens = compose.onAllNodesWithText("Agenda")
        itens[itens.fetchSemanticsNodes().lastIndex].performClick()
    }

    // Confirma os acessos pelo inicio, animais, acoes, formulario e ficha de um animal.
    @Test
    fun agendaAbrePelosMenusExistentes() {
        abrirInicio()
        compose.onNodeWithText("Ver todos").performClick()
        compose.onNodeWithText("Novembro 2026").assertIsDisplayed()
        compose.onNodeWithText("Animais").performClick()
        abrirAgenda()
        compose.onNodeWithText("Novembro 2026").assertIsDisplayed()
        compose.onNodeWithContentDescription("Adicionar").performClick()
        compose.onNodeWithText("Ações rápidas").assertIsDisplayed()
        abrirAgenda()
        compose.onNodeWithText("Novembro 2026").assertIsDisplayed()
        compose.onNodeWithText("Início").performClick()
        compose.onNodeWithText("Cadastrar\nanimal").performScrollTo().performClick()
        abrirAgenda()
        compose.onNodeWithText("Novembro 2026").assertIsDisplayed()
        compose.onNodeWithText("Animais").performClick()
        compose.onNodeWithText("Lua").performScrollTo().performClick()
        abrirAgenda()
        compose.onNodeWithText("Novembro 2026").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Olá, Helena!").assertIsDisplayed()
    }

    // Verifica a selecao, os compromissos, a virada de ano e a preservacao do mes.
    @Test
    fun calendarioFiltraCompromissosETrocaAno() {
        abrirInicio()
        abrirAgenda()
        compose.onNodeWithText("Vacina da Lua").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Visita ao Bento").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("8").performScrollTo().performClick()
        compose.onNodeWithText("Nenhum compromisso em 8/11/2026.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Ver todos").performClick()
        compose.onNodeWithText("Vacina da Lua").performScrollTo().performClick()
        compose.onNodeWithText("Compromisso de exemplo.", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Fechar").performClick()
        compose.onNodeWithText("›").performScrollTo().performClick()
        compose.onNodeWithText("Dezembro 2026").assertIsDisplayed()
        compose.onNodeWithText("›").performClick()
        compose.onNodeWithText("Janeiro 2027").assertIsDisplayed()
        compose.onNodeWithText("‹").performClick()
        compose.onNodeWithText("Dezembro 2026").assertIsDisplayed()
        compose.onNodeWithText("Animais").performClick()
        abrirAgenda()
        compose.onNodeWithText("Dezembro 2026").assertIsDisplayed()
    }
}