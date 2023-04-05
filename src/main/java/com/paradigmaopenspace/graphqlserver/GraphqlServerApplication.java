package com.paradigmaopenspace.graphqlserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
	static AtomicLong idCounter=new AtomicLong(3L);
	private static List<Artista> bbdd=List.copyOf(obtenerArtistas(null));

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

		var todasLasObras = obtenerObras(null);

		return Flux.fromIterable(todasLasObras).collectList()
				.map(listDeObras -> {
					Map<Long, List<Obra>> obrasDeCadaArtistaId = listDeObras.stream()
							.collect(Collectors.groupingBy(Obra::artistaId));

					return artistas.stream()
							.collect(Collectors.toMap(
									unArtista -> unArtista, //K, el Artista
									unArtista -> Objects.requireNonNullElseGet(obrasDeCadaArtistaId.get(Long.parseLong(unArtista.id().toString())), Collections::emptyList))); //V, la lista de obras
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

	@MutationMapping
	@PreAuthorize("hasRole('USER')")
	Mono<Artista> agregarArtista (@Argument ArtistaInput nuevo) {
		var nuevoArtista=new Artista(idCounter.incrementAndGet(),nuevo.apellido(),nuevo.estilo(),Collections.emptyList(),Collections.emptyList());
		bbdd=obtenerArtistas(nuevoArtista);
		return Mono.just(nuevoArtista);
	}

	// Service o Repository o llamada a otra API que obtiene todas las obras
	private static List<Obra> obtenerObras(Obra otra) {
		var listaBase= new ArrayList<>(listaDeObras());
		if (null!=otra)
			listaBase.add(otra);
		return listaBase;
	}
	private static List<Artista> obtenerArtistas(Artista otro) {
		var listaBase= new ArrayList<>(listaDeArtistas());
		if (null!=otro)
			listaBase.add(otro);
		return listaBase;
	}

	private static List<Artista> listaDeArtistas() {
		return List.of(
				new Artista(1L,"Levstein","Videoarte",
						Collections.emptyList(),Collections.emptyList()),
				new Artista(2L,"Casile","Performance",
						Collections.emptyList(),Collections.emptyList()),
				new Artista(3L,"Obeid","Videoarte",
						Collections.emptyList(),Collections.emptyList()));
	}

	private static List<Obra> listaDeObras(){
		return List.of(
				new Obra(Long.parseLong("1"), "Poemas para leer frente al espejo","poemas.jpg"),
				new Obra(Long.parseLong("1"),"Vocabulario","vocab.jpg"),
				new Obra(Long.parseLong("2"),"Lengua húmeda","lengua.jpg"),
				new Obra(Long.parseLong("3"),"Dobles","dobles_video.jpg")
		);
	}
}


record Artista (Long id, String apellido, String estilo, List<Obra> obras, List<Premio> premios){}
record ArtistaInput (String apellido, String estilo){}
record Obra (Long artistaId, String titulo, String imagen){}
record Premio(Integer ano, String nombre){}