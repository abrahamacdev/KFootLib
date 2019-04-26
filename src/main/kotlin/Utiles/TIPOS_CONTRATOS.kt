package Utiles

enum class TIPOS_CONTRATOS(val value: String) {

    // Tipos de contrato
    TIPO_CONTRATO_DESCONOCIDO("DESCONOCIDO"),     // No sabemos que tipo de contrato es
    TIPO_CONTRATO_ALQUILER("ALQUILER"),           // Están en alquiler
    TIPO_CONTRATO_VENTA("VENTA"),                 // Están en venta
    TIPO_CONTRATO_COMPARTIR("COMPARTIR"),         // Son para compartir
    TIPO_CONTRATO_VACACIONAL("VACACIONAL")        // Son para alquiler vacacional

}