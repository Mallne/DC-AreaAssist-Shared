package cloud.mallne.dicentra.areaassist.units

/**
 * Units to measure computer storage, bandwidth, etc..
 */
class BinarySize(suffix: String, ratio: Double = 1.0, databasePrimitive: String) :
    Units(suffix, ratio, databasePrimitive) {
    operator fun div(other: BinarySize) = ratio / other.ratio

    companion object {
        val bytes = BinarySize("B", databasePrimitive = "bytes")
        val kilobytes = BinarySize("kB", 1000.0, "kilobytes")
        val megabytes = BinarySize("MB", 1000.0 * kilobytes `in` bytes, "megabytes")
        val gigabytes = BinarySize("GB", 1000.0 * megabytes `in` bytes, "gigabytes")
        val terabytes = BinarySize("TB", 1000.0 * gigabytes `in` bytes, "terabytes")
        val petabytes = BinarySize("PB", 1000.0 * terabytes `in` bytes, "petabytes")
        val kibibytes = BinarySize("KiB", 1024.0, "kibibytes")
        val mebibytes = BinarySize("MiB", 1024.0 * kibibytes `in` bytes, "mebibytes")
        val gibibytes = BinarySize("GiB", 1024.0 * mebibytes `in` bytes, "gibibytes")
        val tebibytes = BinarySize("TiB", 1024.0 * gibibytes `in` bytes, "tebibytes")
        val pebibytes = BinarySize("PiB", 1024.0 * tebibytes `in` bytes, "pebibytes")
        val bits = BinarySize("bits", 1.0 / 8, "bits")
        val kilobits = BinarySize("kbit", 1000.0 * bits `in` bytes, "kilobits")
        val megabits = BinarySize("Mbit", 1000.0 * kilobits `in` bytes, "megabits")
        val gigabits = BinarySize("Gbit", 1000.0 * megabits `in` bytes, "gigabits")
        val terabits = BinarySize("Tbit", 1000.0 * gigabits `in` bytes, "terabits")
        val petabits = BinarySize("Pbit", 1000.0 * terabits `in` bytes, "petabits")
        val kibibits = BinarySize("Kibit", 1024.0, "kibibits")
        val mebibits = BinarySize("Mibit", 1024.0 * kibibits `in` bytes, "mebibits")
        val gibibits = BinarySize("Gibit", 1024.0 * mebibits `in` bytes, "gibibits")
        val tebibits = BinarySize("Tibit", 1024.0 * gibibits `in` bytes, "tebibits")
        val pebibits = BinarySize("Tibit", 1024.0 * tebibits `in` bytes, "pebibits")

        fun Measure<BinarySize>.autoConvertBinaryBytes(): Measure<BinarySize> = autoConvert(
            bytes, bytes, listOf(
                kilobytes,
                megabytes,
                gigabytes,
                terabytes,
                petabytes,
            )
        )

        enum class UnitStore(override val unit: BinarySize) : IUnitStore<BinarySize> {
            BYTES(unit = bytes),
            KILOBYTES(unit = kilobytes),
            MEGABYTES(unit = megabytes),
            GIGABYTES(unit = gigabytes),
            TERABYTES(unit = terabytes),
            PETABYTES(unit = petabytes),
            KIBIBYTES(unit = kibibytes),
            MEBIBYTES(unit = mebibytes),
            GIBIBYTES(unit = gibibytes),
            TEBIBYTES(unit = tebibytes),
            PEBIBYTES(unit = pebibytes),
            BITS(unit = bits),
            KILOBITS(unit = kilobits),
            MEGABITS(unit = megabits),
            GIGABITS(unit = gigabits),
            TERABITS(unit = terabits),
            PETABITS(unit = petabits),
            KIBIBITS(unit = kibibits),
            MEBIBITS(unit = mebibits),
            GIBIBITS(unit = gibibits),
            TEBIBITS(unit = tebibits),
            PEBIBITS(unit = pebibits),
        }
    }
}