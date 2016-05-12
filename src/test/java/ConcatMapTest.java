import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class ConcatMapTest {
    TestSubscriber<String> ts = new TestSubscriber();
    private TestScheduler scheduler = new TestScheduler();

    @Test
    public void testConcatMap() throws Exception {
        Observable<Observable<String>> flattenMe = Observable.just(
                Observable.just("hi", "there"),
                Observable.just("how", "are", "you")
        );

        flattenMe.concatMap(observable -> observable).subscribe(ts);

        System.out.println(ts.getOnNextEvents().size());
        System.out.println(ts.getOnNextEvents());
    }

    @Test
    public void testConcatMapWithThreads() throws Exception {
        Observable<String> o1 =
                Observable.interval(10, TimeUnit.MILLISECONDS, scheduler)
                        .zipWith(Observable.range(0, 5), (i, r) -> i)
                        .map(num -> "one: " + num);
        Observable<String> o2 = Observable.interval(10, TimeUnit.MILLISECONDS, scheduler)
                .map(num -> "two: " + num);

        Subject<Observable<String>, Observable<String>> subject = PublishSubject.create();
        subject.concatMap(o -> o).subscribe(ts);
        subject.onNext(o1);
        scheduler.advanceTimeBy(20, TimeUnit.MILLISECONDS);
        subject.onNext(o2);

        scheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);

        System.out.println(ts.getOnNextEvents());
    }
}
