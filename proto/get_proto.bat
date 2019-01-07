@echo off

for %%a in (*) do (
	if "%%~xa" == ".proto" (
		echo Compiling %%a...
		CALL protoc --kotlin_out=proto_out/ --plugin=protoc-gen-kotlin=C:\protoc\protoc-gen-kotlin-0.3.0\bin\protoc-gen-kotlin.bat %%a
	)
)