import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class IntervalTest {

    @Test
    public void testIntervalWithTestScheduler() throws Exception {
        TestScheduler scheduler = new TestScheduler();
        List<Long> list = new ArrayList<>();

        Observable<Long> observable = Observable.interval(1, TimeUnit.SECONDS, scheduler).take(5);
        observable.subscribe(list::add);
        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        assertThat(list, contains(0L,1L,2L,3L,4L));
    }

    @Test
    public void testIntervalWithTestSchedulerAndTestSubscriber() throws Exception {
        TestScheduler scheduler = new TestScheduler();
        TestSubscriber<Long> sub = new TestSubscriber();

        Observable<Long> observable = Observable.interval(1, TimeUnit.SECONDS, scheduler).take(5);
        observable.subscribe(sub);
        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        assertThat(sub.getOnNextEvents(), contains(0L,1L,2L,3L,4L));
    }

    @Test
    public void testIntervalWithTestSubscriber() throws Exception {
        TestScheduler scheduler = new TestScheduler();
        TestSubscriber<Long> subscriber = TestSubscriber.create(3);

        Observable.interval(1, TimeUnit.SECONDS, scheduler).subscribe(subscriber);
        scheduler.advanceTimeBy(3, TimeUnit.SECONDS);

        assertThat(subscriber.getOnNextEvents(), contains(0L,1L,2L));
    }
}
