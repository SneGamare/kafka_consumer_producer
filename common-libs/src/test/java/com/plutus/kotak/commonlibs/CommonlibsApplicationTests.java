package com.plutus.kotak.commonlibs;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
@ActiveProfiles("test")
class CommonlibsApplicationTests {

	@Autowired
	private InputDestination inputDestination;

	@Autowired
	private OutputDestination outputDestination;

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads successfully
		assertThat(inputDestination).isNotNull();
		assertThat(outputDestination).isNotNull();
	}

	@Test
	void testApplicationStartup() {
		// This test verifies that the application starts successfully
		assertThat(CommonlibsApplication.class).isNotNull();
	}

	@Test
	void testKafkaConfiguration() {
		// Test Kafka message sending and receiving
		String testMessage = "Test message";
		inputDestination.send(new GenericMessage<>(testMessage));
		
		Message<byte[]> received = outputDestination.receive();
		assertThat(received).isNotNull();
		assertThat(new String(received.getPayload())).contains(testMessage);
	}

}
