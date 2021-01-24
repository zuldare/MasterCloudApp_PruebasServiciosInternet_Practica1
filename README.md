# MasterCloudApp_PruebasServiciosInternet_Practica1
Practical Job 1 of "Pruebas de Servicios de Internet" subject of MasterCloudApps University Master's Degree

# Usage
## Run application
### Install application
```shell script
mvn install
```
### Start application
```shell script
mvn spring-boot:run
```
#### Get books (bash)
- Request example:
    ```shell script
    curl --location --request GET 'https://localhost:8443/api/books/'
    ```
- Response example:
    ```json
    [
        {
            "id": 1,
            "title": "SUEÑOS DE ACERO Y NEON",
            "description": "Los personajes que protagonizan este relato sobreviven en una sociedad en decadencia a la que, no obstante, lograrán devolver la posibilidad de un futuro. Año 2484. En un mundo dominado por las grandes corporaciones, solo un hombre, Jordi Thompson, detective privado deslenguado y vividor, pero de gran talento y sentido d..."
        },
        {
            "id": 2,
            "title": "LA VIDA SECRETA DE LA MENTE",
            "description": "La vida secreta de la mentees un viaje especular que recorre el cerebro y el pensamiento: se trata de descubrir nuestra mente para entendernos hasta en los más pequeños rincones que componen lo que somos, cómo forjamos las ideas en los primeros días de vida, cómo damos forma a las decisiones que nos constituyen, cómo soñamos y cómo imaginamos, por qué sentimos ciertas emociones hacia los demás, cómo los demás influyen en nosotros, y cómo el cerebro se transforma y, con él, lo que somos."
        },
        {
            "id": 3,
            "title": "CASI SIN QUERER",
            "description": "El amor algunas veces es tan complicado como impredecible. Pero al final lo que más valoramos son los detalles más simples, los más bonitos, los que llegan sin avisar. Y a la hora de escribir sobre sentimientos, no hay nada más limpio que hacerlo desde el corazón. Y eso hace Defreds en este libro."
        },
        {
            "id": 4,
            "title": "TERMINAMOS Y OTROS POEMAS SIN TERMINAR",
            "description": "Recopilación de nuevos poemas, textos en prosa y pensamientos del autor. Un sabio dijo una vez: «Pocas cosas hipnotizan tanto en este mundo como una llama y como la luna, será porque no podemos cogerlas o porque nos iluminan en la penumbra». Realmente no sé si alguien dijo esta cita o me la acabo de inventar pero deberían de haberla escrito porque el poder hipnótico que ejercen esa mujer de rojo y esa dama blanca sobre el ser humano es digna de estudio."
        },
        {
            "id": 5,
            "title": "LA LEGIÓN PERDIDA",
            "description": "En el año 53 a. C. el cónsul Craso cruzó el Éufrates para conquistar Oriente, pero su ejército fue destrozado en Carrhae. Una legión entera cayó prisionera de los partos. Nadie sabe a ciencia cierta qué pasó con aquella legión perdida.150 años después, Trajano está a punto de volver a cruzar el Éufrates. ..."
        }
    ]
    ```

#### Create book (bash)
- Request example:
    ```shell script
    curl --location --request POST 'https://localhost:8443/api/books/' \
    --header 'Authorization: Basic dXNlcjpwYXNz' \
    --header 'Content-Type: application/json' \
    --data-raw '{ 
        "title": "SUEÑOS DE ACERO Y NEON", 
        "description": "Los personajes que protagonizan este relato sobreviven en una sociedad en decadencia a la que, no obstante, lograrán devolver la posibilidad de un futuro. Año 2484. En un mundo dominado por las grandes corporaciones, solo un hombre, Jordi Thompson, detective privado deslenguado y vividor, pero de gran talento y sentido d..."
    }'
    ```
- Response example:
    ```json
    {
        "id": 1,
        "title": "SUEÑOS DE ACERO Y NEON",
        "description": "Los personajes que protagonizan este relato sobreviven en una sociedad en decadencia a la que, no obstante, lograrán devolver la posibilidad de un futuro. Año 2484. En un mundo dominado por las grandes corporaciones, solo un hombre, Jordi Thompson, detective privado deslenguado y vividor, pero de gran talento y sentido d..."
    }
    ```

#### Delete book (bash)
- Request example:
    ```shell script
    curl --location --request DELETE 'https://localhost:8443/api/books/1' \
    --header 'Authorization: Basic YWRtaW46cGFzcw=='
    ```
- Return codes:
    - 200: If book was successfully deleted.
    - 404: If book was not found.

## Run Tests
Tests are under _src/test/java/es/urjc/code/daw/library_ folder.

There are two kinds of tests:
- Unit tests: they are in _unit_ folder. All of them are annotated with
    ```java
    @Tag("UnitTests")
    ```
- E2E tests: they are in _e2e_ folder
    ```java
    @Tag("E2e-Test")
    ```

### Run Unit tests
- In shell:
    ```shell script
    mvn test -Punit
    ```

### Run E2E tests
- In shell:
    ```shell script
    mvn test -Pe2e
    ```

### Run all tests
- In shell:
    ```shell script
    mvn test
    ```

# Author

👤 **Jaime Hernández Ortiz**

* Github: [@zuldare](https://github.com/zuldare)

Note that you should have to [install JDK 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) and [Maven](https://maven.apache.org/install.html) as prerequisite.
