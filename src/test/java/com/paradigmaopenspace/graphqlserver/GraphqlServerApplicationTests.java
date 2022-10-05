package com.paradigmaopenspace.graphqlserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;

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

}
