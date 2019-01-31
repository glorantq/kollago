
data class LoginRequest(
    val firebaseUid: String = "",
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<LoginRequest> {
    override operator fun plus(other: LoginRequest?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<LoginRequest> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = LoginRequest.protoUnmarshalImpl(u)
    }
}

data class LoginResponse(
    val errorCode: LoginResponse.ErrorCode = LoginResponse.ErrorCode.fromValue(0),
    val ownProfile: ProfileData? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<LoginResponse> {
    override operator fun plus(other: LoginResponse?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<LoginResponse> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = LoginResponse.protoUnmarshalImpl(u)
    }

    data class ErrorCode(override val value: Int) : pbandk.Message.Enum {
        companion object : pbandk.Message.Enum.Companion<ErrorCode> {
            val OK = ErrorCode(0)
            val INVALID_UID = ErrorCode(1)

            override fun fromValue(value: Int) = when (value) {
                0 -> OK
                1 -> INVALID_UID
                else -> ErrorCode(value)
            }
        }
    }
}

data class ProfileRequest(
    val firebaseUid: String = "",
    val gameSecret: String = "",
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<ProfileRequest> {
    override operator fun plus(other: ProfileRequest?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<ProfileRequest> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = ProfileRequest.protoUnmarshalImpl(u)
    }
}

data class ProfileResponse(
    val profile: ProfileData? = null,
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<ProfileResponse> {
    override operator fun plus(other: ProfileResponse?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<ProfileResponse> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = ProfileResponse.protoUnmarshalImpl(u)
    }
}

data class UpdateProfile(
    val firebaseUid: String = "",
    val xpDelta: Int = 0,
    val coinsDelta: Int = 0,
    val gameSecret: String = "",
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<UpdateProfile> {
    override operator fun plus(other: UpdateProfile?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<UpdateProfile> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = UpdateProfile.protoUnmarshalImpl(u)
    }
}

data class MoveBase(
    val firebaseUid: String = "",
    val position: Coordinates? = null,
    val gameSecret: String = "",
    val unknownFields: Map<Int, pbandk.UnknownField> = emptyMap()
) : pbandk.Message<MoveBase> {
    override operator fun plus(other: MoveBase?) = protoMergeImpl(other)
    override val protoSize by lazy { protoSizeImpl() }
    override fun protoMarshal(m: pbandk.Marshaller) = protoMarshalImpl(m)
    companion object : pbandk.Message.Companion<MoveBase> {
        override fun protoUnmarshal(u: pbandk.Unmarshaller) = MoveBase.protoUnmarshalImpl(u)
    }
}

private fun LoginRequest.protoMergeImpl(plus: LoginRequest?): LoginRequest = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun LoginRequest.protoSizeImpl(): Int {
    var protoSize = 0
    if (firebaseUid.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(firebaseUid)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun LoginRequest.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (firebaseUid.isNotEmpty()) protoMarshal.writeTag(10).writeString(firebaseUid)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun LoginRequest.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): LoginRequest {
    var firebaseUid = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return LoginRequest(firebaseUid, protoUnmarshal.unknownFields())
        10 -> firebaseUid = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}

private fun LoginResponse.protoMergeImpl(plus: LoginResponse?): LoginResponse = plus?.copy(
    ownProfile = ownProfile?.plus(plus.ownProfile) ?: plus.ownProfile,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun LoginResponse.protoSizeImpl(): Int {
    var protoSize = 0
    if (errorCode.value != 0) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.enumSize(errorCode)
    if (ownProfile != null) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.messageSize(ownProfile)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun LoginResponse.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (errorCode.value != 0) protoMarshal.writeTag(8).writeEnum(errorCode)
    if (ownProfile != null) protoMarshal.writeTag(18).writeMessage(ownProfile)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun LoginResponse.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): LoginResponse {
    var errorCode: LoginResponse.ErrorCode = LoginResponse.ErrorCode.fromValue(0)
    var ownProfile: ProfileData? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return LoginResponse(errorCode, ownProfile, protoUnmarshal.unknownFields())
        8 -> errorCode = protoUnmarshal.readEnum(LoginResponse.ErrorCode.Companion)
        18 -> ownProfile = protoUnmarshal.readMessage(ProfileData.Companion)
        else -> protoUnmarshal.unknownField()
    }
}

private fun ProfileRequest.protoMergeImpl(plus: ProfileRequest?): ProfileRequest = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun ProfileRequest.protoSizeImpl(): Int {
    var protoSize = 0
    if (firebaseUid.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(firebaseUid)
    if (gameSecret.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.stringSize(gameSecret)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun ProfileRequest.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (firebaseUid.isNotEmpty()) protoMarshal.writeTag(10).writeString(firebaseUid)
    if (gameSecret.isNotEmpty()) protoMarshal.writeTag(18).writeString(gameSecret)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun ProfileRequest.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): ProfileRequest {
    var firebaseUid = ""
    var gameSecret = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return ProfileRequest(firebaseUid, gameSecret, protoUnmarshal.unknownFields())
        10 -> firebaseUid = protoUnmarshal.readString()
        18 -> gameSecret = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}

private fun ProfileResponse.protoMergeImpl(plus: ProfileResponse?): ProfileResponse = plus?.copy(
    profile = profile?.plus(plus.profile) ?: plus.profile,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun ProfileResponse.protoSizeImpl(): Int {
    var protoSize = 0
    if (profile != null) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.messageSize(profile)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun ProfileResponse.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (profile != null) protoMarshal.writeTag(10).writeMessage(profile)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun ProfileResponse.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): ProfileResponse {
    var profile: ProfileData? = null
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return ProfileResponse(profile, protoUnmarshal.unknownFields())
        10 -> profile = protoUnmarshal.readMessage(ProfileData.Companion)
        else -> protoUnmarshal.unknownField()
    }
}

private fun UpdateProfile.protoMergeImpl(plus: UpdateProfile?): UpdateProfile = plus?.copy(
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun UpdateProfile.protoSizeImpl(): Int {
    var protoSize = 0
    if (firebaseUid.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(firebaseUid)
    if (xpDelta != 0) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.int32Size(xpDelta)
    if (coinsDelta != 0) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.int32Size(coinsDelta)
    if (gameSecret.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(4) + pbandk.Sizer.stringSize(gameSecret)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun UpdateProfile.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (firebaseUid.isNotEmpty()) protoMarshal.writeTag(10).writeString(firebaseUid)
    if (xpDelta != 0) protoMarshal.writeTag(16).writeInt32(xpDelta)
    if (coinsDelta != 0) protoMarshal.writeTag(24).writeInt32(coinsDelta)
    if (gameSecret.isNotEmpty()) protoMarshal.writeTag(34).writeString(gameSecret)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun UpdateProfile.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): UpdateProfile {
    var firebaseUid = ""
    var xpDelta = 0
    var coinsDelta = 0
    var gameSecret = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return UpdateProfile(firebaseUid, xpDelta, coinsDelta, gameSecret, protoUnmarshal.unknownFields())
        10 -> firebaseUid = protoUnmarshal.readString()
        16 -> xpDelta = protoUnmarshal.readInt32()
        24 -> coinsDelta = protoUnmarshal.readInt32()
        34 -> gameSecret = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}

private fun MoveBase.protoMergeImpl(plus: MoveBase?): MoveBase = plus?.copy(
    position = position?.plus(plus.position) ?: plus.position,
    unknownFields = unknownFields + plus.unknownFields
) ?: this

private fun MoveBase.protoSizeImpl(): Int {
    var protoSize = 0
    if (firebaseUid.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(1) + pbandk.Sizer.stringSize(firebaseUid)
    if (position != null) protoSize += pbandk.Sizer.tagSize(2) + pbandk.Sizer.messageSize(position)
    if (gameSecret.isNotEmpty()) protoSize += pbandk.Sizer.tagSize(3) + pbandk.Sizer.stringSize(gameSecret)
    protoSize += unknownFields.entries.sumBy { it.value.size() }
    return protoSize
}

private fun MoveBase.protoMarshalImpl(protoMarshal: pbandk.Marshaller) {
    if (firebaseUid.isNotEmpty()) protoMarshal.writeTag(10).writeString(firebaseUid)
    if (position != null) protoMarshal.writeTag(18).writeMessage(position)
    if (gameSecret.isNotEmpty()) protoMarshal.writeTag(26).writeString(gameSecret)
    if (unknownFields.isNotEmpty()) protoMarshal.writeUnknownFields(unknownFields)
}

private fun MoveBase.Companion.protoUnmarshalImpl(protoUnmarshal: pbandk.Unmarshaller): MoveBase {
    var firebaseUid = ""
    var position: Coordinates? = null
    var gameSecret = ""
    while (true) when (protoUnmarshal.readTag()) {
        0 -> return MoveBase(firebaseUid, position, gameSecret, protoUnmarshal.unknownFields())
        10 -> firebaseUid = protoUnmarshal.readString()
        18 -> position = protoUnmarshal.readMessage(Coordinates.Companion)
        26 -> gameSecret = protoUnmarshal.readString()
        else -> protoUnmarshal.unknownField()
    }
}
