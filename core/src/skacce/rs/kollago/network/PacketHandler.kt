package skacce.rs.kollago.network

import com.badlogic.gdx.Gdx
import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import pbandk.Message
import java.lang.reflect.Method
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

class PacketHandler(kryo: Kryo) : Listener() {
    private val packetMapping: BiMap<Int, KClass<Message<*>>> = HashBiMap.create()
    private val handlerMapping: MutableMap<KClass<Message<*>>, Handler> = hashMapOf()
    private val responseMapping: MutableMap<String, ResponseHandler> = hashMapOf()

    init {
        kryo.register(ByteArray::class.java)
        kryo.register(KryoMessageWrapper::class.java)
    }

    fun registerHandler0(packet: KClass<Message<*>>, handler: Handler) {
        handlerMapping[packet] = handler
        registerPacket(packet)

        Gdx.app.log("PacketHandler", "Created handler for ${packet.simpleName}!")
    }

    inline fun <reified T : Message<*>> registerHandler(crossinline handler: (connection: Connection, packet: T, responseId: String) -> Unit) {
        registerHandler0(T::class as KClass<Message<*>>) { connection, packet, responseId ->
            handler(connection, packet as T, responseId)
        }
    }

    fun <T : Message<*>> registerPacket(packet: KClass<T>) {
        packetMapping[packetMapping.size] = packet as KClass<Message<*>>

        Gdx.app.log("PacketHandler", "Registered packet: ${packet.simpleName}")
    }

    override fun received(connection: Connection, `object`: Any) {
        if(`object` is KryoMessageWrapper) {
            val packetWrapper: KryoMessageWrapper = `object`

            if(!packetMapping.containsKey(packetWrapper.messageId)) {
                Gdx.app.error("PacketHandler", "Received unknown message!")
                return
            }

            val packetClass: KClass<Message<*>> = packetMapping[packetWrapper.messageId]!!

            val method: Method? = packetClass.companionObject!!.java.getMethod("protoUnmarshal", ByteArray::class.java)

            if(method == null) {
                Gdx.app.error("PacketHandler", "Failed to get unmarshal method!")
                return
            }

            val message: Message<*> = method.invoke(packetClass.companionObjectInstance!!, packetWrapper.data) as Message<*>

            if(packetWrapper.responseId.isNotBlank()) {
                val responseId: String = packetWrapper.responseId

                if(responseMapping.containsKey(responseId)) {
                    responseMapping[responseId]!!(message)
                    responseMapping.remove(responseId)

                    Gdx.app.log("PacketHandler", "Handled response $responseId")

                    return
                }
            }

            if(handlerMapping.containsKey(packetClass)) {
                handlerMapping[packetClass]!!(connection, message, packetWrapper.responseId)

                Gdx.app.log("PacketHandler", "Called handler for ${packetClass.simpleName}")
            }
        }
    }

    fun sendPacket(packet: Message<*>, responseId: String, connection: Connection) {
        if(!packetMapping.inverse().containsKey(packet::class)) {
            Gdx.app.error("PacketHandler", "This packet isn't registered!")
        }

        val packetId: Int = packetMapping.inverse()[packet::class]!!

        val packetWrapper: KryoMessageWrapper = KryoMessageWrapper(packetId, responseId, packet.protoMarshal())
        connection.sendTCP(packetWrapper)
    }

    fun sendPacketForResponse(packet: Message<*>, connection: Connection, handler: ResponseHandler) {
        if(!packetMapping.inverse().containsKey(packet::class)) {
            Gdx.app.error("PacketHandler", "This packet isn't registered!")
        }

        val packetId: Int = packetMapping.inverse()[packet::class]!!
        val responseId: UUID = UUID.randomUUID()

        responseMapping[responseId.toString()] = handler

        val packetWrapper: KryoMessageWrapper = KryoMessageWrapper(packetId, responseId.toString(), packet.protoMarshal())
        connection.sendTCP(packetWrapper)
    }

    class KryoMessageWrapper(var messageId: Int, var responseId: String, var data: ByteArray) {
        constructor() : this(0, "", byteArrayOf())
    }
}

typealias Handler = (connection: Connection, packet: Any, responseId: String) -> Unit
typealias ResponseHandler = (response: Any) -> Unit