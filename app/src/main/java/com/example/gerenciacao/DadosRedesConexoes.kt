package com.example.gerenciacao

import java.text.Normalizer
import java.util.Locale

// Representa somente as informacoes publicas de outro abrigo, separadas dos dados do seu abrigo.
data class AbrigoRede(
    val id: String,
    val nome: String,
    val cidade: String,
    val bairro: String,
    val descricao: String,
    val animais: Int,
    val capacidade: Int,
    val distanciaExemploKm: Double?,
    val emailExemplo: String,
    val equipeExemplo: List<String>,
    val telefone: String = "Não informado",
    val endereco: String = "Endereço não cadastrado",
    val fotoRecurso: Int? = null
)

// Valores de exemplo; nao correspondem a uma busca real nem usam a localizacao do aparelho.
val AbrigosRedeExemplo = listOf(
    AbrigoRede("patas-unidas", "Patas Unidas", "São Paulo", "Vila Esperança",
        "Um exemplo de abrigo dedicado ao acolhimento e à preparação dos bichinhos para um novo lar.",
        18, 30, 2.4, "contato@patasunidas.example", listOf("Ana", "Pedro", "Marina")),
    AbrigoRede("cantinho-focinhos", "Cantinho dos Focinhos", "São Paulo", "Vila Maria",
        "Um exemplo de abrigo que acolhe bichinhos e acompanha os cuidados de cada um até a chegada de uma nova família.",
        25, 40, 4.7, "contato@cantinhodosfocinhos.example", listOf("Ana", "Bruno", "Clara", "Daniel")),
    AbrigoRede("lar-animal", "Abrigo Lar Animal", "Guarulhos", "Centro",
        "Um exemplo de equipe que organiza o acolhimento, os cuidados e as adoções dos bichinhos.",
        20, 20, 7.2, "contato@laranimal.example", listOf("Carla", "Rafael")),
    AbrigoRede("patas-felizes", "Patas Felizes", "São Paulo", "Santana",
        "Um exemplo de abrigo com uma rotina de cuidado e convivência para os bichinhos acolhidos.",
        12, 24, 9.1, "contato@patasfelizes.example", listOf("Paula", "João")),
    AbrigoRede("patinhas-bem", "Patinhas do Bem", "Osasco", "Centro",
        "Um exemplo de abrigo que reúne pessoas dedicadas a oferecer cuidado e encontrar novos lares.",
        16, 25, 12.8, "contato@patinhasdobem.example", listOf("Luiza", "Marcos"))
)

// Calcula a disponibilidade sem exibir vagas negativas quando a capacidade estiver excedida.
fun vagasRede(abrigo: AbrigoRede): Int = (abrigo.capacidade - abrigo.animais).coerceAtLeast(0)

// Normaliza a busca para que Sao encontre São e maiusculas nao alterem o resultado.
fun normalizarBuscaRede(texto: String): String = Normalizer.normalize(texto, Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).trim()

// Combina a aba, o texto, a cidade, as vagas e a distancia de demonstracao em uma unica busca.
fun filtrarRedes(
    abrigos: List<AbrigoRede>, conexoes: Set<String>, somenteConexoes: Boolean,
    busca: String, cidade: String, raioExemploKm: Int, somenteComVagas: Boolean, ordem: String
): List<AbrigoRede> {
    val termo = normalizarBuscaRede(busca)
    val resultado = abrigos.filter { abrigo ->
        (!somenteConexoes || abrigo.id in conexoes) &&
                (termo.isBlank() || normalizarBuscaRede("${abrigo.nome} ${abrigo.cidade} ${abrigo.bairro}").contains(termo)) &&
                (cidade == "Todas" || abrigo.cidade == cidade) &&
                (!somenteComVagas || vagasRede(abrigo) > 0) &&
                (raioExemploKm == 0 || abrigo.distanciaExemploKm?.let { it <= raioExemploKm } == true)
    }
    return when (ordem) {
        "Distância" -> resultado.sortedWith(compareBy<AbrigoRede> { it.distanciaExemploKm ?: Double.MAX_VALUE }.thenBy { normalizarBuscaRede(it.nome) })
        else -> resultado.sortedBy { normalizarBuscaRede(it.nome) }
    }
}

// Formata somente o valor ilustrativo recebido, sem fingir calculo de GPS.
fun distanciaRede(abrigo: AbrigoRede): String = abrigo.distanciaExemploKm?.let {
    String.format(Locale.forLanguageTag("pt-BR"), "%.1f km · exemplo", it)
} ?: "Distância indisponível"