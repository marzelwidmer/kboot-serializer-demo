package ch.keepcalm.kbootserializerdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.MediaTypes
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.support.WebStack

@SpringBootApplication
class KbootSerializerDemoApplication

fun main(args: Array<String>) {
	runApplication<KbootSerializerDemoApplication>(*args)
}



@RestController
@RequestMapping(produces = [MediaTypes.HAL_JSON_VALUE])
@EnableHypermediaSupport(stacks = [WebStack.WEBFLUX], type = [EnableHypermediaSupport.HypermediaType.HAL])
class IndexResource() {
    companion object REL {
        const val REL_SPRING_INITIALIZR = "start-spring"
    }

    @GetMapping("/")
    suspend fun index(): org.springframework.hateoas.EntityModel<Unit> {
        return EntityModel.of(Unit, org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo(org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn(IndexResource::class.java).index()).withSelfRel().toMono().awaitSingle())
            .add(org.springframework.hateoas.Link.of("http://start.spring.io").withRel(REL_SPRING_INITIALIZR))
    }

    @GetMapping(value = ["/project"])
    suspend fun getProject(): Project {
        // Serializing objects
        val data = Project("kotlinx.serialization", "Kotlin")

        val string = Json.encodeToString(data)
        println(string) // {"name":"kotlinx.serialization","language":"Kotlin"}

        // Deserializing back into objects
        val obj = Json.decodeFromString<Project>(string)
        println(obj) // Project(name=kotlinx.serialization, language=Kotlin)

        return Project("kotlinx.serialization", "Kotlin")
    }
}


@Serializable
data class Project(val name: String, val language: String)
