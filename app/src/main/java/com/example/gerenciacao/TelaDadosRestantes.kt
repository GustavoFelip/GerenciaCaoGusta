package com.example.gerenciacao

import java.io.Serializable
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

// Armazena datas e horarios como texto validado, compativel com Android 7 e posteriores.
data class CompromissoAbrigo(val id: String = UUID.randomUUID().toString(), val titulo: String,
                             val tipo: String, val data: String, val horario: String, val animalId: String? = null,
                             val local: String = "", val observacoes: String = "") : Serializable

// Produtos e recebimentos de bichinhos nao possuem valor em reais no resumo financeiro.
enum class NaturezaAbrigo { DINHEIRO, PRODUTOS, BICHINHO, DESPESA }
data class LancamentoAbrigo(val id: String = UUID.randomUUID().toString(), val descricao: String,
                            val natureza: NaturezaAbrigo, val data: String, val centavos: Long = 0,
                            val quantidade: String = "", val categoria: String = "", val animalId: String? = null,
                            val observacoes: String = "") : Serializable

// Ofertas de exemplo nao incluem links reais nem promessas comerciais.
data class OfertaAbrigo(val id: String = UUID.randomUUID().toString(), val titulo: String,
                        val categoria: String, val parceiro: String, val descricao: String, val link: String = "",
                        val tipo: String = "Produto", val centavos: Long? = null, val imagemUri: String? = null) : Serializable

// Os cargos abaixo servem apenas a demonstracao local, sem autenticar usuarios.
enum class FuncaoAbrigo(val titulo: String) { ADMIN("Administrador"), CUIDADOR("Cuidador"), VOLUNTARIO("Voluntário") }
data class MembroAbrigo(val id: String, val nome: String, val funcao: FuncaoAbrigo) : Serializable
data class ConviteAbrigo(val id: String, val email: String, val funcao: FuncaoAbrigo, val codigo: String) : Serializable
data class InformacoesAbrigo(val nome: String = "Abrigo Esperança", val telefone: String = "",
                             val redeSocial: String = "", val capacidade: Int = 60) : Serializable

// Uma unica colecao compartilhada mantem todas as novas telas coerentes durante a sessao.
data class DadosRestantes(
    val compromissos: List<CompromissoAbrigo> = emptyList(),
    val agendaDataAlvo: String? = null,
    val lancamentos: List<LancamentoAbrigo> = emptyList(),
    val ofertas: List<OfertaAbrigo> = emptyList(),
    val abrigo: InformacoesAbrigo = InformacoesAbrigo(),
    val membros: List<MembroAbrigo> = listOf(MembroAbrigo("helena", "Helena", FuncaoAbrigo.ADMIN),
        MembroAbrigo("ana", "Ana", FuncaoAbrigo.CUIDADOR), MembroAbrigo("paulo", "Paulo", FuncaoAbrigo.VOLUNTARIO)),
    val usuarioLocalId: String = "helena",
    val convites: List<ConviteAbrigo> = emptyList(),
    val compromissoId: String? = null, val editarCompromissoId: String? = null,
    val ofertaId: String? = null, val membroId: String? = null,
    val modeloPostagem: String = "Procura um lar"
) : Serializable

// Inicializa exemplos explicitos sem fabricar relacoes para bichinhos cadastrados depois.
fun dadosRestantesExemplo(animais: List<AnimalExemplo>): DadosRestantes {
    val lua = animais.firstOrNull { it.nome == "Lua" }?.id
    val bento = animais.firstOrNull { it.nome == "Bento" }?.id
    val entradas = listOf(60000L, 80000L, 50000L, 90000L)
    val saidas = listOf(30000L, 40000L, 32000L, 40000L)
    val dias = listOf("03", "10", "17", "24")
    val movimentos = entradas.indices.flatMap { i -> listOf(
        LancamentoAbrigo(descricao = "Doação de exemplo ${i + 1}", natureza = NaturezaAbrigo.DINHEIRO,
            data = "${dias[i]}/11/2026", centavos = entradas[i]),
        LancamentoAbrigo(descricao = "Despesa de exemplo ${i + 1}", natureza = NaturezaAbrigo.DESPESA,
            data = "${dias[i]}/11/2026", centavos = saidas[i], categoria = "Outros"))
    } + LancamentoAbrigo(descricao = "Ração recebida", natureza = NaturezaAbrigo.PRODUTOS,
        data = "07/11/2026", quantidade = "20 kg")
    return DadosRestantes(compromissos = listOf(
        CompromissoAbrigo(titulo = "Vacina da Lua", tipo = "Cuidados", data = "07/11/2026", horario = "14:00", animalId = lua,
            local = "Clínica parceira", observacoes = "Levar a carteirinha de vacinação."),
        CompromissoAbrigo(titulo = "Visita ao Bento", tipo = "Visitas", data = "07/11/2026", horario = "16:00", animalId = bento)),
        lancamentos = movimentos, ofertas = listOf(
            OfertaAbrigo(titulo = "Ração para abrigo", categoria = "Alimentação", parceiro = "Marca parceira",
                descricao = "Consulte tamanhos, disponibilidade e valores com o parceiro."),
            OfertaAbrigo(titulo = "Consulta veterinária", categoria = "Saúde", parceiro = "Clínica parceira", tipo = "Serviço",
                descricao = "Consulte a região atendida e as condições do serviço."),
            OfertaAbrigo(titulo = "Banho e tosa", categoria = "Serviços", parceiro = "Atendimento local", tipo = "Serviço",
                descricao = "Consulte a disponibilidade e as condições do serviço."),
            OfertaAbrigo(titulo = "Medicamentos", categoria = "Saúde", parceiro = "Produtos veterinários",
                descricao = "Consulte as condições com o parceiro. Não substitui orientação veterinária.")))
}

// Rejeita datas inexistentes e texto extra, sem normalizar 31/02 para outro mes.
fun dataValidaAbrigo(texto: String): Boolean {
    if (!Regex("[0-9]{2}/[0-9]{2}/[0-9]{4}").matches(texto)) return false
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }
    val posicao = ParsePosition(0)
    return formato.parse(texto, posicao) != null && posicao.index == texto.length && texto.takeLast(4).toInt() in 1900..2200
}

// Retorna uma chave ordenavel sem depender de java.time ou de desugaring.
fun chaveDataAbrigo(data: String): String = data.takeLast(4) + data.substring(3, 5) + data.take(2)
fun hojeAbrigo(): String = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).format(java.util.Date())

// Recebe reais sem separador de milhares; usa inteiros para evitar arredondamento binario.
fun centavosAbrigo(texto: String): Long? {
    if (!Regex("[0-9]{1,9}(,[0-9]{1,2})?").matches(texto.trim())) return null
    return runCatching { BigDecimal(texto.trim().replace(',', '.')).movePointRight(2).longValueExact() }
        .getOrNull()?.takeIf { it > 0 }
}

// Converte o valor monetario para a apresentacao brasileira.
fun reaisAbrigo(valor: Long): String = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    .format(BigDecimal.valueOf(valor, 2))

// Agrupa registros pela mesma competencia usada no resumo e no relatorio.
fun doPeriodoAbrigo(data: String, mes: Int, ano: Int): Boolean =
    dataValidaAbrigo(data) && data.substring(3, 5).toInt() == mes && data.takeLast(4).toInt() == ano

// Verifica somente a permissao demonstrativa da pessoa local, sem substituir uma API segura.
fun administradorAbrigo(dados: DadosRestantes): Boolean = dados.membros.any { it.id == dados.usuarioLocalId && it.funcao == FuncaoAbrigo.ADMIN }