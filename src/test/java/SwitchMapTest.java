import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;
import rx.subjects.PublishSubject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class SwitchMapTest {
    @Test
    public void testOne() throws Exception {
        PublishSubject<String> subject = PublishSubject.create();
        TestSubscriber<String> ts = new TestSubscriber<>();
        subject
                .switchMap(str -> Observable.just("hi"))
                .subscribe(ts);

        subject.onNext("foo");
        assertThat(ts.getOnNextEvents(), contains("hi"));
    }

    @Test
    public void testTwo() throws Exception {
        TestScheduler testScheduler = new TestScheduler();
        TestSubscriber<String> ts = new TestSubscriber<>();
        Observable<String> ob1 = Observable.interval(10L, TimeUnit.MILLISECONDS, testScheduler)
                .map(ii -> "one");
        Observable<String> ob2 = Observable.interval(20L, 10L, TimeUnit.MILLISECONDS, testScheduler)
                .map(ii -> "two");

        ob1.switchMap(item -> ob2).subscribe(ts);

        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);

        // TODO: finish
        System.out.println(ts.getOnNextEvents());
    }
}
