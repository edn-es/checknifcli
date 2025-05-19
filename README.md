## CheckNif Command Line version

Una herramienta de línea de comandos para la validación masiva de Números de Identificación Fiscal (NIF) españoles contra la Agencia Estatal de Administración Tributaria (AEAT).

## Descripción

`checknifcli` es una utilidad diseñada para automatizar la verificación de una lista de NIFs y sus correspondientes nombres con la base de datos de la AEAT. Esto es especialmente útil para empresas o particulares que necesitan validar grandes volúmenes de información de contribuyentes de forma eficiente.

El programa toma como entrada un archivo de texto donde cada línea contiene un NIF y un nombre asociado, separados por el carácter "|". Se comunica con los servicios de la AEAT utilizando un certificado digital proporcionado por el usuario y su contraseña. Finalmente, genera un archivo de salida con los NIFs que no coinciden con el nombre registrado en la AEAT.

## Características Principales

* **Validación masiva:** Procesa múltiples NIFs de forma eficiente a partir de un archivo de entrada.
* **Integración con la AEAT:** Se conecta directamente con los servicios de la Agencia Tributaria para la validación.
* **Autenticación por certificado:** Utiliza un certificado digital (de empresa o particular) para una comunicación segura con la AEAT.
* **Generación de informe:** Crea un archivo con los NIFs que no superan la validación, facilitando la identificación de errores.
* **Formato de entrada sencillo:** Espera un archivo de texto plano con un formato `NIF|Nombre`.

## Requisitos

Para utilizar `checknifcli`, necesitas lo siguiente:

* Un **certificado digital** válido (de empresa o particular) en formato compatible con la librería utilizada para la comunicación segura.
* La **contraseña** asociada a dicho certificado.
* Un **archivo de texto de entrada** donde cada línea contenga un NIF y el nombre asociado, separados por el carácter "|". Por ejemplo:
    ```
    12345678A|Nombre Apellido1 Apellido2
    B98765432|Razón Social
    X1234567Y|Otro Nombre
    ```
* **Java o Linux**. Si quieres ejecutar la version command line necesitas Linux pero puedes
ejecutar la version Java en cualquier otro sistema operativo


## Build

```bash
.gradlew nativeRun
```

## Ejemplo

```bash
build/native/nativeComplie/checknifcli entrada.txt -e err-prod.txt
```
