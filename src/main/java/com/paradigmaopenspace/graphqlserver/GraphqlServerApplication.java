package com.paradigmaopenspace.graphqlserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
	@PreAuthorize("hasRole('ADMIN')")
	Flux<Artista> artistas(){
		log.info("Llamado al query: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND));
		return Flux.fromIterable(bbdd)
				.delaySubscription(Duration.of(3, ChronoUnit.SECONDS))
				.doOnSubscribe(e->log.info("Suscrito: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND)))
				.doOnComplete(()-> log.info("Completado."));
	}

	@BatchMapping(typeName = "Artista")
	Mono<Map<Artista, List<Obra>>> obras(List<Artista> artistas){
		log.info("Obteniendo obras: "+ Instant.now().get(ChronoField.MILLI_OF_SECOND));
		var artistasIds = artistas.stream()
				.map(Artista::id)
				.toList();

		var todasLasObras = obtenerObras(artistasIds);

		return todasLasObras.collectList()
				.map(obras -> {
					Map<Long, List<Obra>> obrasDeCadaArtistaId = obras.stream()
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

	private Flux<Obra> obtenerObras(List<Long> artistasIds) {
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