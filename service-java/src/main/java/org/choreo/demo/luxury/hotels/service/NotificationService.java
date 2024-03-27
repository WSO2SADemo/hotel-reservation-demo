package org.choreo.demo.luxury.hotels.service;

import org.choreo.demo.luxury.hotels.model.Reservation;
import org.choreo.demo.luxury.hotels.model.ReservationEvent;
import org.choreo.demo.luxury.hotels.repo.ReservationRepository;
import org.choreo.demo.luxury.hotels.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    @Value("${notification-api.endpoint}")
    private String notificationApiEndpoint;


    @Autowired
    UserRepository userRepository;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    WebClient webClient;



    public void sendNotification(ReservationEvent event, Reservation reservation) {

        // send notification
        switch (event) {
            case ReservationCreated:
                logger.info("NotificationService: Sending reservation created notification to user: " + reservation.getUser().getId());
                onReservationCreated(reservation);
                break;
            case ReservationUpdated:
                logger.info("NotificationService: Sending reservation updated notification to user: " +  reservation.getUser().getId());
                onReservationUpdated(reservation);
                break;
            case ReservationDeleted:
                logger.info("NotificationService: Sending reservation cancelled notification to user: "  + reservation.getUser().getId());
                onReservationCancelled(reservation);
                break;
        }

    }

    private void onReservationCreated(Reservation reservation) {
        String subject = "You have a booking at Lux hotels";
        String body = "Your booking with reservation id " + reservation.getId() + " has been confirmed. We look forward to seeing you!";

        EmailRequest emailRequest = new EmailRequest(reservation.getUser().getEmail(), subject, body);
        sendEmail(emailRequest).subscribe();
    }


    private void onReservationUpdated(Reservation reservation) {
        String subject = "Your booking at Lux hotels has been updated";
        String body = "Your booking with reservation id " + reservation.getId() + " has been updated. We look forward to seeing you!";

        EmailRequest emailRequest = new EmailRequest(reservation.getUser().getEmail(), subject, body);
        sendEmail(emailRequest).subscribe();
    }

    private void onReservationCancelled(Reservation reservation) {
        String subject = "Your booking at Lux hotels has been cancelled";
        String body = "Your booking with reservation id " + reservation.getId() + " has been cancelled. We hope to see you soon!";

        EmailRequest emailRequest = new EmailRequest(reservation.getUser().getEmail(), subject, body);
        sendEmail(emailRequest).subscribe();
    }

    public Mono<String> sendEmail(EmailRequest emailRequest) {
        return this.webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/send-email").build())
                .bodyValue(emailRequest)
                .retrieve()
                .bodyToMono(String.class);
    }


}
