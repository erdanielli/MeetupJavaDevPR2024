package study.meetup.springuicada.application.util;

public interface UseCase<I, O> {

    O execute(I input);

}
