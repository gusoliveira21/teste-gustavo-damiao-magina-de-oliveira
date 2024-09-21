package com.aiko.domain.model

import com.squareup.moshi.Json

data class Linha(
    @Json(name = "cl") val codigoLinha: Int?,
    @Json(name = "lc") val circular: Boolean?,
    @Json(name = "lt") val letreiroNumerico: String?,
    @Json(name = "sl") val sentido: Int?,
    @Json(name = "tl") val tipoLinha: Int?,
    @Json(name = "tp") val terminalPrincipal: String?,
    @Json(name = "ts") val terminalSecundario: String?
)

data class Parada(
    @Json(name = "cp") val codigoParada: Int?,
    @Json(name = "np") val nomeParada: String?,
    @Json(name = "ed") val enderecoParada: String?,
    @Json(name = "py") val latitude: Double?,
    @Json(name = "px") val longitude: Double?
)

data class Previsao(
    @Json(name = "hr") val horarioReferencia: String?,
    @Json(name = "p") val pontoParada: PontoDeParada?
)

data class PontoDeParada(
    @Json(name = "cp") val codigoParada: Int?,
    @Json(name = "np") val nomeParada: String?,
    @Json(name = "py") val latitude: Double?,
    @Json(name = "px") val longitude: Double?,
    @Json(name = "l") val linhas: List<LinhaPrevisao>?
)

data class LinhaPrevisao(
    @Json(name = "c") val letreiroCompleto: String?,
    @Json(name = "cl") val codigoLinha: Int?,
    @Json(name = "sl") val sentidoLinha: Int?,
    @Json(name = "lt0") val destino: String?,
    @Json(name = "lt1") val origem: String?,
    @Json(name = "qv") val quantidadeVeiculos: Int?,
    @Json(name = "vs") val veiculos: List<VeiculoPrevisao>?
)

data class VeiculoPrevisao(
    @Json(name = "p") val prefixoVeiculo: String?,
    @Json(name = "t") val horarioPrevisto: String?,
    @Json(name = "a") val acessivel: Boolean?,
    @Json(name = "ta") val horarioCaptura: String?,
    @Json(name = "py") val latitude: Double?,
    @Json(name = "px") val longitude: Double?
)

data class PosicaoVeiculo(
    @Json(name = "hr") val horarioReferencia: String?,
    @Json(name = "l") val linhasVeiculos: List<LinhaVeiculo>?
)

data class LinhaVeiculo(
    @Json(name = "c") val letreiroCompleto: String?,
    @Json(name = "cl") val codigoLinha: Int?,
    @Json(name = "sl") val sentidoLinha: Int?,
    @Json(name = "lt0") val destino: String?,
    @Json(name = "lt1") val origem: String?,
    @Json(name = "qv") val quantidadeVeiculos: Int?,
    @Json(name = "vs") val veiculos: List<Veiculo>?
)

data class Veiculo(
    @Json(name = "p") val prefixoVeiculo: Int?,
    @Json(name = "a") val acessivel: Boolean?,
    @Json(name = "ta") val horarioCaptura: String?,
    @Json(name = "py") val latitude: Double?,
    @Json(name = "px") val longitude: Double?
)

data class Corredor(
    @Json(name = "cc") val codigoCorredor: Int?,
    @Json(name = "nc") val nomeCorredor: String?
)

data class Empresa(
    @Json(name = "hr") val horarioReferencia: String?,
    @Json(name = "e") val areasOperacao: List<AreaOperacao>?
)

data class AreaOperacao(
    @Json(name = "a") val codigoArea: Int?,
    @Json(name = "e") val empresas: List<DadosEmpresa>?
)

data class DadosEmpresa(
    @Json(name = "a") val codigoArea: Int?,
    @Json(name = "c") val codigoEmpresa: Int?,
    @Json(name = "n") val nomeEmpresa: String?
)
