
data class NearStops(
    val firebaseUid: String = "",
    val position: Coordinates? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<NearStops> {
    override operator fun plus(other: NearStops?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<NearStops> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = NearStops.protoUnmarshalImpl(u)
    }
}

data class NearBases(
    val firebaseUid: String = "",
    val position: Coordinates? = null,
    val gameSecret: String = "",
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<NearBases> {
    override operator fun plus(other: NearBases?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<NearBases> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = NearBases.protoUnmarshalImpl(u)
    }
}

data class NearFeaturesResponse(
    val stopData: List<StopData> = emptyList(),
    val baseData: List<BaseData> = emptyList(),
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<NearFeaturesResponse> {
    override operator fun plus(other: NearFeaturesResponse?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<NearFeaturesResponse> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = NearFeaturesResponse.protoUnmarshalImpl(u)
    }
}

private fun NearStops.protoMergeImpl(plus: NearStops?): NearStops = plus?.copy(
    position = position?.plus(plus.position) ?: plus.position,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun NearStops.protoSizeImpl(): Int {
    var protoSize = 0
    if (firebaseUid.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(firebaseUid)
    if (position != null) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.messageSize(position)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun NearStops.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (firebaseUid.isNotEmpty()) protoMarshal.writeTag(10).writeString(firebaseUid)
    if (position != null) protoMarshal.writeTag(18).writeMessage(position)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun NearStops.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): NearStops {
    var firebaseUid = ""
    var position: Coordinates? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return NearStops(firebaseUid, position, protoUnmarshal.unknownFields())
        10 -> firebaseUid = protoUnmarshal.readString()
        18 -> position = protoUnmarshal.readMessage(Coordinates.Companion)
        else -> protoUnmarshal.unknownField()
    }
}

private fun NearBases.protoMergeImpl(plus: NearBases?): NearBases = plus?.copy(
    position = position?.plus(plus.position) ?: plus.position,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun NearBases.protoSizeImpl(): Int {
    var protoSize = 0
    if (firebaseUid.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(firebaseUid)
    if (position != null) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.messageSize(position)
    if (gameSecret.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.stringSize(gameSecret)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun NearBases.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (firebaseUid.isNotEmpty()) protoMarshal.writeTag(10).writeString(firebaseUid)
    if (position != null) protoMarshal.writeTag(18).writeMessage(position)
    if (gameSecret.isNotEmpty()) protoMarshal.writeTag(26).writeString(gameSecret)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun NearBases.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): NearBases {
    var firebaseUid = ""
    var position: Coordinates? = null
    var gameSecret = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return NearBases(firebaseUid, position, gameSecret, protoUnmarshal.unknownFields())
        10 -> firebaseUid = protoUnmarshal.readString()
        18 -> position = protoUnmarshal.readMessage(Coordinates.Companion)
        26 -> gameSecret = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}

private fun NearFeaturesResponse.protoMergeImpl(plus: NearFeaturesResponse?): NearFeaturesResponse = plus?.copy(
    stopData = stopData + plus.stopData,
    baseData = baseData + plus.baseData,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun NearFeaturesResponse.protoSizeImpl(): Int {
    var protoSize = 0
    if (stopData.isNotEmpty()) protoSize += (pbandk.Sizer.tagSize(1) * stopData.size) + stopData.sumBy(pbandk.Sizer::messageSize)
    if (baseData.isNotEmpty()) protoSize += (pbandk.Sizer.tagSize(2) * baseData.size) + baseData.sumBy(pbandk.Sizer::messageSize)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun NearFeaturesResponse.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (stopData.isNotEmpty()) stopData.forEach { protoMarshal.writeTag(10).writeMessage(it) }
    if (baseData.isNotEmpty()) baseData.forEach { protoMarshal.writeTag(18).writeMessage(it) }
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun NearFeaturesResponse.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): NearFeaturesResponse {
    var stopData: pbandk.ListWithSize.Builder<StopData>? = null
    var baseData: pbandk.ListWithSize.Builder<BaseData>? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return NearFeaturesResponse(pbandk.ListWithSize.Builder.fixed(stopData), pbandk.ListWithSize.Builder.fixed(baseData), protoUnmarshal.unknownFields())
        10 -> stopData = protoUnmarshal.readRepeatedMessage(stopData, StopData.Companion, true)
        18 -> baseData = protoUnmarshal.readRepeatedMessage(baseData, BaseData.Companion, true)
        else -> protoUnmarshal.unknownField()
    }
}
