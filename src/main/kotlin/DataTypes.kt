class DataTypes {

    val EMPTY = ""
    val KV = "кв"
    val CATEGORY_AREA = "кат. земель"
    val CATEGORY_PROTECTION = "кат. защитности"
    val OZU = "озу"
    val LESB = "lesb"

    //val categoryArea: MutableMap<String, String> = mutableMapOf()
    val categoryProtection: MutableMap<String, String> = mutableMapOf()
    val ozu: MutableMap<String, String> = mutableMapOf()
    val filterParameters = listOf(
        EMPTY,
        KV,
        CATEGORY_AREA,
        CATEGORY_PROTECTION,
        OZU,
        LESB
    )

    val executeParameters = listOf(
        EMPTY,
        CATEGORY_AREA,
        CATEGORY_PROTECTION,
        OZU,
        LESB
    )
    init {

        ozu.putAll(mapOf(
            "000" to "-",
            "021" to "уч. среди безлесн",
            "062" to "берегозащитные",
            "073" to "опушка",
            "113" to "глухари",
            "123" to "редкие жив и раст",
            "133" to "заказник",
            "143" to "реликт и энд. породы",
            "152" to "реликт и энд. раст",
            "253" to "вокруг нас. пунктов",
            "363" to "запас < 50кбм",
            "445" to "бобры",
            "449" to "полосы л. вдоль безлесн пр.",
            "088" to "088"

        ))

        categoryProtection.putAll(mapOf(
            "304601" to "эксплуатационные",
            "121400" to "водоохранная",
            "110100" to "запретные полосы",
            "110200" to "нерестоохранные",
            "120800" to "защитные по дорогам",
            "131802" to "зел. зона",
            "131900" to "ЗСО",
            "142900" to "ООПТ",
            "400000" to "исключения"
        ))

    }
}

