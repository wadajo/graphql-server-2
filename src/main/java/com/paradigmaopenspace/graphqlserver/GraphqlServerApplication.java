package com.paradigmaopenspace.graphqlserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@SpringBootApplication
public class GraphqlServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraphqlServerApplication.class, args);
	}

}

@Controller
class GraphqlController{
	AtomicLong idCounter=new AtomicLong();
	List<Artista> bbdd=List.of(
			new Artista(idCounter.incrementAndGet(),"Levstein","Videoarte",
					Collections.emptyList()),
			new Artista(idCounter.incrementAndGet(),"Casile","Performance",
					Collections.emptyList()));

	@QueryMapping
	Flux<Artista> artistas(){
		return Flux.fromIterable(bbdd);
	}

	@BatchMapping(typeName = "Artista")
	public Mono<Map<Artista, List<Obra>>> obras(List<Artista> artistas){

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

	private Flux<Obra> obtenerObras(List<Long> artistasIds) {
		return Flux.just(
				new Obra(Long.parseLong("1"), "Poemas para leer frente al espejo","poemas.jpg"),
				new Obra(Long.parseLong("1"),"Vocabulario","vocab.jpg"),
				new Obra(Long.parseLong("2"),"Lengua húmeda","lengua.jpg")
		);
	}
}

record Artista (Long id, String apellido, String estilo, List<Obra> obras){}
record Obra (Long artistaId, String nombre, String imagen){}