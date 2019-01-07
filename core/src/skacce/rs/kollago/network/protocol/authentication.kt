package skacce.rs.kollago.network.protocol

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
