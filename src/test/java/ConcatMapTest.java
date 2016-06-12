import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

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

        assertThat(ts.getOnNextEvents(), contains("hi", "there", "how", "are", "you"));
    }

    @Test
    public void testConcatMapWithThreads() throws Exception {
        Observable<String> o1 =
                Observable.interval(10, TimeUnit.MILLISECONDS, scheduler)
                        .zipWith(Observable.range(0, 3), (i, r) -> i)
                        .map(num -> "one: " + num);
        Observable<String> o2 = Observable.interval(10, TimeUnit.MILLISECONDS, scheduler)
                .map(num -> "two: " + num);

        Observable.just(o1, o2)
                .concatMap(o -> o)
                .subscribe(ts);

        scheduler.advanceTimeBy(60, TimeUnit.MILLISECONDS);

        assertThat(ts.getOnNextEvents(), contains(
                "one: 0", "one: 1", "one: 2", "two: 0", "two: 1", "two: 2"));
    }

    @Test
    public void testSecondSubjectIgnoredUntilFirstCompletes() throws Exception {
        PublishSubject<String> s1 = PublishSubject.create();
        PublishSubject<String> s2 = PublishSubject.create();

        Observable.just(s1, s2)
                .concatMap(o -> o)
                .subscribe(ts);

        s1.onNext("one");
        s2.onNext("ignored");
        s1.onNext("two");
        s1.onCompleted();
        s2.onNext("three");

        assertThat(ts.getOnNextEvents(), contains("one", "two", "three"));
    }
}
