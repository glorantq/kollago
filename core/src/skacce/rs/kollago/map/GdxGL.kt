package skacce.rs.kollago.map

import com.badlogic.gdx.Gdx
import org.oscim.backend.GL
import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class GdxGL : GL {
    override fun attachShader(program: Int, shader: Int) = Gdx.gl.glAttachShader(program, shader)

    override fun bindAttribLocation(program: Int, index: Int, name: String) = Gdx.gl.glBindAttribLocation(program, index, name)

    override fun bindBuffer(target: Int, buffer: Int) = Gdx.gl.glBindBuffer(target, buffer)

    override fun bindFramebuffer(target: Int, framebuffer: Int) = Gdx.gl.glBindFramebuffer(target, framebuffer)

    override fun bindRenderbuffer(target: Int, renderbuffer: Int) = Gdx.gl.glBindRenderbuffer(target, renderbuffer)

    override fun blendColor(red: Float, green: Float, blue: Float, alpha: Float) = Gdx.gl.glBlendColor(red, green, blue, alpha)

    override fun blendEquation(mode: Int) = Gdx.gl.glBlendEquation(mode)

    override fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int) = Gdx.gl.glBlendEquationSeparate(modeRGB, modeAlpha)

    override fun blendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int) = Gdx.gl.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)

    override fun bufferData(target: Int, size: Int, data: Buffer, usage: Int) = Gdx.gl.glBufferData(target, size, data, usage)

    override fun bufferSubData(target: Int, offset: Int, size: Int, data: Buffer) = Gdx.gl.glBufferSubData(target, offset, size, data)

    override fun checkFramebufferStatus(target: Int): Int = Gdx.gl.glCheckFramebufferStatus(target)

    override fun compileShader(shader: Int) = Gdx.gl.glCompileShader(shader)

    override fun createProgram(): Int = Gdx.gl.glCreateProgram()

    override fun createShader(type: Int): Int = Gdx.gl.glCreateShader(type)

    override fun deleteBuffers(n: Int, buffers: IntBuffer) = Gdx.gl.glDeleteBuffers(n, buffers)

    override fun deleteFramebuffers(n: Int, framebuffers: IntBuffer) = Gdx.gl.glDeleteFramebuffers(n, framebuffers)

    override fun deleteProgram(program: Int) = Gdx.gl.glDeleteProgram(program)

    override fun deleteRenderbuffers(n: Int, renderbuffers: IntBuffer) = Gdx.gl.glDeleteRenderbuffers(n, renderbuffers)

    override fun deleteShader(shader: Int) = Gdx.gl.glDeleteShader(shader)

    override fun detachShader(program: Int, shader: Int) = Gdx.gl.glDetachShader(program, shader)

    override fun disableVertexAttribArray(index: Int) = Gdx.gl.glDisableVertexAttribArray(index)

    override fun drawElements(mode: Int, count: Int, type: Int, offset: Int) = Gdx.gl.glDrawElements(mode, count, type, offset)

    override fun enableVertexAttribArray(index: Int) = Gdx.gl.glEnableVertexAttribArray(index)

    override fun framebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int) = Gdx.gl.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)

    override fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int) = Gdx.gl.glFramebufferTexture2D(target, attachment, textarget, texture, level)

    override fun genBuffers(n: Int, buffers: IntBuffer) = Gdx.gl.glGenBuffers(n, buffers)

    override fun generateMipmap(target: Int) = Gdx.gl.glGenerateMipmap(target)

    override fun genFramebuffers(n: Int, framebuffers: IntBuffer) = Gdx.gl.glGenFramebuffers(n, framebuffers)

    override fun genRenderbuffers(n: Int, renderbuffers: IntBuffer) = Gdx.gl.glGenRenderbuffers(n, renderbuffers)

    override fun getActiveAttrib(program: Int, index: Int, size: IntBuffer, type: Buffer): String = Gdx.gl.glGetActiveAttrib(program, index, size, type)

    override fun getActiveUniform(program: Int, index: Int, size: IntBuffer, type: Buffer): String = Gdx.gl.glGetActiveUniform(program, index, size, type)

    override fun getAttachedShaders(program: Int, maxcount: Int, count: Buffer, shaders: IntBuffer) = Gdx.gl.glGetAttachedShaders(program, maxcount, count, shaders)

    override fun getAttribLocation(program: Int, name: String): Int = Gdx.gl.glGetAttribLocation(program, name)

    override fun getBooleanv(pname: Int, params: Buffer) = Gdx.gl.glGetBooleanv(pname, params)

    override fun getBufferParameteriv(target: Int, pname: Int, params: IntBuffer) = Gdx.gl.glGetBufferParameteriv(target, pname, params)

    override fun getFloatv(pname: Int, params: FloatBuffer) = Gdx.gl.glGetFloatv(pname, params)

    override fun getFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: IntBuffer) = Gdx.gl.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params)

    override fun getProgramiv(program: Int, pname: Int, params: IntBuffer) = Gdx.gl.glGetProgramiv(program, pname, params)

    override fun getProgramInfoLog(program: Int): String = Gdx.gl.glGetProgramInfoLog(program)

    override fun getRenderbufferParameteriv(target: Int, pname: Int, params: IntBuffer) = Gdx.gl.glGetRenderbufferParameteriv(target, pname, params)

    override fun getShaderiv(shader: Int, pname: Int, params: IntBuffer) = Gdx.gl.glGetShaderiv(shader, pname, params)

    override fun getShaderInfoLog(shader: Int): String = Gdx.gl.glGetShaderInfoLog(shader)

    override fun getShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: IntBuffer, precision: IntBuffer) = Gdx.gl.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision)

    override fun getShaderSource(shader: Int, bufsize: Int, length: Buffer, source: String) { }

    override fun getTexParameterfv(target: Int, pname: Int, params: FloatBuffer) = Gdx.gl.glGetTexParameterfv(target, pname, params)

    override fun getTexParameteriv(target: Int, pname: Int, params: IntBuffer) = Gdx.gl.glGetTexParameteriv(target, pname, params)

    override fun getUniformfv(program: Int, location: Int, params: FloatBuffer) = Gdx.gl.glGetUniformfv(program, location, params)

    override fun getUniformiv(program: Int, location: Int, params: IntBuffer) = Gdx.gl.glGetUniformiv(program, location, params)

    override fun getUniformLocation(program: Int, name: String): Int = Gdx.gl.glGetUniformLocation(program, name)

    override fun getVertexAttribfv(index: Int, pname: Int, params: FloatBuffer) = Gdx.gl.glGetVertexAttribfv(index, pname, params)

    override fun getVertexAttribiv(index: Int, pname: Int, params: IntBuffer) = Gdx.gl.glGetVertexAttribiv(index, pname, params)

    override fun getVertexAttribPointerv(index: Int, pname: Int, pointer: Buffer) = Gdx.gl.glGetVertexAttribPointerv(index, pname, pointer)

    override fun isBuffer(buffer: Int): Boolean = Gdx.gl.glIsBuffer(buffer)

    override fun isEnabled(cap: Int): Boolean = Gdx.gl.glIsEnabled(cap)

    override fun isFramebuffer(framebuffer: Int): Boolean = Gdx.gl.glIsFramebuffer(framebuffer)

    override fun isProgram(program: Int): Boolean = Gdx.gl.glIsProgram(program)

    override fun isRenderbuffer(renderbuffer: Int): Boolean = Gdx.gl.glIsRenderbuffer(renderbuffer)

    override fun isShader(shader: Int): Boolean = Gdx.gl.glIsShader(shader)

    override fun isTexture(texture: Int): Boolean = Gdx.gl.glIsTexture(texture)

    override fun linkProgram(program: Int) = Gdx.gl.glLinkProgram(program)

    override fun releaseShaderCompiler() = Gdx.gl.glReleaseShaderCompiler()

    override fun renderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) = Gdx.gl.glRenderbufferStorage(target, internalformat, width, height)

    override fun sampleCoverage(value: Float, invert: Boolean) = Gdx.gl.glSampleCoverage(value, invert)

    override fun shaderBinary(n: Int, shaders: IntBuffer, binaryformat: Int, binary: Buffer, length: Int) = Gdx.gl.glShaderBinary(n, shaders, binaryformat, binary, length)

    override fun shaderSource(shader: Int, string: String) = Gdx.gl.glShaderSource(shader, string)

    override fun stencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int) = Gdx.gl.glStencilFuncSeparate(face, func, ref, mask)

    override fun stencilMaskSeparate(face: Int, mask: Int) = Gdx.gl.glStencilMaskSeparate(face, mask)

    override fun stencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int) = Gdx.gl.glStencilOpSeparate(face, fail, zfail, zpass)

    override fun texParameterfv(target: Int, pname: Int, params: FloatBuffer) = Gdx.gl.glTexParameterfv(target, pname, params)

    override fun texParameteri(target: Int, pname: Int, param: Int) = Gdx.gl.glTexParameteri(target, pname, param)

    override fun texParameteriv(target: Int, pname: Int, params: IntBuffer) = Gdx.gl.glTexParameteriv(target, pname, params)

    override fun uniform1f(location: Int, x: Float) = Gdx.gl.glUniform1f(location, x)

    override fun uniform1fv(location: Int, count: Int, v: FloatBuffer) = Gdx.gl.glUniform1fv(location, count, v)

    override fun uniform1i(location: Int, x: Int) = Gdx.gl.glUniform1i(location, x)

    override fun uniform1iv(location: Int, count: Int, v: IntBuffer) = Gdx.gl.glUniform1iv(location, count, v)

    override fun uniform2f(location: Int, x: Float, y: Float) = Gdx.gl.glUniform2f(location, x, y)

    override fun uniform2fv(location: Int, count: Int, v: FloatBuffer) = Gdx.gl.glUniform2fv(location, count, v)

    override fun uniform2i(location: Int, x: Int, y: Int) = Gdx.gl.glUniform2i(location, x, y)

    override fun uniform2iv(location: Int, count: Int, v: IntBuffer) = Gdx.gl.glUniform2iv(location, count, v)

    override fun uniform3f(location: Int, x: Float, y: Float, z: Float) = Gdx.gl.glUniform3f(location, x, y, z)

    override fun uniform3fv(location: Int, count: Int, v: FloatBuffer) = Gdx.gl.glUniform4fv(location, count, v)

    override fun uniform3i(location: Int, x: Int, y: Int, z: Int) = Gdx.gl.glUniform3i(location, x, y, z)

    override fun uniform3iv(location: Int, count: Int, v: IntBuffer) = Gdx.gl.glUniform3iv(location, count, v)

    override fun uniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) = Gdx.gl.glUniform4f(location, x, y, z, w)

    override fun uniform4fv(location: Int, count: Int, v: FloatBuffer) = Gdx.gl.glUniform4fv(location, count, v)

    override fun uniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) = Gdx.gl.glUniform4i(location, x, y, z, w)

    override fun uniform4iv(location: Int, count: Int, v: IntBuffer) = Gdx.gl.glUniform4iv(location, count, v)

    override fun uniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: FloatBuffer) = Gdx.gl.glUniformMatrix2fv(location, count, transpose, value)

    override fun uniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FloatBuffer) = Gdx.gl.glUniformMatrix3fv(location, count, transpose, value)

    override fun uniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatBuffer) = Gdx.gl.glUniformMatrix4fv(location, count, transpose, value)

    override fun useProgram(program: Int) = Gdx.gl.glUseProgram(program)

    override fun validateProgram(program: Int) = Gdx.gl.glValidateProgram(program)

    override fun vertexAttrib1f(indx: Int, x: Float) = Gdx.gl.glVertexAttrib1f(indx, x)

    override fun vertexAttrib1fv(indx: Int, values: FloatBuffer) = Gdx.gl.glVertexAttrib1fv(indx, values)

    override fun vertexAttrib2f(indx: Int, x: Float, y: Float) = Gdx.gl.glVertexAttrib2f(indx, x, y)

    override fun vertexAttrib2fv(indx: Int, values: FloatBuffer) = Gdx.gl.glVertexAttrib2fv(indx, values)

    override fun vertexAttrib3f(indx: Int, x: Float, y: Float, z: Float) = Gdx.gl.glVertexAttrib3f(indx, x, y, z)

    override fun vertexAttrib3fv(indx: Int, values: FloatBuffer) = Gdx.gl.glVertexAttrib3fv(indx, values)

    override fun vertexAttrib4f(indx: Int, x: Float, y: Float, z: Float, w: Float) = Gdx.gl.glVertexAttrib4f(indx, x, y, z, w)

    override fun vertexAttrib4fv(indx: Int, values: FloatBuffer) = Gdx.gl.glVertexAttrib4fv(indx, values)

    override fun vertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Buffer) = Gdx.gl.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)

    override fun vertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int) = Gdx.gl.glVertexAttribPointer(indx, size, type, normalized, stride, offset)

    override fun activeTexture(texture: Int) = Gdx.gl.glActiveTexture(texture)

    override fun bindTexture(target: Int, texture: Int) = Gdx.gl.glBindTexture(target, texture)

    override fun blendFunc(sfactor: Int, dfactor: Int) = Gdx.gl.glBlendFunc(sfactor, dfactor)

    override fun clear(mask: Int) = Gdx.gl.glClear(mask)

    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) = Gdx.gl.glClearColor(red, green, blue, alpha)

    override fun clearDepthf(depth: Float) = Gdx.gl.glClearDepthf(depth)

    override fun clearStencil(s: Int) = Gdx.gl.glClearStencil(s)

    override fun colorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) = Gdx.gl.glColorMask(red, green, blue, alpha)

    override fun compressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, imageSize: Int, data: Buffer) = Gdx.gl.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data)

    override fun compressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, imageSize: Int, data: Buffer) = Gdx.gl.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data)

    override fun copyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int) = Gdx.gl.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border)

    override fun copyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int) = Gdx.gl.glCopyTexImage2D(target, level, xoffset, yoffset, x, y, width, height)

    override fun cullFace(mode: Int) = Gdx.gl.glCullFace(mode)

    override fun deleteTextures(n: Int, textures: IntBuffer) = Gdx.gl.glDeleteTextures(n, textures)

    override fun depthFunc(func: Int) = Gdx.gl.glDepthFunc(func)

    override fun depthMask(flag: Boolean) = Gdx.gl.glDepthMask(flag)

    override fun depthRangef(zNear: Float, zFar: Float) = Gdx.gl.glDepthRangef(zNear, zFar)

    override fun disable(cap: Int) = Gdx.gl.glDisable(cap)

    override fun drawArrays(mode: Int, first: Int, count: Int) = Gdx.gl.glDrawArrays(mode, first, count)

    override fun drawElements(mode: Int, count: Int, type: Int, indices: Buffer) = Gdx.gl.glDrawElements(mode, count, type, indices)

    override fun enable(cap: Int) = Gdx.gl.glEnable(cap)

    override fun finish() = Gdx.gl.glFinish()

    override fun flush() = Gdx.gl.glFlush()

    override fun frontFace(mode: Int) = Gdx.gl.glFrontFace(mode)

    override fun genTextures(n: Int, textures: IntBuffer) = Gdx.gl.glGenTextures(n, textures)

    override fun getError(): Int = Gdx.gl.glGetError()

    override fun getIntegerv(pname: Int, params: IntBuffer) = Gdx.gl.glGetIntegerv(pname, params)

    override fun getString(name: Int): String = Gdx.gl.glGetString(name)

    override fun hint(target: Int, mode: Int) = Gdx.gl.glHint(target, mode)

    override fun lineWidth(width: Float) = Gdx.gl.glLineWidth(width)

    override fun pixelStorei(pname: Int, param: Int) = Gdx.gl.glPixelStorei(pname, param)

    override fun polygonOffset(factor: Float, units: Float) = Gdx.gl.glPolygonOffset(factor, units)

    override fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: Buffer) = Gdx.gl.glReadPixels(x, y, width, height, format, type, pixels)

    override fun scissor(x: Int, y: Int, width: Int, height: Int) = Gdx.gl.glScissor(x, y, width, height)

    override fun stencilFunc(func: Int, ref: Int, mask: Int) = Gdx.gl.glStencilFunc(func, ref, mask)

    override fun stencilMask(mask: Int) = Gdx.gl.glStencilMask(mask)

    override fun stencilOp(fail: Int, zfail: Int, zpass: Int) = Gdx.gl.glStencilOp(fail, zfail, zpass)

    override fun texImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Buffer) = Gdx.gl.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels)

    override fun texParameterf(target: Int, pname: Int, param: Float) = Gdx.gl.glTexParameterf(target, pname, param)

    override fun texSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: Buffer) = Gdx.gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels)

    override fun viewport(x: Int, y: Int, width: Int, height: Int) = Gdx.gl.glViewport(x, y, width, height)
}