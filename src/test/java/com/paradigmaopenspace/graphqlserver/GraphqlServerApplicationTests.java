package com.paradigmaopenspace.graphqlserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.List;

@GraphQlTest(GraphqlController.class)
class GraphqlServerApplicationTests {

	@Autowired
	private GraphQlTester graphQlTester;

	@Test
	void apellidos(){
		this.graphQlTester
				.documentName("artistas_test1")
				.execute()
				.path("artistas")
				.entity(List.class)
				.matches(lista->lista.size()==3);
	}

}
