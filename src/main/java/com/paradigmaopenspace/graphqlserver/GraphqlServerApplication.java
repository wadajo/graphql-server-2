package com.paradigmaopenspace.graphqlserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@SpringBootApplication
public class GraphqlServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlServerApplication.class, args);
	}

}

@Controller
class GraphqlController{
	Logger log= LoggerFactory.getLogger("MyLogger");
	AtomicLong idCounter=new AtomicLong();
	List<Artista> bbdd=List.of(
			new Artista(idCounter.incrementAndGet(),"Levstein","Videoarte",
					Collections.emptyList(),Collections.emptyList()),
			new Artista(idCounter.incrementAndGet(),"Casile","Performance",
					Collections.emptyList(),Collections.emptyList()),
			new Artista(idCounter.incrementAndGet(),"Obeid","Videoarte",
					Collections.emptyList(),Collections.emptyList()));

	@QueryMapping
	@PreAuthorize("hasRole('USER')")
	Flux<Artista> artistas(){
		log.info("Llamado al query general: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND));
		return Flux.fromIterable(bbdd)
				.delaySubscription(Duration.of(1, ChronoUnit.SECONDS))
				.doOnSubscribe(e->log.info("Suscrito query general: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND)))
				.doOnComplete(()-> log.info("Completado query general."));
	}

	@QueryMapping
	@PreAuthorize("hasRole('USER')")
	Flux<Artista> artistasPorEstilo(@Argument String estilo){
		log.info("Llamado al query por estilo: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND));
		var encontrados=bbdd.stream().filter(artista -> artista.estilo().equalsIgnoreCase(estilo)).toList();
		return Flux.fromIterable(encontrados)
				.delaySubscription(Duration.of(1, ChronoUnit.SECONDS))
				.doOnSubscribe(e->log.info("Suscrito query por estilo: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND)))
				.doOnComplete(()-> log.info("Completado query por estilo."));
	}

	@QueryMapping
	@PreAuthorize("hasRole('USER')")
	Mono<Artista> artistaPorId(@Argument String id){
		log.info("Llamado al query individual: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND));
		Artista encontrado = bbdd.get(0);
		return Mono.just(encontrado)
				.delaySubscription(Duration.of(3, ChronoUnit.SECONDS))
				.doOnSubscribe(e->log.info("Suscrito query individual: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND)))
				.doOnTerminate(()-> log.info("Completado query individual."));
	}

	@BatchMapping(typeName = "Artista")
	Mono<Map<Artista, List<Obra>>> obras(List<Artista> artistas){
		log.info("Obteniendo obras: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND));

		var todasLasObras = obtenerObras();

		return todasLasObras.collectList()
				.map(listDeObras -> {
					Map<Long, List<Obra>> obrasDeCadaArtistaId = listDeObras.stream()
							.collect(Collectors.groupingBy(Obra::artistaId));

					return artistas.stream()
							.collect(Collectors.toMap(
									unArtista -> unArtista, //K, el Artista
									unArtista -> obrasDeCadaArtistaId.get(Long.parseLong(unArtista.id().toString())))); //V, la lista de obras
				});
	}

	@SchemaMapping
	List<Premio> premios(Artista artista){
		log.info("Obteniendo premios: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND));
		return switch (artista.apellido()){
			case "Levstein" -> List.of(new Premio(2018,"Premio Estímulo Ministerio de Innovación y Cultura de Santa Fe"));
			case "Casile" -> List.of(new Premio(2015,"6º Premio Itaú de Artes Visuales"));
			default -> Collections.emptyList();
		};
	}

	// Service o Repository o llamada a otra API que obtiene todas las obras
	private Flux<Obra> obtenerObras() {
		return Flux.just(
				new Obra(Long.parseLong("1"), "Poemas para leer frente al espejo","poemas.jpg"),
				new Obra(Long.parseLong("1"),"Vocabulario","vocab.jpg"),
				new Obra(Long.parseLong("2"),"Lengua húmeda","lengua.jpg"),
				new Obra(Long.parseLong("3"),"Dobles","dobles_video.jpg")
		);
	}
}

record Artista (Long id, String apellido, String estilo, List<Obra> obras, List<Premio> premios){}
record Obra (Long artistaId, String titulo, String imagen){}
record Premio(Integer ano, String nombre){}