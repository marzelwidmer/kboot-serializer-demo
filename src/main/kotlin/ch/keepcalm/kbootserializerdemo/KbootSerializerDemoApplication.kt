package ch.keepcalm.kbootserializerdemo

import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.support.WebStack
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.*


@SpringBootApplication
class KbootSerializerDemoApplication

fun main(args: Array<String>) {
    runApplication<KbootSerializerDemoApplication>(*args)
}


@Serializable
data class RecordDto(
    @SerialName("eMail") val abbreviation_canton_and_fl: String
)


@RestController
@RequestMapping(produces = [MediaTypes.HAL_JSON_VALUE])
@EnableHypermediaSupport(stacks = [WebStack.WEBFLUX], type = [EnableHypermediaSupport.HypermediaType.HAL])
class IndexResource(private var messageSource: MessageSource) {
    companion object REL {
        const val REL_SPRING_INITIALIZR = "start-spring"
    }

    @GetMapping("/fooBar")
    suspend fun fooBar() =  RecordDto("hello")

    @GetMapping("/")
    suspend fun index(): org.springframework.hateoas.EntityModel<Unit> {
        return EntityModel.of(
            Unit,
            org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo(org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn(IndexResource::class.java).index())
                .withSelfRel().toMono().awaitSingle()
        )
            .add(org.springframework.hateoas.Link.of("http://start.spring.io").withRel(REL_SPRING_INITIALIZR))
    }

    @GetMapping(value = ["/project"])
    suspend fun getProject(@RequestParam name: String): Project {
        // Serializing objects
        val data = Project("kotlinx.serialization", "Kotlin")

        val string = Json.encodeToString(data)
        println(string) // {"name":"kotlinx.serialization","language":"Kotlin"}

        // Deserializing back into objects
        val obj = Json.decodeFromString<Project>(string)
        println(obj) // Project(name=kotlinx.serialization, language=Kotlin)

        return Project(name)
    }

    @PostMapping(value = ["/project"])
    suspend fun postProject(@RequestBody /*@Validated*/ project: Project): Project {
        return Project(project.name, project.language)
    }


    @GetMapping(value = ["/person"])
    suspend fun getPerson(@RequestParam @Valid name: String) = Person(name = name, email = "foo@bar.com")

    @PostMapping(value = ["/person"])
    suspend fun postPerson(@RequestBody @Valid person: Person): Person {
        return person
    }

    @GetMapping(value = ["foo"])
    suspend fun foo() {
        val locale = LocaleContextHolder.getLocale()
        val foo = messageSource.getMessage("IndexResource.msg", null, locale)
        println(foo)
    }

}

// https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/basic-serialization.md#data-validation
@Serializable
data class Project(@SerialName("name") val name: String, @Required val language: String = computeLanguage()) {
    init {
        require(name.isNotEmpty()) { "name cannot be empty" }
        require(name.length > 2) { "name lenght are not enoghty" }
    }
}
fun computeLanguage(): String {
    println("Computing")
    return "Kotlin"
}

data class Person(
    @get:Size(min = 2, max = 15, message = "custom message")
    val name: String,
    @get:NotEmpty(message = "mymessage}")
    @get:Email
    @SerialName("name") val email: String,

//    @get:NotNull
//    @get:Size(max=64)
//    private val name: String,
//    @get:Min(0)
//    private val age: Int
)

@Configuration
class AppConfig {
    @Bean
    fun validator(): LocalValidatorFactoryBean {
        return LocalValidatorFactoryBean()
    }
}
