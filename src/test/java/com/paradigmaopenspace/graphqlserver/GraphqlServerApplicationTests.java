package com.paradigmaopenspace.graphqlserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.graphql.test.tester.GraphQlTester;

@GraphQlTest(GraphqlController.class)
class GraphqlServerApplicationTests {

	@Autowired
	private GraphQlTester graphQlTester;

	@Test
	void debeHaberTresArtistas(){
		this.graphQlTester
				.documentName("artistas_test1")
				.execute()
				.path("artistas")
				.entityList(Artista.class)
				.hasSize(3);
	}

	@Test
	void levsteinDebeTenerDosObras(){
		this.graphQlTester
				.documentName("artistas_test2")
				.execute()
				.path("artistas[0].obras")
				.entityList(Obra.class)
				.hasSize(2);
	}

	@Test
	void obeidDebeTenerEstiloVideoarte(){
		this.graphQlTester
				.documentName("artistas_test3")
				.execute()
				.path("artistas[2].estilo")
				.entity(String.class)
				.matches(estilo->estilo.equals("Videoarte"));
	}

	@Test
	void miraldaAgregadoDebeTenerId4(){
		this.graphQlTester
				.documentName("artistas_test4")
				.execute()
				.path("agregarArtista.id")
				.entity(Long.class)
				.isEqualTo(4L);
	}

}
