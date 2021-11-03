package com.Meditation.Sounds.frequencies.lemeor

const val QUANTUM_TIER_SUBS_MONTH = "new.qicoil.subs.quantum.tier.month"
const val QUANTUM_TIER_SUBS_ANNUAL = "new.qicoil.subs.quantum.tier.annual"
const val QUANTUM_TIER_SUBS_ANNUAL_7_DAY_TRIAL="7_days_trial_yearly"

enum class InappPurchase(val sku: String, val categoryId: Int,val Id: Int) {
    NULL("", -1, -1),

    HIGHER_QUANTUM_TIER_INAPP_NMN("new.qicoil.inapp.nmn", 37, -1),
    HIGHER_QUANTUM_TIER_INAPP_NAD("new.qicoil.inapp.nad", 36, -1),
    HIGHER_QUANTUM_TIER_INAPP_AYAHUASCA("new.qicoil.inapp.ayahuasca", 34,220),
    HIGHER_QUANTUM_TIER_INAPP_DMT("new.qicoil.inapp.dmt", 35,219),
    HIGHER_QUANTUM_TIER_INAPP_FITNESS("new.qicoil.inapp.higher.quantum.tier.fitness", 24, -1),
    HIGHER_QUANTUM_TIER_INAPP_SKIN_CARE("new.qicoil.inapp.higher.quantum.tier.skin.care", 23, -1),
    HIGHER_QUANTUM_TIER_INAPP_BEAUTY_II("new.qicoil.inapp.higher.quantum.tier.beauty2", 22, -1),
    HIGHER_QUANTUM_TIER_INAPP_BEAUTY_I("new.qicoil.inapp.higher.quantum.tier.beauty1", 21, -1),
    HIGHER_QUANTUM_TIER_INAPP_PROTECTION("new.qicoil.inapp.higher.quantum.tier.protection", 20, -1),
    HIGHER_QUANTUM_TIER_INAPP_TRANSFORMATION_MEDITATION("new.qicoil.inapp.higher.quantum.tier.meditation.spiritual", 19, -1),
    HIGHER_QUANTUM_TIER_INAPP_MANIFESTING("new.qicoil.inapp.higher.quantum.tier.manifesting", 18, -1),
    HIGHER_QUANTUM_TIER_INAPP_WISDOM("new.qicoil.inapp.higher.quantum.tier.wisdom", 17, -1),
    HIGHER_QUANTUM_TIER_INAPP_BRAIN("new.qicoil.inapp.higher.quantum.tier.brain.boost2", 16, -1),
    HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LOVE("new.qicoil.inapp.higher.quantum.tier.abundance.love", 15, -1),
    HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_HAPPINESS("new.qicoil.inapp.higher.quantum.tier.abundance.happiness", 14, -1),
    HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_SUCCESS("new.qicoil.inapp.higher.quantum.tier.abundance.success", 13, -1),
    HIGHER_QUANTUM_TIER_INAPP_ABUNDANCE_LUCK("new.qicoil.inapp.higher.quantum.tier.abundance.luck", 12, -1),
    HIGHER_QUANTUM_TIER_INAPP_LIFE_FORCE("new.qicoil.inapp.higher.quantum.tier.life.force", 11, -1),
    HIGHER_QUANTUM_TIER_INAPP_WELLNESS_III("new.qicoil.inapp.higher.quantum.tier.wellness.3", 10, -1),
    HIGHER_QUANTUM_TIER_INAPP_WELLNESS_II("new.qicoil.inapp.higher.quantum.tier.wellness.2", 9, -1),
    HIGHER_QUANTUM_TIER_INAPP_WELLNESS_I("new.qicoil.inapp.higher.quantum.tier.wellness.1", 8, -1);

    companion object {
        @JvmStatic
        fun getCategoryId(id: Int): InappPurchase = values().find { value -> value.categoryId == id } ?: NULL

        @JvmStatic
        fun getId(id: Int): InappPurchase = values().find { value -> value.Id == id } ?: NULL
    }
}

