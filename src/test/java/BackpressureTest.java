import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class BackpressureTest {
    @Test
    public void testBackpressureWithReplaySubject() throws Exception {
        ReplaySubject s = ReplaySubject.create();
        TestSubscriber ts = new TestSubscriber(1);
        s.subscribe(ts);

        s.onNext("hi");
        s.onNext("there");

        ts.assertValueCount(1);
    }

    @Test
    public void testBackpressureWorksWithBackpressureOperator() throws Exception {
        ReplaySubject s = ReplaySubject.create();
        TestSubscriber ts = new TestSubscriber(1);
        AtomicBoolean dropped = new AtomicBoolean(false);
        s.onBackpressureDrop(i -> dropped.set(true))
                .subscribe(ts);

        s.onNext("hi");
        s.onNext("there");

        ts.assertValueCount(1);
        assertThat(dropped.get(), is(true));
    }

    @Test
    public void testBackpressureDoesntWorkWithBehaviorSubject() throws Exception {
        BehaviorSubject s = BehaviorSubject.create();
        TestSubscriber ts = new TestSubscriber(1);
        s.subscribe(ts);

        s.onNext("hi");
        s.onNext("there");

        // This is bad, it ignores backpressure, it should have ony gotten 1
        // because we told the test subscriber to only ask for 1
        ts.assertValueCount(2);
    }

    @Test
    public void testBackpressureWithThreadAndSubject() throws Exception {
        PublishSubject s = PublishSubject.create();
        TestSubscriber ts = new TestSubscriber(1);
        CountDownLatch latch = new CountDownLatch(1);
        s.observeOn(Schedulers.newThread())
                .onBackpressureDrop(i -> latch.countDown())
                .subscribe(ts);

        s.onNext("hi");
        s.onNext("there");

        assertThat("drop called", latch.await(100, TimeUnit.MILLISECONDS), is(true));
        ts.assertValueCount(1);
    }

    @Test
    public void testBackpressureWithThreadDoesntWorkIfBackpressureOperatorFirst() throws Exception {
        PublishSubject s = PublishSubject.create();
        TestSubscriber ts = new TestSubscriber(1);
        CountDownLatch latch = new CountDownLatch(1);
        s.onBackpressureDrop(i -> latch.countDown())
                .observeOn(Schedulers.newThread())
                .subscribe(ts);

        s.onNext("hi");
        s.onNext("there");

        assertThat("drop called", latch.await(100, TimeUnit.MILLISECONDS), is(false));
        ts.assertValueCount(1);
    }

    @Test
    public void testBackpressureWithThread() throws Exception {
        TestSubscriber ts = new TestSubscriber(1);
        CountDownLatch latch = new CountDownLatch(1);
        Observable<Object> o = Observable.create(subscriber -> {
            subscriber.onNext("hi");
            subscriber.onNext("there");
        });
        o.observeOn(Schedulers.newThread())
                .onBackpressureDrop(i -> latch.countDown())
                .subscribe(ts);

        assertThat("drop called", latch.await(100, TimeUnit.MILLISECONDS), is(true));
        ts.assertValueCount(1);
    }

    // TODO: test observeOn(scheduler, 1)
    // One critical thing is missing here, o.observeOn(scheduler, 1); where 1 is the buffer size.
    // This was important to solving an issue once, should write tests for it...

}
