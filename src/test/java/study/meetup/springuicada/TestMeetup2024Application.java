package study.meetup.springuicada;

import org.springframework.boot.SpringApplication;

public class TestMeetup2024Application {

    public static void main(String[] args) {
        SpringApplication.from(Meetup2024Application::main).with(TestcontainersConfiguration.class).run(args);
    }

}
