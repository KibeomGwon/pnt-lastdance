package com.pnt.pnt_spring.global.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	// 팀원이 생성한 큐 이름과 정확히 일치해야 합니다.
	public static final String NEWS_QUEUE_NAME = "NEWS";

	/**
	 * 1. 큐 정의 (AI 서버가 구독할 큐)
	 * - durable = true: RabbitMQ가 재시작돼도 큐가 사라지지 않음
	 * - 만약 이미 큐가 존재한다면, Spring이 연결만 하고 새로 만들지는 않습니다.
	 */
	@Bean
	public Queue newsQueue() {
		return new Queue(NEWS_QUEUE_NAME, true);
	}

	// 2. Exchange, Binding 삭제됨
	// (Service에서 큐 이름으로 바로 전송)

	/**
	 * 3. JSON 메시지 컨버터 (필수)
	 * Java 객체(AiNewsRequest)를 JSON 문자열로 자동 변환해 줌
	 */
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * 4. RabbitTemplate 설정
	 * 위에서 만든 JSON 컨버터를 템플릿에 적용
	 */
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}
}