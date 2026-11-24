# Manual de Instrucciones y Configuración de Seguridad Avanzada Móvil

Este documento detalla la arquitectura de seguridad implementada en la aplicación Android **ITQ - Seguridad Avanzada**, abarcando la protección criptográfica local aislada en hardware (TEE/HSM) y la comunicación segura en red con **TLS Pinning**.

---

## 🛡️ 1. Arquitectura Criptográfica Local

### 1.1 Android KeyStore System (TEE / HSM)
La gestión de claves maestras se realiza a través de la clase [`KeyStoreManager.java`](app/src/main/java/com/itq/seguridadavanzadamovilapp/KeyStoreManager.java).

- **Proveedor:** `AndroidKeyStore`
- **Algoritmo:** AES-256 en modo GCM sin relleno (`AES/GCM/NoPadding`).
- **Aislamiento:** Las claves criptográficas se generan e instancian directamente en el entorno de ejecución seguro del procesador (**TEE / HSM**), garantizando que las claves nunca residan en la memoria RAM del proceso principal de la aplicación.
- **Autenticidad:** Cada bloque de texto cifrado incluye un vector de inicialización (**IV**) de 12 bytes concatenado al payload cifrado y codificado en Base64.

### 1.2 EncryptedSharedPreferences (Jetpack Security)
Para el almacenamiento persistente seguro en disco, la aplicación utiliza la biblioteca Jetpack `androidx.security:security-crypto`:

- **MasterKey:** Creada con esquema de clave `MasterKey.KeyScheme.AES256_GCM`.
- **Cifrado de Llaves y Valores:** `AES256_SIV` para las llaves de preferencia y `AES256_GCM` para los valores.

### 1.3 Protección en Ofuscación R8 / ProGuard
El modelo DTO [`CredencialSeguraDto.java`](app/src/main/java/com/itq/seguridadavanzadamovilapp/CredencialSeguraDto.java) cuenta con la anotación `@Keep` y la regla en [`proguard-rules.pro`](app/proguard-rules.pro):

```proguard
-keep class com.itq.seguridadavanzadamovilapp.CredencialSeguraDto { *; }
```

---

## 🌐 2. Comunicación Segura HTTPS y TLS Pinning

La configuración de red se gestiona mediante el archivo descriptor [`network_security_config.xml`](app/src/main/res/xml/network_security_config.xml).

### 2.1 Requisitos del Digest SHA-256
> [!IMPORTANT]
> Android exige que los elementos `<pin digest="SHA-256">` estén expresados obligatoriamente en **Base64** representando la huella digital SHA-256 de 32 bytes de la clave pública `SubjectPublicKeyInfo` (SPKI) del certificado.
> **No colocar valores en formato hexadecimal de 64 caracteres**, ya que causará un error de análisis (`XmlConfigSource$ParserException`) impidiendo el arranque de la app.

### 2.2 Estructura del Config XML
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">jsonplaceholder.typicode.com</domain>

        <pin-set expiration="2027-12-31">
            <!-- Pin principal SHA-256 en Base64 -->
            <pin digest="SHA-256">kuj8niDJZ7L/FhVll1wOJFjX6v0tfSGasazraQ0i1a0=</pin>
            <!-- Pin de respaldo (Backup Pin) -->
            <pin digest="SHA-256">WJG6Vacsln05Vyy6ULfkcZyA8InTgOXgMpBuQO9o8O8=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

---

## 🛠️ 3. Obtención de Pines SPKI SHA-256 Base64

Para extraer el pin de clave pública de cualquier servidor HTTPS seguro, se puede ejecutar el siguiente comando en consola (requiere OpenSSL):

```bash
openssl s_client -connect jsonplaceholder.typicode.com:443 -servername jsonplaceholder.typicode.com </dev/null 2>/dev/null \
  | openssl x509 -pubkey -noout \
  | openssl pkey -pubin -outform der \
  | openssl dgst -sha256 -binary \
  | openssl base64
```

O mediante el script de Python utilizando la librería `cryptography`:

```python
import ssl, socket, hashlib, base64
from cryptography import x509
from cryptography.hazmat.primitives import serialization

hostname = 'jsonplaceholder.typicode.com'
ctx = ssl.create_default_context()
with socket.create_connection((hostname, 443)) as sock:
    with ctx.wrap_socket(sock, server_hostname=hostname) as ssock:
        der_cert = ssock.getpeercert(binary_form=True)
        cert = x509.load_der_x509_certificate(der_cert)
        spki = cert.public_key().public_bytes(
            encoding=serialization.Encoding.DER,
            format=serialization.PublicFormat.SubjectPublicKeyInfo
        )
        print('SPKI Base64 Pin:', base64.b64encode(hashlib.sha256(spki).digest()).decode('utf-8'))
```

---

## 🧪 4. Guía de Pruebas y Validación

### 4.1 Prueba de Conexión Exitosa
1. Asegurarse de que el pin principal del servidor esté configurado correctamente en `network_security_config.xml`.
2. Abrir la aplicación y presionar **PROBAR COMUNICACIÓN HTTPS CON TLS PINNING**.
3. El estado en la interfaz cambiará a:
   > `Conexión HTTPS exitosa. Certificado y Pin SHA-256 validados.`
4. La lista mostrará los elementos obtenidos por la API REST cifrados localmente.

### 4.2 Prueba de Rechazo Simulado (Violación de TLS Pinning)
1. Para probar que el sistema rechaza servidores no autorizados o ataques de intermediario (MITM), modifique **ambos pines** (principal y de respaldo) en `network_security_config.xml` utilizando valores Base64 de 32 bytes falsos:

```xml
<pin-set expiration="2027-12-31">
    <pin digest="SHA-256">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</pin>
    <pin digest="SHA-256">BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=</pin>
</pin-set>
```

2. Compile y despliegue la aplicación.
3. Al presionar **PROBAR COMUNICACIÓN HTTPS CON TLS PINNING**, el sistema bloqueará la conexión e informará:
   > `Fallo de Seguridad: TLS Pinning o red rechazada.`

> [!NOTE]
> En la implementación de [`SeguridadAvanzadaRepository.java`](app/src/main/java/com/itq/seguridadavanzadamovilapp/SeguridadAvanzadaRepository.java), se incluye `request.setShouldCache(false)` y `requestQueue.getCache().clear()` para garantizar que Volley desactive el caché HTTP de disco y realice siempre un Handshake de red en tiempo real.
