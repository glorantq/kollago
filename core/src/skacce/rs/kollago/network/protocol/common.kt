package skacce.rs.kollago.network.protocol

data class Coordinates(
        val latitude: Float = 0.0F,
        val longitude: Float = 0.0F,
        val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<Coordinates> {
    override operator fun plus(other: Coordinates?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<Coordinates> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = Coordinates.protoUnmarshalImpl(u)
    }
}

data class ProfileData(
        val username: String = "",
        val model: ProfileData.PlayerModel = ProfileData.PlayerModel.fromValue(0),
        val xp: Long = 0L,
        val coins: Long = 0L,
        val baseId: String = "",
        val flagId: String = "",
        val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<ProfileData> {
    override operator fun plus(other: ProfileData?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<ProfileData> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = ProfileData.protoUnmarshalImpl(u)
    }

    data class PlayerModel(override val value: Int) : pbandk.Message.Enum {
        companion object : pbandk.Message.Enum.Companion<PlayerModel> {
            val DEKU = PlayerModel(0)
            val URARAKA = PlayerModel(1)

            override fun fromValue(value: Int) = when (value) {
                0 -> DEKU
                1 -> URARAKA
                else -> PlayerModel(value)
            }
        }
    }
}

data class StopData(
        val stopId: String = "",
        val coordinates: Coordinates? = null,
        val name: String = "",
        val timeout: Long = 0L,
        val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<StopData> {
    override operator fun plus(other: StopData?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<StopData> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = StopData.protoUnmarshalImpl(u)
    }
}

data class BaseData(
        val baseId: String = "",
        val level: Int = 0,
        val items: String = "",
        val coordinates: Coordinates? = null,
        val ownerProfile: ProfileData? = null,
        val timeout: Long = 0L,
        val lastMoved: Long = 0L,
        val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<BaseData> {
    override operator fun plus(other: BaseData?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<BaseData> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = BaseData.protoUnmarshalImpl(u)
    }
}

private fun Coordinates.protoMergeImpl(plus: Coordinates?): Coordinates = plus?.copy(
        unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun Coordinates.protoSizeImpl(): Int {
    var protoSize = 0
    if (latitude != 0.0F) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.floatSize(latitude)
    if (longitude != 0.0F) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.floatSize(longitude)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun Coordinates.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (latitude != 0.0F) protoMarshal.writeTag(13).writeFloat(latitude)
    if (longitude != 0.0F) protoMarshal.writeTag(21).writeFloat(longitude)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun Coordinates.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): Coordinates {
    var latitude = 0.0F
    var longitude = 0.0F
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return Coordinates(latitude, longitude, protoUnmarshal.unknownFields())
        13 -> latitude = protoUnmarshal.readFloat()
        21 -> longitude = protoUnmarshal.readFloat()
        else -> protoUnmarshal.unknownField()
    }
}

private fun ProfileData.protoMergeImpl(plus: ProfileData?): ProfileData = plus?.copy(
        unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun ProfileData.protoSizeImpl(): Int {
    var protoSize = 0
    if (username.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(username)
    if (model.value != 0) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.enumSize(model)
    if (xp != 0L) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.int64Size(xp)
    if (coins != 0L) protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.int64Size(coins)
    if (baseId.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(5) + pbandk.Sizer.stringSize(baseId)
    if (flagId.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(6) + pbandk.Sizer.stringSize(flagId)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun ProfileData.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (username.isNotEmpty()) protoMarshal.writeTag(10).writeString(username)
    if (model.value != 0) protoMarshal.writeTag(16).writeEnum(model)
    if (xp != 0L) protoMarshal.writeTag(24).writeInt64(xp)
    if (coins != 0L) protoMarshal.writeTag(32).writeInt64(coins)
    if (baseId.isNotEmpty()) protoMarshal.writeTag(42).writeString(baseId)
    if (flagId.isNotEmpty()) protoMarshal.writeTag(50).writeString(flagId)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun ProfileData.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): ProfileData {
    var username = ""
    var model: ProfileData.PlayerModel = ProfileData.PlayerModel.fromValue(0)
    var xp = 0L
    var coins = 0L
    var baseId = ""
    var flagId = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return ProfileData(username, model, xp, coins,
                baseId, flagId, protoUnmarshal.unknownFields())
        10 -> username = protoUnmarshal.readString()
        16 -> model = protoUnmarshal.readEnum(ProfileData.PlayerModel.Companion)
        24 -> xp = protoUnmarshal.readInt64()
        32 -> coins = protoUnmarshal.readInt64()
        42 -> baseId = protoUnmarshal.readString()
        50 -> flagId = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}

private fun StopData.protoMergeImpl(plus: StopData?): StopData = plus?.copy(
        coordinates = coordinates?.plus(plus.coordinates) ?: plus.coordinates,
        unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun StopData.protoSizeImpl(): Int {
    var protoSize = 0
    if (stopId.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(stopId)
    if (coordinates != null) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.messageSize(coordinates)
    if (name.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.stringSize(name)
    if (timeout != 0L) protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.int64Size(timeout)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun StopData.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (stopId.isNotEmpty()) protoMarshal.writeTag(10).writeString(stopId)
    if (coordinates != null) protoMarshal.writeTag(18).writeMessage(coordinates)
    if (name.isNotEmpty()) protoMarshal.writeTag(26).writeString(name)
    if (timeout != 0L) protoMarshal.writeTag(32).writeInt64(timeout)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun StopData.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): StopData {
    var stopId = ""
    var coordinates: Coordinates? = null
    var name = ""
    var timeout = 0L
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return StopData(stopId, coordinates, name, timeout, protoUnmarshal.unknownFields())
        10 -> stopId = protoUnmarshal.readString()
        18 -> coordinates = protoUnmarshal.readMessage(Coordinates.Companion)
        26 -> name = protoUnmarshal.readString()
        32 -> timeout = protoUnmarshal.readInt64()
        else -> protoUnmarshal.unknownField()
    }
}

private fun BaseData.protoMergeImpl(plus: BaseData?): BaseData = plus?.copy(
        coordinates = coordinates?.plus(plus.coordinates) ?: plus.coordinates,
        ownerProfile = ownerProfile?.plus(plus.ownerProfile) ?: plus.ownerProfile,
        unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun BaseData.protoSizeImpl(): Int {
    var protoSize = 0
    if (baseId.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(baseId)
    if (level != 0) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.int32Size(level)
    if (items.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.stringSize(items)
    if (coordinates != null) protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.messageSize(coordinates)
    if (ownerProfile != null) protoSize += pbandk.Sizer.tagSize(5) + pbandk.Sizer.messageSize(ownerProfile)
    if (timeout != 0L) protoSize += pbandk.Sizer.tagSize(6) + pbandk.Sizer.int64Size(timeout)
    if (lastMoved != 0L) protoSize += pbandk.Sizer.tagSize(7) + pbandk.Sizer.int64Size(lastMoved)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun BaseData.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (baseId.isNotEmpty()) protoMarshal.writeTag(10).writeString(baseId)
    if (level != 0) protoMarshal.writeTag(16).writeInt32(level)
    if (items.isNotEmpty()) protoMarshal.writeTag(26).writeString(items)
    if (coordinates != null) protoMarshal.writeTag(34).writeMessage(coordinates)
    if (ownerProfile != null) protoMarshal.writeTag(42).writeMessage(ownerProfile)
    if (timeout != 0L) protoMarshal.writeTag(48).writeInt64(timeout)
    if (lastMoved != 0L) protoMarshal.writeTag(56).writeInt64(lastMoved)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun BaseData.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): BaseData {
    var baseId = ""
    var level = 0
    var items = ""
    var coordinates: Coordinates? = null
    var ownerProfile: ProfileData? = null
    var timeout = 0L
    var lastMoved = 0L
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return BaseData(baseId, level, items, coordinates,
                ownerProfile, timeout, lastMoved, protoUnmarshal.unknownFields())
        10 -> baseId = protoUnmarshal.readString()
        16 -> level = protoUnmarshal.readInt32()
        26 -> items = protoUnmarshal.readString()
        34 -> coordinates = protoUnmarshal.readMessage(Coordinates.Companion)
        42 -> ownerProfile = protoUnmarshal.readMessage(ProfileData.Companion)
        48 -> timeout = protoUnmarshal.readInt64()
        56 -> lastMoved = protoUnmarshal.readInt64()
        else -> protoUnmarshal.unknownField()
    }
}
