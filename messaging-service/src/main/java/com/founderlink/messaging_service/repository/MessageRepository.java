package com.founderlink.messaging_service.repository;

import com.founderlink.messaging_service.document.Message;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<Message, String> {

	List<Message> findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByTimestampAsc(
			String sender1,
			String receiver1,
			String sender2,
			String receiver2);
}

